<template>
	<tera-drilldown
		:node="node"
		:menu-items="menuItems"
		@update:selection="onSelection"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
	>
		<!---<template #sidebar>
			<tera-slider-panel v-model:is-open="isSidebarOpen" header="Validate configuration settings" content-width="360px">
				<template #content>

				</template>
			</tera-slider-panel>
		</template>-->
		<div :tabName="FunmanTabs.Wizard" class="px-3 wizard-section">
			<tera-drilldown-section>
				<template #header-controls-right>
					<Button :loading="showSpinner" class="run-button" label="Run" icon="pi pi-play" @click="runMakeQuery" />
				</template>
				<main>
					<Accordion multiple :active-index="[0, 1]" class="accordion-component">
						<AccordionTab header="Model checks">
							<!---<i class="pi pi-info-circle" v-tooltip="validateParametersToolTip" />-->
							<p class="mt-1">Model configurations will be tested against these constraints.</p>
							<tera-compartment-constraint :variables="modelStates" :mass="mass" />
							<tera-constraint-group-form
								v-for="(cfg, index) in node.state.constraintGroups"
								:key="selectedOutputId + ':' + index"
								:config="cfg"
								:index="index"
								:model-states="modelStates"
								:model-parameters="modelParameters"
								@delete-self="deleteConstraintGroupForm"
								@update-self="updateConstraintGroupForm"
							/>
							<Button
								class="add-constraint-spacer"
								text
								icon="pi pi-plus"
								label="Add new check"
								size="small"
								@click="addConstraintForm"
							/>
						</AccordionTab>
						<AccordionTab header="Settings">
							<!---<i class="pi pi-info-circle" v-tooltip="validateParametersToolTip" />-->
							<p class="mt-1">The validator will use these parameters to execute the sanity checks.</p>
							<div class="section-row timespan">
								<div class="w-full">
									<label>Start time</label>
									<tera-input-number v-model="knobs.currentTimespan.start" />
								</div>
								<div class="w-full">
									<label>End time</label>
									<tera-input-number v-model="knobs.currentTimespan.end" />
								</div>
								<div class="w-full">
									<label>Number of steps</label>
									<tera-input-number v-model="knobs.numberOfSteps" />
								</div>
							</div>
							<tera-input-text :disabled="true" class="timespan-list mb-2" v-model="requestStepListString" />
							<template v-if="showAdditionalOptions">
								<div>
									<label>Tolerance</label>
									<div class="input-tolerance fadein animation-ease-in-out animation-duration-350">
										<tera-input-number v-model="knobs.tolerance" />
										<Slider v-model="knobs.tolerance" :min="0" :max="1" :step="0.01" class="w-full mr-2" />
									</div>
								</div>
								<div class="section-row fadein animation-duration-600">
									<!-- This will definitely require a proper tool tip. -->
									<label class="w-auto mr-2">Select parameters of interest <i class="pi pi-info-circle" /></label>
									<MultiSelect
										ref="columnSelect"
										:modelValue="variablesOfInterest"
										:options="requestParameters.map((d: any) => d.name)"
										:show-toggle-all="false"
										class="w-auto"
										@update:modelValue="onToggleVariableOfInterest"
										:maxSelectedLabels="1"
										placeholder="Select variables"
									/>
								</div>
							</template>
							<Button
								text
								icon="pi pi-eye"
								label="Show additional options"
								size="small"
								v-if="!showAdditionalOptions"
								@click="toggleAdditonalOptions"
							/>
							<Button
								text
								icon="pi pi-eye-slash"
								label="Hide additional options"
								size="small"
								v-if="showAdditionalOptions"
								@click="toggleAdditonalOptions"
							/>
						</AccordionTab>
					</Accordion>
				</main>
			</tera-drilldown-section>
		</div>
		<div :tabName="FunmanTabs.Notebook">
			<tera-drilldown-section>
				<main>
					<!-- TODO: notebook functionality -->
					<p>{{ requestConstraints }}</p>
				</main>
			</tera-drilldown-section>
		</div>

		<template #preview>
			<tera-drilldown-preview
				title="Validation results"
				v-model:output="selectedOutputId"
				@update:selection="onSelection"
				:options="outputs"
				is-selectable
				class="pb-3 pl-2 pr-4"
			>
				<template v-if="showSpinner">
					<tera-progress-spinner :font-size="2" is-centered style="height: 100%" />
				</template>
				<template v-else>
					<tera-funman-output
						v-if="activeOutput"
						:fun-model-id="activeOutput.value?.[0]"
						:trajectoryState="node.state.trajectoryState"
						@update:trajectoryState="updateTrajectorystate"
					/>
					<div v-else class="flex flex-column h-full justify-content-center">
						<tera-operator-placeholder :node="node" />
					</div>
				</template>
			</tera-drilldown-preview>
		</template>
	</tera-drilldown>
