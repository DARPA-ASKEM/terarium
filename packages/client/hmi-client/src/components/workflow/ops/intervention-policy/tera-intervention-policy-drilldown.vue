<template>
	<tera-drilldown
		:node="node"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
		hide-dropdown
	>
		<template #sidebar>
			<tera-slider-panel v-model:is-open="isSidebarOpen" content-width="360px" header="Intervention policies">
				<template #content>
					<section>
						<tera-input-text v-model="filterInterventionsText" placeholder="Filter" />
						<ul v-if="!isFetchingPolicies">
							<li v-for="policy in interventionPoliciesFiltered" :key="policy.id">
								<tera-intervention-policy-card
									:interventionPolicy="policy"
									:selected="selectedPolicy?.id === policy.id"
									@click="onReplacePolicy(policy)"
									@use-intervention="onReplacePolicy(policy)"
								/>
							</li>
						</ul>
						<tera-progress-spinner v-else is-centered />
					</section>
				</template>
			</tera-slider-panel>
		</template>
		<tera-columnar-panel>
			<tera-drilldown-section class="px-3">
				<template #header-controls-left> Select an intervention policy or create a new one here. </template>
				<template #header-controls-right>
					<Button outlined severity="secondary" label="Reset" @click="onResetPolicy" />
				</template>
				<ul class="flex flex-column gap-2">
					<li v-for="(intervention, index) in knobs.transientInterventionPolicy.interventions" :key="index">
						<tera-intervention-card
							:intervention="intervention"
							:parameterOptions="parameterOptions"
							:stateOptions="stateOptions"
							@update="onUpdateInterventionCard($event, index)"
							@delete="onDeleteIntervention(index)"
						/>
					</li>
				</ul>
				<Button
					class="align-self-start mt-2"
					text
					label="Add intervention"
					@click="addIntervention"
					icon="pi pi-plus"
					size="small"
				/>
			</tera-drilldown-section>
			<tera-drilldown-section>
				<template #header-controls-left>
					<tera-toggleable-input
						v-if="typeof knobs.transientInterventionPolicy.name === 'string'"
						:model-value="knobs.transientInterventionPolicy.name"
						@update:model-value="onChangeName"
						tag="h4"
					/>
				</template>
				<template #header-controls-right>
					<Button label="Save as..." outlined severity="secondary" @click="showSaveModal = true" />
					<Button class="mr-3" label="Save" @click="onSaveInterventionPolicy" :disabled="isSaveDisabled" />
				</template>
				<Accordion v-if="knobs.transientInterventionPolicy.id" multiple :active-index="[0, 1]">
					<AccordionTab>
						<template #header>
							<Button v-if="!isEditingDescription" class="start-edit" text @click.stop="onEditDescription">
								<h5 class="btn-content">Description</h5>
								<i class="pi pi-pencil" />
							</Button>
							<span v-else class="confirm-cancel">
								<span>Description</span>
								<Button icon="pi pi-times" text @click.stop="isEditingDescription = false" />
								<Button icon="pi pi-check" text @click.stop="onConfirmEditDescription" />
							</span>
						</template>
						<p class="description text" v-if="!isEditingDescription">
							{{ knobs.transientInterventionPolicy.description }}
						</p>
						<Textarea
							v-else
							ref="descriptionTextareaRef"
							class="w-full"
							placeholder="Enter a description"
							v-model="newDescription"
						/>
					</AccordionTab>
					<AccordionTab header="Charts">
						<ul class="flex flex-column gap-2">
							<li v-for="(interventions, appliedTo) in groupedOutputParameters" :key="appliedTo">
								<h5 class="pb-2">{{ appliedTo }}</h5>
								<vega-chart
									expandable
									:are-embed-actions-visible="false"
									:visualization-spec="preparedCharts[appliedTo]"
								/>
								<ul>
									<li class="pb-2" v-for="intervention in interventions" :key="intervention.name">
										<h6 class="pb-1">{{ intervention.name }}</h6>
										<ul v-if="!isEmpty(intervention.staticInterventions)">
											<li
												v-for="staticIntervention in intervention.staticInterventions"
												:key="staticIntervention.timestep"
											>
												<p>
													Set {{ intervention.type }} {{ appliedTo }} to {{ staticIntervention.value }} at time step
													{{ staticIntervention.timestep }}.
												</p>
											</li>
										</ul>
										<p v-else-if="!isEmpty(intervention.dynamicInterventions)">
											Set {{ intervention.type }} {{ appliedTo }} to
											{{ intervention.dynamicInterventions[0].value }} when the
											{{ intervention.dynamicInterventions[0].parameter }}
											{{
												intervention.dynamicInterventions[0].isGreaterThan ? 'increases to above' : 'decreases to below'
											}}
											{{ intervention.dynamicInterventions[0].threshold }}.
										</p>
									</li>
								</ul>
							</li>
						</ul>
					</AccordionTab>
				</Accordion>
				<div v-else class="flex align-items-center h-full">
					<Vue3Lottie :animationData="EmptySeed" :height="150" loop autoplay />
				</div>
			</tera-drilldown-section>
		</tera-columnar-panel>
	</tera-drilldown>
	<tera-save-asset-modal
		:initial-name="knobs.transientInterventionPolicy.name"
		:is-visible="showSaveModal"
		:asset="knobs.transientInterventionPolicy"
		:asset-type="AssetType.InterventionPolicy"
		@close-modal="showSaveModal = false"
		@on-save="onSaveAsInterventionPolicy"
	/>
