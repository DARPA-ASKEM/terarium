<template>
	<tera-drilldown :node="node" @on-close-clicked="emit('close')">
		<template #sidebar>
			<tera-slider-panel
				v-model:is-open="isSidebarOpen"
				content-width="360px"
				header="Intervention policies"
			>
				<template #content>
					<div class="m-3">
						<div class="flex flex-column gap-1">
							<tera-input v-model="filterInterventionsText" placeholder="Filter" />
						</div>
						<ul v-if="!isFetchingPolicies">
							<li v-for="policy in interventionPoliciesFiltered" :key="policy.id">
								<tera-interventions-policy-card :interventionPolicy="policy" :selected="true" />
							</li>
						</ul>
						<tera-progress-spinner v-else is-centered />
					</div>
				</template>
			</tera-slider-panel>
		</template>
		<tera-columnar-panel>
			<tera-drilldown-section class="pl-3 pr-3">
				<template #header-controls-left>
					<span>Select an intervention policy or create a new one here.</span>
				</template>
				<template #header-controls-right>
					<Button outlined severity="secondary" label="Reset"></Button>
					<Button @click="console.log('run')" label="Run" />
				</template>
				<ul>
					<li
						v-for="(intervention, index) in knobs.transientInterventionPolicy.values"
						:key="index"
					>
						<tera-intervention-card
							:intervention="intervention"
							@update="onUpdate($event, index)"
						/>
					</li>
				</ul>
			</tera-drilldown-section>
			<div>test2</div>
		</tera-columnar-panel>
	</tera-drilldown>
</template>

<script setup lang="ts">
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import { WorkflowNode } from '@/types/workflow';
import TeraSliderPanel from '@/components/widgets/tera-slider-panel.vue';
import { computed, onMounted, ref, watch } from 'vue';
import TeraColumnarPanel from '@/components/widgets/tera-columnar-panel.vue';
import { cloneDeep, uniqueId } from 'lodash';
import Button from 'primevue/button';
import TeraInput from '@/components/widgets/tera-input.vue';
import { getModel } from '@/services/model';
import { Model } from '@/types/Types';
import { logger } from '@/utils/logger';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import TeraInterventionsPolicyCard from './tera-interventions-policy-card.vue';
import {
	DummyIntervention,
	DummyInterventionPolicy,
	InterventionsOperation,
	InterventionsState
} from './tera-interventions-operation';
import TeraInterventionCard from './tera-intervention-card.vue';

const interventionPolicies: DummyInterventionPolicy[] = [
	{
		id: uniqueId(),
		name: 'Policy 1',
		description: 'Policy 1',
		createdOn: new Date(),
		modelId: 'model1',
		values: [
			{ name: 'Start masking', parameterId: 'beta', setting: [{ threshold: 0.1, timestep: 1 }] }
		]
	},
	{
		id: uniqueId(),
		name: 'Policy 2',
		description: 'Policy 2',
		createdOn: new Date(),
		modelId: 'model1',
		values: [
			{
				name: 'End masking',
				parameterId: 'beta',
				setting: [{ parameterId: 'Infected_Old', threshold: 1050, timestep: 0.75 }]
			}
		]
	}
];

const props = defineProps<{
	node: WorkflowNode<InterventionsState>;
}>();
const emit = defineEmits(['close', 'update-state', 'select-output', 'append-output']);

interface BasicKnobs {
	transientInterventionPolicy: DummyInterventionPolicy;
}

const knobs = ref<BasicKnobs>({
	transientInterventionPolicy: {
		name: '',
		description: '',
		modelId: '',
		values: []
	}
});

const isSidebarOpen = ref(true);
const filterInterventionsText = ref('');
const model = ref<Model | null>(null);
const isFetchingPolicies = ref(false);
const interventionsPolicyList = ref<DummyInterventionPolicy[]>([]);
const interventionPoliciesFiltered = computed(() =>
	interventionsPolicyList.value.filter((policy) =>
		policy.name?.toLowerCase().includes(filterInterventionsText.value.toLowerCase())
	)
);

const initialize = async () => {
	const state = props.node.state;
	const modelId = props.node.inputs[0].value?.[0];
	if (!modelId) return;

	// fetch intervention policies
	await fetchInterventionPolicies(modelId);

	model.value = await getModel(modelId);
	if (state.transientInterventionPolicy.id) {
		// copy the state into the knobs if it exists
		knobs.value.transientInterventionPolicy = cloneDeep(state.transientInterventionPolicy);
	} else {
		applyInterventionPolicy(interventionPolicies[0]);
	}
};

const applyInterventionPolicy = (interventionPolicy: DummyInterventionPolicy) => {
	const state = cloneDeep(props.node.state);
	knobs.value.transientInterventionPolicy = cloneDeep(interventionPolicy);

	const listOfPolicyIds: string[] = props.node.outputs.map((output) => output.value?.[0]);
	// Update output port:
	if (!interventionPolicy.id) {
		logger.error('Policy not found');
		return;
	}
	// Check if this output already exists
	if (listOfPolicyIds.includes(interventionPolicy.id)) {
		// Select the existing output
		const output = props.node.outputs.find((ele) => ele.value?.[0] === interventionPolicy.id);
		emit('select-output', output?.id);
	}
	// If the output does not already exist
	else {
		// Append this config to the output.
		state.transientInterventionPolicy = interventionPolicy;
		emit('append-output', {
			type: InterventionsOperation.outputs[0].type,
			label: interventionPolicy.name,
			value: interventionPolicy.id,
			state
		});
	}
	logger.success(`Policy applied ${interventionPolicy.name}`);
};

const fetchInterventionPolicies = async (modelId: string) => {
	isFetchingPolicies.value = true;
	console.log(modelId);
	// const response = await getInterventionPolicies(model.value?.id);
	interventionsPolicyList.value = interventionPolicies;
	isFetchingPolicies.value = false;
};

const onUpdate = (intervention: DummyIntervention, index: number) => {
	knobs.value.transientInterventionPolicy.values[index] = cloneDeep(intervention);
};

watch(
	() => knobs.value,
	async () => {
		const state = cloneDeep(props.node.state);
		state.transientInterventionPolicy = knobs.value.transientInterventionPolicy;
		emit('update-state', state);
	},
	{ deep: true }
);

onMounted(() => {
	initialize();
});
</script>

<style scoped>
ul {
	list-style: none;
}
</style>
