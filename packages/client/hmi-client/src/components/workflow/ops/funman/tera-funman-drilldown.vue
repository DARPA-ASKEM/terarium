<template>
	<tera-drilldown
		:node="node"
		@update:selection="onSelection"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
	>
		<div :tabName="DrilldownTabs.Wizard">
			<tera-slider-panel
				class="input-config"
				v-model:is-open="isSliderOpen"
				header="Validate configuration settings"
				content-width="515px"
			>
				<template #content>
					<div class="top-toolbar">
						<p>Set your model checks and settings then click run.</p>
						<div class="btn-group">
							<Button label="Reset" outlined severity="secondary" />
							<Button :loading="showSpinner" label="Run" icon="pi pi-play" @click="runMakeQuery" />
						</div>
					</div>
					<main>
						<Accordion multiple :active-index="[0, 1]">
							<AccordionTab>
								<template #header>
									Model checks
									<i class="pi pi-info-circle pl-2" v-tooltip="validateParametersToolTip" />
								</template>
								<p class="mb-3">
									Implement sanity checks on the state space of the model to see how the parameter space of the model is
									partitioned into satisfiable and unsatisfiable regions separated by decision boundaries.
								</p>
								<ul>
									<li>
										<section>
											<header class="flex w-full gap-3 mb-2">
												<tera-toggleable-input v-model="knobs.compartmentalConstraint.name" tag="h3" />
												<div class="ml-auto flex align-items-center">
													<label class="mr-2">Active</label>
													<InputSwitch class="mr-3" v-model="knobs.compartmentalConstraint.isActive" />
												</div>
											</header>
											<div class="flex align-items-center gap-6">
												<katex-element
													:expression="
														stringToLatexExpression(
															stateIds
																.map((s, index) => `${s}${index === stateIds.length - 1 ? `\\geq 0` : ','}`)
																.join('')
														)
													"
												/>
												<katex-element
													:expression="
														stringToLatexExpression(`${stateIds.join('+')} = ${displayNumber(mass)} \\ \\forall \\ t`)
													"
												/>
											</div>
										</section>
									</li>
									<li v-for="(cfg, index) in node.state.constraintGroups" :key="selectedOutputId + ':' + index">
										<tera-constraint-group-form
											:config="cfg"
											:index="index"
											:state-ids="stateIds"
											:parameter-ids="parameterIds"
											:observable-ids="observableIds"
											@update-self="updateConstraintGroupForm(index, $event.key, $event.value)"
											@delete-self="deleteConstraintGroupForm(index)"
										/>
									</li>
								</ul>
								<Button
									class="mt-2"
									text
									icon="pi pi-plus"
									label="Add new check"
									size="small"
									@click="addConstraintForm"
								/>
							</AccordionTab>
							<AccordionTab>
								<template #header>
									Settings
									<i class="pi pi-info-circle pl-2" v-tooltip="validateParametersToolTip" />
								</template>
								<label>Select parameters of interest</label>
								<MultiSelect
									ref="columnSelect"
									:modelValue="variablesOfInterest"
									:options="requestParameters.map((d: any) => d.name)"
									:show-toggle-all="false"
									class="w-full mt-1 mb-2"
									@update:modelValue="onToggleVariableOfInterest"
									:maxSelectedLabels="1"
									placeholder="Select variables"
								/>
								<div class="mb-2 timespan">
									<div class="timespan-input">
										<label>Start time</label>
										<tera-input-number class="mt-1" v-model="knobs.currentTimespan.start" />
									</div>
									<div class="timespan-input">
										<label>End time</label>
										<tera-input-number class="mt-1" v-model="knobs.currentTimespan.end" />
									</div>
									<div class="timespan-input">
										<label>Number of timepoints</label>
										<tera-input-number class="mt-1" v-model="knobs.numberOfSteps" />
									</div>
								</div>
								<tera-input-text :disabled="true" class="timespan-list mb-2" v-model="requestStepListString" />
								<div>
									<label>Tolerance</label>
									<div class="mt-1 input-tolerance fadein animation-ease-in-out animation-duration-350">
										<tera-input-number v-model="knobs.tolerance" />
										<Slider v-model="knobs.tolerance" :min="0" :max="1" :step="0.01" class="w-full mr-2" />
									</div>
								</div>
							</AccordionTab>
						</Accordion>
					</main>
				</template>
			</tera-slider-panel>
		</div>
		<div :tabName="DrilldownTabs.Notebook">
			<tera-drilldown-section>
				<main>
					<!-- TODO: notebook functionality -->
					<div class="mt-3 ml-4 mr-2">Notebook is under construction. Use the wizard for now.</div>
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
			>
				<tera-progress-spinner v-if="showSpinner" :font-size="2" is-centered style="height: 100%" />
				<template v-else>
					<tera-funman-output
						v-if="activeOutput"
						:fun-model-id="activeOutput.value?.[0]"
						:trajectoryState="node.state.trajectoryState"
						@update:trajectoryState="updateTrajectorystate"
					/>
					<tera-operator-placeholder v-else class="h-full" :node="node" />
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
import TeraSliderPanel from '@/components/widgets/tera-slider-panel.vue';