</template>

<script setup lang="ts">
import _, { floor } from 'lodash';
import { computed, ref, watch } from 'vue';
import Button from 'primevue/button';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import TeraInputNumber from '@/components/widgets/tera-input-number.vue';
import Slider from 'primevue/slider';
import MultiSelect from 'primevue/multiselect';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';

import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
// import TeraSliderPanel from '@/components/widgets/tera-slider-panel.vue';

import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';

import type { FunmanPostQueriesRequest, Model, ModelConfiguration, ModelParameter } from '@/types/Types';
import { makeQueries } from '@/services/models/funman-service';
import { WorkflowNode, WorkflowOutput } from '@/types/workflow';
import { getAsConfiguredModel, getModelConfigurationById } from '@/services/model-configurations';
import { useToastService } from '@/services/toast';
import { pythonInstance } from '@/python/PyodideController';
import TeraFunmanOutput from '@/components/workflow/ops/funman/tera-funman-output.vue';
import TeraCompartmentConstraint from '@/components/workflow/ops/funman/tera-compartment-constraint.vue';
import TeraConstraintGroupForm from '@/components/workflow/ops/funman/tera-constraint-group-form.vue';
import { FunmanOperationState, ConstraintGroup } from './funman-operation';

const props = defineProps<{
	node: WorkflowNode<FunmanOperationState>;
}>();

const emit = defineEmits(['append-output', 'select-output', 'update-state', 'close']);

enum FunmanTabs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}
const toast = useToastService();
/* const validateParametersToolTip =
	'Validate the configuration of the model using functional model analysis (FUNMAN). \n \n The parameter space regions defined by the model configuration are evaluated to satisfactory or unsatisfactory depending on whether they generate model outputs that are within a given set of time-dependent constraints';
*/
const showSpinner = ref(false);
const showAdditionalOptions = ref(false);
// const isSidebarOpen = ref(true);

interface BasicKnobs {
	tolerance: number;
	currentTimespan: {
		start: number;
		end: number;
	};
	numberOfSteps: number;
	useCompartmentalConstraint: boolean;
}

const knobs = ref<BasicKnobs>({
	tolerance: 0,
	currentTimespan: { start: 0, end: 0 },
	numberOfSteps: 0,
	useCompartmentalConstraint: false
});

const mass = ref('0');

const requestStepList = computed(() => getStepList());
const requestStepListString = computed(() => requestStepList.value.join()); // Just used to display. dont like this but need to be quick

const MAX = 99999999999;
const requestConstraints = computed(() =>
	// Same as node state's except typing for state vs linear constraint
	props.node.state.constraintGroups?.map((ele) => {
		if (ele.constraintType === 'monotonicityConstraint') {
			const weights = ele.weights ? ele.weights : [1.0];
			const constraint: any = {
				soft: true,
				name: ele.name,
				timepoints: null,
				additive_bounds: {
					lb: 0.0,
					// ub: 0.0,
					// closed_upper_bound: true,
					original_width: MAX
				},
				variables: ele.variables,
				weights: weights.map((d) => -Math.abs(d)), // should be all negative
				derivative: true
			};

			if (ele.derivativeType === 'increasing') {
				// delete constraint.additive_bounds.closed_upper_bound;
				// delete constraint.additive_bounds.ub;
				// constraint.additive_bounds.lb = 0;
				constraint.weights = weights.map((d) => Math.abs(d)); // should be all positive
			}
			return constraint;
		}

		if (ele.timepoints) {
			ele.timepoints.closed_upper_bound = true;
		}
		if (ele.variables.length === 1) {
			// State Variable Constraint
			const singleVarConstraint = {
				name: ele.name,
				variable: ele.variables[0],
				interval: ele.interval,
				timepoints: ele.timepoints
			};
			return singleVarConstraint;
		}

		return {
			// Linear Constraint
			name: ele.name,
			variables: ele.variables,
			weights: ele.weights,
			additive_bounds: ele.interval,
			timepoints: ele.timepoints
		};
	})
);

const requestParameters = ref<any[]>([]);
const model = ref<Model | null>();
const modelConfiguration = ref<ModelConfiguration>();

const modelStates = ref<string[]>([]); // Used for form's multiselect.
const modelParameters = ref<string[]>([]);

const selectedOutputId = ref<string>();
const outputs = computed(() => {
	if (!_.isEmpty(props.node.outputs)) {
		return [
			{
				label: 'Select an output',
				items: props.node.outputs
			}
		];
	}
	return [];
});

const activeOutput = ref<WorkflowOutput<FunmanOperationState> | null>(null);