</template>

<script setup lang="ts">
import _, { cloneDeep, groupBy, isEmpty } from 'lodash';
import { computed, onMounted, ref, watch, nextTick, ComponentPublicInstance } from 'vue';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import { WorkflowNode } from '@/types/workflow';
import TeraSliderPanel from '@/components/widgets/tera-slider-panel.vue';
import TeraColumnarPanel from '@/components/widgets/tera-columnar-panel.vue';
import Button from 'primevue/button';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import { getInterventionPoliciesForModel, getModel } from '@/services/model';
import { Intervention, InterventionPolicy, Model, AssetType } from '@/types/Types';
import { logger } from '@/utils/logger';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import { useConfirm } from 'primevue/useconfirm';
import { getParameters, getStates } from '@/model-representation/service';
import TeraToggleableInput from '@/components/widgets/tera-toggleable-input.vue';
import { getInterventionPolicyById, updateInterventionPolicy, blankIntervention } from '@/services/intervention-policy';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Textarea from 'primevue/textarea';
import EmptySeed from '@/assets/images/lottie-empty-seed.json';
import { Vue3Lottie } from 'vue3-lottie';
import { sortDatesDesc } from '@/utils/date';
import { createInterventionChart } from '@/services/charts';
import VegaChart from '@/components/widgets/VegaChart.vue';
import TeraSaveAssetModal from '@/components/project/tera-save-asset-modal.vue';
import TeraInterventionCard from './tera-intervention-card.vue';
import {
	InterventionPolicyOperation,
	InterventionPolicyState,
	isInterventionPoliciesValuesEqual,
	isInterventionPoliciesEqual
} from './intervention-policy-operation';
import TeraInterventionPolicyCard from './tera-intervention-policy-card.vue';

const props = defineProps<{
	node: WorkflowNode<InterventionPolicyState>;
}>();

const emit = defineEmits(['close', 'update-state', 'select-output', 'append-output', 'update-output-port']);

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