import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import TeraToggleableInput from '@/components/widgets/tera-toggleable-input.vue';
import InputSwitch from 'primevue/inputswitch';

import type { FunmanInterval, FunmanPostQueriesRequest, Model, ModelParameter, TimeSpan } from '@/types/Types';
import { makeQueries } from '@/services/models/funman-service';
import { WorkflowNode, WorkflowOutput } from '@/types/workflow';
import {
	getAsConfiguredModel,
	getModelConfigurationById,
	getModelIdFromModelConfigurationId
} from '@/services/model-configurations';
import { useToastService } from '@/services/toast';
import { pythonInstance } from '@/python/PyodideController';
import TeraFunmanOutput from '@/components/workflow/ops/funman/tera-funman-output.vue';
import TeraConstraintGroupForm from '@/components/workflow/ops/funman/tera-constraint-group-form.vue';
import { DrilldownTabs } from '@/types/common';
import { stringToLatexExpression } from '@/services/model';
import { displayNumber } from '@/utils/number';
import { FunmanOperationState, Constraint, ConstraintType, CompartmentalConstraint } from './funman-operation';

const props = defineProps<{
	node: WorkflowNode<FunmanOperationState>;
}>();

const emit = defineEmits(['append-output', 'select-output', 'update-state', 'close']);

interface BasicKnobs {
	tolerance: number;
	currentTimespan: TimeSpan;
	numberOfSteps: number;
	compartmentalConstraint: CompartmentalConstraint;
}

const knobs = ref<BasicKnobs>({
	tolerance: 0,
	currentTimespan: { start: 0, end: 0 },
	numberOfSteps: 0,
	compartmentalConstraint: { name: '', isActive: false }
});

const MAX = 99999999999;
const toast = useToastService();
const validateParametersToolTip =
	'Validate the configuration of the model using functional model analysis (FUNMAN). \n \n The parameter space regions defined by the model configuration are evaluated to satisfactory or unsatisfactory depending on whether they generate model outputs that are within a given set of time-dependent constraints';
let originalModelId = '';

const showSpinner = ref(false);
const isSliderOpen = ref(true);

const mass = ref('0');

const requestStepList = computed(() => getStepList());
const requestStepListString = computed(() => requestStepList.value.join()); // Just used to display. dont like this but need to be quick

const requestParameters = ref<any[]>([]);
const configuredModel = ref<Model | null>();

const stateIds = ref<string[]>([]);
const parameterIds = ref<string[]>([]);
const observableIds = ref<string[]>([]);

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
	if (!configuredModel.value) {
		toast.error('', 'No Model provided for request');
		return;
	}

	const constraints = props.node.state.constraintGroups
		?.map((constraintGroup) => {
			if (!constraintGroup.isActive) return null;
			const { name, constraintType, variables, timepoints } = constraintGroup;
			// Use inputted weights when linearly constrained otherwise use implicit weights
			const weights =
				constraintType === ConstraintType.LinearlyConstrained
					? constraintGroup.weights
					: Array<number>(variables.length).fill(1.0);

			// Increasing/descreasing (monotonicity)
			if (constraintType === ConstraintType.Increasing || constraintType === ConstraintType.Decreasing) {
				return {
					soft: true,
					name,
					timepoints,
					additive_bounds: { lb: 0.0, original_width: MAX },
					variables,
					weights:
						constraintGroup.constraintType === ConstraintType.Increasing
							? weights.map((d) => Math.abs(d))
							: weights.map((d) => -Math.abs(d)),
					derivative: true
				};
			}

			// Use bounds needed that are saved in the UI
			const interval: FunmanInterval = {};
			if (constraintType === ConstraintType.LessThan || constraintType === ConstraintType.LessThanOrEqualTo) {
				interval.ub = constraintGroup.interval.ub;
			} else if (
				constraintType === ConstraintType.GreaterThan ||
				constraintType === ConstraintType.GreaterThanOrEqualTo
			) {
				interval.lb = constraintGroup.interval.lb;
			} else if (constraintType === ConstraintType.LinearlyConstrained) {
				interval.lb = constraintGroup.interval.lb;
				interval.ub = constraintGroup.interval.ub;
			}

			if (constraintType === ConstraintType.LessThanOrEqualTo) {
				interval.closed_upper_bound = true;
			}

			timepoints.closed_upper_bound = true;

			return {
				name,
				variables,
				weights,
				additive_bounds: interval,
				timepoints
			};
		})
		.filter(Boolean); // Removes falsey values

	const request: FunmanPostQueriesRequest = {
		model: configuredModel.value,
		request: {
			constraints,
			parameters: requestParameters.value,
			structure_parameters: [
				{
					name: 'schedules',
					schedules: [{ timepoints: requestStepList.value }]
				}
			],
			config: {
				use_compartmental_constraints: knobs.value.compartmentalConstraint.isActive,
				normalization_constant:
					knobs.value.compartmentalConstraint.isActive && configuredModel.value.semantics ? parseFloat(mass.value) : 1,
				normalize: false,
				tolerance: knobs.value.tolerance
			}
		}
	};

	console.log(request);

	const response = await makeQueries(request, originalModelId);

	// Setup the in-progress id
	const state = _.cloneDeep(props.node.state);
	state.inProgressId = response.id;
	emit('update-state', state);
};