const toggleAdditonalOptions = () => {
	showAdditionalOptions.value = !showAdditionalOptions.value;
};

const menuItems = computed(() => [
	{
		label: 'Save as new model configurations',
		icon: 'pi pi-pencil',
		disabled: true,
		command: () => {}
	}
]);

const variablesOfInterest = ref<string[]>([]);
const onToggleVariableOfInterest = (vals: string[]) => {
	variablesOfInterest.value = vals;
	requestParameters.value.forEach((d) => {
		if (variablesOfInterest.value.includes(d.name)) {
			d.label = 'all';
		} else {
			d.label = 'any';
		}
	});

	const state = _.cloneDeep(props.node.state);
	state.requestParameters = _.cloneDeep(requestParameters.value);
	emit('update-state', state);
};

const runMakeQuery = async () => {
	if (!model.value) {
		toast.error('', 'No Model provided for request');
		return;
	}

	const request: FunmanPostQueriesRequest = {
		model: model.value,
		request: {
			constraints: requestConstraints.value,
			parameters: requestParameters.value,
			structure_parameters: [
				{
					name: 'schedules',
					schedules: [
						{
							timepoints: requestStepList.value
						}
					]
				}
			],
			config: {
				use_compartmental_constraints: knobs.value.useCompartmentalConstraint,
				normalization_constant: 1,
				tolerance: knobs.value.tolerance
			}
		}
	};

	// Calculate the normalization mass of the model = Sum(initials)
	const semantics = model.value.semantics;
	if (knobs.value.useCompartmentalConstraint && semantics) {
		if (request.request.config) {
			request.request.config.normalization_constant = parseFloat(mass.value);
		}
	}
	const response = await makeQueries(request);

	// Setup the in-progress id
	const state = _.cloneDeep(props.node.state);
	state.inProgressId = response.id;
	emit('update-state', state);
};

const addConstraintForm = () => {
	const state = _.cloneDeep(props.node.state);
	const newGroup: ConstraintGroup = {
		borderColour: '#00c387',
		name: '',
		timepoints: { lb: 0, ub: 100 },
		variables: [],
		constraintType: '',
		derivativeType: ''
	};
	state.constraintGroups.push(newGroup);
	emit('update-state', state);
};

const deleteConstraintGroupForm = (data) => {
	const state = _.cloneDeep(props.node.state);
	state.constraintGroups.splice(data.index, 1);
	emit('update-state', state);
};

const updateConstraintGroupForm = (data) => {
	const state = _.cloneDeep(props.node.state);

	state.constraintGroups[data.index] = data.updatedConfig;
	emit('update-state', state);
};

// Used to set requestStepList.
// Grab startTime, endTime, numberOfSteps and create list.
function getStepList() {
	const start = knobs.value.currentTimespan.start;
	const end = knobs.value.currentTimespan.end;
	const steps = knobs.value.numberOfSteps;

	const aList = [start];
	const stepSize = floor((end - start) / steps);
	for (let i = 1; i < steps; i++) {
		aList[i] = i * stepSize;
	}
	aList.push(end);
	return aList;
}

const initialize = async () => {
	const modelConfigurationId = props.node.inputs[0].value?.[0];
	if (!modelConfigurationId) return;
	modelConfiguration.value = await getModelConfigurationById(modelConfigurationId);
	model.value = await getAsConfiguredModel(modelConfiguration.value);
};

const setModelOptions = async () => {
	if (!model.value) return;

	const renameReserved = (v: string) => {
		const reserved = ['lambda'];
		if (reserved.includes(v)) return `${v}_`;
		return v;
	};

	// Calculate mass
	const semantics = model.value.semantics;
	const modelInitials = semantics?.ode.initials;
	const modelMassExpression = modelInitials?.map((d) => renameReserved(d.expression)).join(' + ');

	const parametersMap = {};
	semantics?.ode.parameters?.forEach((d) => {
		// FIXME: may need to sample distributions if value is not available
		parametersMap[renameReserved(d.id)] = d.value || 0;
	});

	const massValue = await pythonInstance.evaluateExpression(modelMassExpression as string, parametersMap);
	mass.value = massValue;

	if (model.value.model.states) {
		modelStates.value = model.value.model.states.map((s) => s.id);
	}

	if (model.value.semantics?.ode.parameters) {
		modelParameters.value = model.value.semantics?.ode.parameters.map((d) => d.id);
	}

	// FIXME
	// observables are not currently supported
	// if (modelConfiguration.value.configuration.semantics?.ode?.observables) {
	// 	modelConfiguration.value.configuration.semantics.ode.observables.forEach((o) => {
	// 		modelColumnNameOptions.push(o.id);
	// 	});
	// }
	// modelStates.value = modelColumnNameOptions;

	const state = _.cloneDeep(props.node.state);
	knobs.value.numberOfSteps = state.numSteps;
	knobs.value.currentTimespan = _.cloneDeep(state.currentTimespan);
	knobs.value.tolerance = state.tolerance;
	knobs.value.useCompartmentalConstraint = state.useCompartmentalConstraint;

	if (model.value.semantics?.ode.parameters) {
		setRequestParameters(model.value.semantics?.ode.parameters);

		variablesOfInterest.value = requestParameters.value.filter((d: any) => d.label === 'all').map((d: any) => d.name);
	} else {
		toast.error('', 'Provided model has no parameters');
	}

	state.requestParameters = _.cloneDeep(requestParameters.value);
	emit('update-state', state);
};

