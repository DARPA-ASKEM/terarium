<template>
	<tera-drilldown
		:node="node"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
		@update:selection="onSelection"
	>
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
								<tera-interventions-policy-card
									:interventionPolicy="policy"
									:selected="selectedPolicyId === policy.id"
									@click="onUsePolicy(policy)"
									@use="onUsePolicy(policy)"
								/>
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
				<ul class="flex flex-column gap-2">
					<li
						v-for="(intervention, index) in knobs.transientInterventionPolicy.interventions"
						:key="index"
					>
						<tera-intervention-card
							:intervention="intervention"
							:parameterOptions="parameterOptions"
							@update="onUpdate($event, index)"
							@delete="onDeleteIntervention(index)"
						/>
					</li>
				</ul>
				<span>
					<Button
						text
						label="Add intervention"
						@click="onAddIntervention"
						icon="pi pi-plus"
						size="small"
					/>
				</span>
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
import { cloneDeep } from 'lodash';
import Button from 'primevue/button';
import TeraInput from '@/components/widgets/tera-input.vue';
import { getInterventionPoliciesForModel, getModel } from '@/services/model';
import { Intervention, InterventionPolicy, Model } from '@/types/Types';
import { logger } from '@/utils/logger';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import { useConfirm } from 'primevue/useconfirm';
import { getParameters } from '@/model-representation/service';
import TeraInterventionsPolicyCard from './tera-interventions-policy-card.vue';
import { InterventionsOperation, InterventionsState } from './tera-interventions-operation';
import TeraInterventionCard from './tera-intervention-card.vue';

const props = defineProps<{
	node: WorkflowNode<InterventionsState>;
}>();
const emit = defineEmits(['close', 'update-state', 'select-output', 'append-output']);

const confirm = useConfirm();
interface BasicKnobs {
	transientInterventionPolicy: InterventionPolicy;
}

const knobs = ref<BasicKnobs>({
	transientInterventionPolicy: {
		name: '',
		description: '',
		modelId: '',
		interventions: []
	}
});

const isSidebarOpen = ref(true);
const filterInterventionsText = ref('');
const model = ref<Model | null>(null);
const isFetchingPolicies = ref(false);
const interventionsPolicyList = ref<InterventionPolicy[]>([]);
const interventionPoliciesFiltered = computed(() =>
	interventionsPolicyList.value.filter((policy) =>
		policy.name?.toLowerCase().includes(filterInterventionsText.value.toLowerCase())
	)
);
const selectedOutputId = ref<string>('');
const selectedPolicyId = computed(
	() => props.node.outputs?.find((o) => o.id === selectedOutputId.value)?.value?.[0]
);

const parameterOptions = computed(() => {
	if (!model.value) return [];
	return getParameters(model.value).map((parameter) => ({
		label: parameter.name ?? parameter.id,
		value: parameter.id
	}));
});

const initialize = async () => {
	const state = props.node.state;
	const modelId = props.node.inputs[0].value?.[0];
	if (!modelId) return;

	// fetch intervention policies
	await fetchInterventionPolicies(modelId);

	model.value = await getModel(modelId);
	if (state.transientInterventionPolicy?.id) {
		// copy the state into the knobs if it exists
		knobs.value.transientInterventionPolicy = cloneDeep(state.transientInterventionPolicy);
	} else {
		const policies = await getInterventionPoliciesForModel(modelId);
		applyInterventionPolicy(policies[0]);
	}
};

const applyInterventionPolicy = (interventionPolicy: InterventionPolicy) => {
	const state = cloneDeep(props.node.state);
	knobs.value.transientInterventionPolicy = cloneDeep(interventionPolicy);

	const listOfPolicyIds: string[] = props.node.outputs.map((output) => output.value?.[0]);
	// Update output port:
	if (!interventionPolicy?.id) {
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
	interventionsPolicyList.value = await getInterventionPoliciesForModel(modelId);
	isFetchingPolicies.value = false;
};

const onUpdate = (intervention: Intervention, index: number) => {
	knobs.value.transientInterventionPolicy.interventions[index] = cloneDeep(intervention);
};

const onSelection = (id: string) => {
	emit('select-output', id);
};

const onUsePolicy = (policy: InterventionPolicy) => {
	confirm.require({
		header: 'Are you sure you want to use this intervention policy?',
		message: `All current interventions will be replaced with those in the selected policy, “${policy.name}” This action cannot be undone.`,
		accept: () => applyInterventionPolicy(policy),
		acceptLabel: 'Confirm',
		rejectLabel: 'Cancel'
	});
};

const onAddIntervention = () => {
	// by default add the first parameter with a static intervention
	const intervention: Intervention = {
		name: 'New Intervention',
		appliedTo: parameterOptions.value[0].value,
		staticInterventions: [{ threshold: 0, value: 0 }],
		dynamicInterventions: []
	};
	knobs.value.transientInterventionPolicy.interventions.push(intervention);
};

const onDeleteIntervention = (index: number) => {
	knobs.value.transientInterventionPolicy.interventions.splice(index, 1);
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

watch(
	() => props.node.active,
	() => {
		if (props.node.active) {
			selectedOutputId.value = props.node.active;
		}
	},
	{ immediate: true }
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