const showSaveModal = ref(false);
const isSidebarOpen = ref(true);
const filterInterventionsText = ref('');
const model = ref<Model | null>(null);
const isFetchingPolicies = ref(false);
const interventionsPolicyList = ref<InterventionPolicy[]>([]);
const interventionPoliciesFiltered = computed(() =>
	interventionsPolicyList.value
		.filter((policy) => policy.name?.toLowerCase().includes(filterInterventionsText.value.toLowerCase()))
		.sort((a, b) => sortDatesDesc(a.createdOn, b.createdOn))
);
const selectedOutputId = ref<string>('');
const selectedPolicy = ref<InterventionPolicy | null>(null);

const newDescription = ref('');
const descriptionTextareaRef = ref<ComponentPublicInstance<typeof Textarea> | null>(null);
const isEditingDescription = ref(false);
const isSaveDisabled = computed(
	() =>
		knobs.value.transientInterventionPolicy.id !== selectedPolicy.value?.id ||
		isInterventionPoliciesEqual(knobs.value.transientInterventionPolicy, selectedPolicy.value) ||
		!isInterventionPoliciesValuesEqual(knobs.value.transientInterventionPolicy, selectedPolicy.value)
);

const parameterOptions = computed(() => {
	if (!model.value) return [];
	return getParameters(model.value).map((parameter) => ({
		label: parameter.name ?? parameter.id,
		value: parameter.id
	}));
});

const stateOptions = computed(() => {
	if (!model.value) return [];
	return getStates(model.value).map((state) => ({
		label: !isEmpty(state.name) ? state.name : state.id,
		value: state.id
	}));
});

const groupedOutputParameters = computed(() =>
	groupBy(knobs.value.transientInterventionPolicy.interventions, 'appliedTo')
);

const preparedCharts = computed(() =>
	_.mapValues(groupedOutputParameters.value, (interventions) => {
		const flattenedData = interventions.flatMap((intervention) =>
			intervention.staticInterventions.map((staticIntervention) => ({
				name: intervention.name,
				value: staticIntervention.value,
				time: staticIntervention.timestep
			}))
		);
		return createInterventionChart(flattenedData);
	})
);

const initialize = async () => {
	const state = props.node.state;
	const modelId = props.node.inputs[0].value?.[0];
	if (!modelId) return;

	// fetch intervention policies
	await fetchInterventionPolicies(modelId);

	model.value = await getModel(modelId);
	if (state.interventionPolicy?.id) {
		// copy the state into the knobs if it exists
		selectedPolicy.value = await getInterventionPolicyById(state.interventionPolicy.id);
	}
	knobs.value.transientInterventionPolicy = cloneDeep(state.interventionPolicy);
};

const applyInterventionPolicy = (interventionPolicy: InterventionPolicy) => {
	const state = cloneDeep(props.node.state);
	if (!interventionPolicy?.id) {
		logger.error('Policy not found');
		return;
	}
	knobs.value.transientInterventionPolicy = cloneDeep(interventionPolicy);

	const listOfPolicyIds: string[] = props.node.outputs.map((output) => output.value?.[0]);
	// Check if this output already exists
	if (listOfPolicyIds.includes(interventionPolicy.id)) {
		// Select the existing output
		const output = props.node.outputs.find((ele) => ele.value?.[0] === interventionPolicy.id);
		emit('select-output', output?.id);
	}
	// If the output does not already exist
	else {
		// Append this config to the output.
		state.interventionPolicy = interventionPolicy;
		emit('append-output', {
			type: InterventionPolicyOperation.outputs[0].type,
			label: interventionPolicy.name,
			value: interventionPolicy.id,
			state
		});
	}
	logger.success(`Policy applied ${interventionPolicy.name}`);
};

const fetchInterventionPolicies = async (modelId: string) => {
	isFetchingPolicies.value = true;
	interventionsPolicyList.value = await getInterventionPoliciesForModel(modelId);
	isFetchingPolicies.value = false;
};