// eslint-disable-next-line
const setRequestParameters = (modelParameters: ModelParameter[]) => {
	const previous = props.node.state.requestParameters;
	const labelMap = new Map<string, string>();
	previous.forEach((p) => {
		labelMap.set(p.name, p.label);
	});

	requestParameters.value = modelParameters.map((ele) => {
		let interval = { lb: ele.value, ub: ele.value };
		if (ele.distribution) {
			interval = {
				lb: ele.distribution.parameters.minimum,
				ub: ele.distribution.parameters.maximum
			};
		}

		const param = { name: ele.id, interval, label: 'any' };
		if (labelMap.has(param.name)) {
			param.label = labelMap.get(param.name) as string;
		}
		return param;
	});
};

const updateTrajectorystate = (s: string) => {
	const state = _.cloneDeep(props.node.state);
	state.trajectoryState = s;
	emit('update-state', state);
};

const onSelection = (id: string) => {
	emit('select-output', id);
};

watch(
	() => props.node.state.inProgressId,
	(id) => {
		if (!id || id === '') {
			showSpinner.value = false;
		} else {
			showSpinner.value = true;
		}
	},
	{ immediate: true }
);

/* Check for simple parameter changes */
watch(
	() => knobs.value,
	() => {
		const state = _.cloneDeep(props.node.state);
		state.tolerance = knobs.value.tolerance;
		state.currentTimespan.start = knobs.value.currentTimespan.start;
		state.currentTimespan.end = knobs.value.currentTimespan.end;
		state.numSteps = knobs.value.numberOfSteps;
		state.useCompartmentalConstraint = knobs.value.useCompartmentalConstraint;

		emit('update-state', state);
	},
	{ deep: true }
);

watch(
	() => props.node.inputs[0],
	async () => {
		// Set model, modelConfiguration, modelStates
		await initialize();
		setModelOptions();
	},
	{ immediate: true }
);

watch(
	() => props.node.active,
	() => {
		// Update selected output
		if (props.node.active) {
			activeOutput.value = props.node.outputs.find((d) => d.id === props.node.active) as any;
			selectedOutputId.value = props.node.active;
			setModelOptions();
		}
	},
	{ immediate: true }
);
</script>

<style scoped>
.primary-text {
	display: flex;
	align-items: center;
	color: var(--Text-Primary, #020203);
	/* Body Medium/Semibold */
	font-size: var(--font-body-medium);
	font-style: normal;
	font-weight: 600;
	line-height: 1.5rem;
	padding: 0.25rem 0rem 0rem 0rem;
	/* 150% */
	letter-spacing: 0.03125rem;
}

.secondary-text {
	color: var(--Text-Secondary, #667085);
	/* Body Small/Regular */
	font-size: var(--font-body-small);
	font-style: normal;
	font-weight: 400;
	line-height: 1.3125rem;
	/* 150% */
	letter-spacing: 0.01563rem;
}

.section-row {
	display: flex;
	padding: 0.5rem 0rem;
	align-items: center;
	gap: 0.8125rem;
	align-self: stretch;
}

.input-tolerance {
	display: flex;
	padding: var(--gap-small) 0 var(--gap-small) 0;
	width: 100%;
	align-items: center;
	gap: 0.8125rem;
	align-self: stretch;
	gap: 1.5rem;
}

div.section-row.timespan > div > span {
	width: 100%;
}

.timespan-list {
	width: 100%;
}

.spacer {
	margin-top: 1rem;
	margin-bottom: 1rem;
}

.add-constraint-spacer {
	margin-top: 0.5rem;
	margin-bottom: 2rem;
}

.green-text {
	color: var(--Primary, #1b8073);
}

.green-text:hover {
	color: var(--text-color-subdued);
}

.run-button {
	margin-right: auto;
}

.wizard-section {
	background-color: var(--surface-100);
}

/** Override default styles */
.accordion-component:deep(.p-accordion-header-link) {
	background-color: var(--surface-100);
}

.accordion-component:deep(.p-accordion-content) {
	background-color: var(--surface-100);
}
</style>