const addConstraintForm = () => {
	const state = _.cloneDeep(props.node.state);
	state.constraintGroups.push({
		name: `Constraint ${state.constraintGroups.length + 1}`,
		isActive: true,
		timepoints: { lb: 0, ub: 100 },
		interval: { lb: 0, ub: 100 },
		constraint: Constraint.State,
		variables: [],
		weights: [],
		constraintType: ConstraintType.LessThan
	});
	emit('update-state', state);
};

const deleteConstraintGroupForm = (index: number) => {
	const state = _.cloneDeep(props.node.state);
	state.constraintGroups.splice(index, 1);
	emit('update-state', state);
};

const updateConstraintGroupForm = (index: number, key: string, value: any) => {
	const state = _.cloneDeep(props.node.state);

	// Changing constraint resets settings
	if (key === 'constraint') {
		state.constraintGroups[index].variables = [];
		state.constraintGroups[index].weights = [];
		state.constraintGroups[index].interval = { lb: 0, ub: 100 };
	}

	// Update changes
	state.constraintGroups[index][key] = value;

	// Make sure weights makes sense
	const weightLength = state.constraintGroups[index].weights.length;
	const variableLength = state.constraintGroups[index].variables.length;
	if (weightLength !== variableLength) {
		state.constraintGroups[index].weights = Array<number>(variableLength).fill(1.0);
	}
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
	const modelConfiguration = await getModelConfigurationById(modelConfigurationId);
	configuredModel.value = await getAsConfiguredModel(modelConfiguration);
	originalModelId = await getModelIdFromModelConfigurationId(modelConfigurationId);
};

const setModelOptions = async () => {
	if (!configuredModel.value) return;

	const renameReserved = (v: string) => {
		const reserved = ['lambda'];
		if (reserved.includes(v)) return `${v}_`;
		return v;
	};

	// Calculate mass
	const semantics = configuredModel.value.semantics;
	const modelInitials = semantics?.ode.initials;
	const modelMassExpression = modelInitials?.map((d) => renameReserved(d.expression)).join(' + ');

	const parametersMap = {};
	semantics?.ode.parameters?.forEach((d) => {
		// FIXME: may need to sample distributions if value is not available
		parametersMap[renameReserved(d.id)] = d.value || 0;
	});

	mass.value = await pythonInstance.evaluateExpression(modelMassExpression as string, parametersMap);

	const ode = configuredModel.value.semantics?.ode;
	if (ode) {
		if (ode.initials) stateIds.value = ode.initials.map((s) => s.target);
		if (ode.parameters) parameterIds.value = ode.parameters.map((d) => d.id);
		if (ode.observables) observableIds.value = ode.observables.map((d) => d.id);
	}

	const state = _.cloneDeep(props.node.state);
	knobs.value.numberOfSteps = state.numSteps;
	knobs.value.currentTimespan = _.cloneDeep(state.currentTimespan);
	knobs.value.tolerance = state.tolerance;
	knobs.value.compartmentalConstraint = state.compartmentalConstraint;

	if (configuredModel.value.semantics?.ode.parameters) {
		setRequestParameters(configuredModel.value.semantics?.ode.parameters);
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
		state.compartmentalConstraint = knobs.value.compartmentalConstraint;

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
.top-toolbar {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: var(--gap-1) var(--gap);
}

.btn-group {
	display: flex;
	align-items: center;
	gap: var(--gap-small);
	margin-left: auto;
}

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

.timespan {
	display: flex;
	align-items: center;
	justify-content: space-between;

	& .timespan-input {
		display: flex;
		flex-direction: column;
	}
}

.input-tolerance {
	display: flex;
	width: 100%;
	align-items: center;
	align-self: stretch;
	gap: var(--gap-medium);
}

ul {
	list-style-type: none;
	display: flex;
	flex-direction: column;
	gap: var(--gap-2);

	& section {
		display: flex;
		padding: var(--gap-4);
		flex-direction: column;
		background: var(--gray-50);
		border: 1px solid var(--surface-border-light);
		border-radius: var(--border-radius);
	}
}

.timespan-list {
	width: 100%;
}

.p-accordion {
	padding: 0 var(--gap-2);
}

/* Override grid template so output expands when sidebar is closed */
.overlay-container:deep(section.scale main) {
	grid-template-columns: auto 1fr;
}
</style>