const onUpdateInterventionCard = (intervention: Intervention, index: number) => {
	// Clone the entire interventions array
	const updatedInterventions = [...knobs.value.transientInterventionPolicy.interventions];

	// Replace the intervention at the specified index with a deep clone of the updated intervention
	updatedInterventions[index] = cloneDeep(intervention);

	// Reassign the updated interventions array back to the transientInterventionPolicy
	// This ensures that we're not modifying the original array in place
	knobs.value.transientInterventionPolicy.interventions = updatedInterventions;
};

const onReplacePolicy = (policy: InterventionPolicy) => {
	if (selectedPolicy.value?.id === policy.id) return;
	if (isSaveDisabled.value) {
		applyInterventionPolicy(policy);
	} else {
		confirm.require({
			header: 'Are you sure you want to use this intervention policy?',
			message: `All current interventions will be replaced with those in the selected policy, “${policy.name}” This action cannot be undone.`,
			accept: () => applyInterventionPolicy(policy),
			acceptLabel: 'Confirm',
			rejectLabel: 'Cancel'
		});
	}
};

const addIntervention = () => {
	// by default add the first parameter with a static intervention
	knobs.value.transientInterventionPolicy.interventions.push(blankIntervention);
};

const onDeleteIntervention = (index: number) => {
	// Create a new array excluding the intervention at the specified index
	const updatedInterventions = knobs.value.transientInterventionPolicy.interventions.filter(
		(_intervention, i) => i !== index
	);

	// Reassign the updated interventions array back to the transientInterventionPolicy
	// This ensures that we're not modifying the original array in place and Vue's reactivity system detects the change
	knobs.value.transientInterventionPolicy.interventions = updatedInterventions;

	// If the deleted intervention was the last one, add a new empty one
	if (isEmpty(knobs.value.transientInterventionPolicy.interventions)) {
		addIntervention();
	}
};

const onChangeName = async (name: string) => {
	knobs.value.transientInterventionPolicy.name = name;
};

const onEditDescription = async () => {
	isEditingDescription.value = true;
	newDescription.value = knobs.value.transientInterventionPolicy.description ?? '';
	await nextTick();
	descriptionTextareaRef.value?.$el.focus();
};

const onConfirmEditDescription = async () => {
	knobs.value.transientInterventionPolicy.description = newDescription.value;
	isEditingDescription.value = false;
};

const onResetPolicy = () => {
	confirm.require({
		header: 'Are you sure you want to reset the policy?',
		message: 'This action cannot be undone.',
		accept: () => {
			if (selectedPolicy.value) knobs.value.transientInterventionPolicy = cloneDeep(selectedPolicy.value);
		},
		acceptLabel: 'Confirm',
		rejectLabel: 'Cancel'
	});
};

const onSaveAsInterventionPolicy = (data: InterventionPolicy) => {
	applyInterventionPolicy(data);
};

const onSaveInterventionPolicy = async () => {
	const policy = cloneDeep(knobs.value.transientInterventionPolicy);
	const data = await updateInterventionPolicy(policy);
	if (!data) {
		logger.error('Failed to save intervention policy');
		return;
	}
	initialize();
};

watch(
	() => knobs.value,
	async () => {
		const state = cloneDeep(props.node.state);
		state.interventionPolicy = knobs.value.transientInterventionPolicy;
		emit('update-state', state);
	},
	{ deep: true }
);

watch(
	() => props.node.active,
	() => {
		if (props.node.active) {
			selectedOutputId.value = props.node.active;
			initialize();
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

section {
	display: flex;
	flex-direction: column;
	gap: var(--gap);
	padding: 0 var(--gap);
}

button.start-edit {
	display: flex;
	gap: var(--gap-3);
	width: fit-content;
	padding: var(--gap-2);

	& > .btn-content {
		color: var(--text-color);
	}

	& > .pi {
		color: var(--text-color-subdued);
	}
}

.confirm-cancel {
	display: flex;
	align-items: center;
	gap: var(--gap-1);
	& > span {
		margin-left: var(--gap-2);
	}
}
</style>
