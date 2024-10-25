<template>
	<tera-drilldown
		:node="node"
		@update:selection="onSelection"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
	>
		<tera-slider-panel
			:tabName="DrilldownTabs.Wizard"
			class="input-config"
			v-model:is-open="isSliderOpen"
			header="Validate configuration settings"
			content-width="420px"
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
								label="Add constraint"
								size="small"
								@click="addConstraintForm"
							/>
						</AccordionTab>
						<AccordionTab>
							<template #header>
								Settings
								<i class="pi pi-info-circle pl-2" v-tooltip="validateParametersToolTip" />
							</template>
							<section class="flex flex-column gap-2">
								<label>Select parameters of interest</label>
								<MultiSelect
									ref="columnSelect"
									class="w-full"
									:model-value="variablesOfInterest"
									:options="requestParameters"
									option-label="name"
									option-disabled="disabled"
									:show-toggle-all="false"
									placeholder="Select variables"
									@update:model-value="onToggleVariableOfInterest"
								/>
								<span class="timespan">
									<div>
										<label>Start time</label>
										<tera-input-number class="w-12" v-model="knobs.currentTimespan.start" />
									</div>
									<div>
										<label>End time</label>
										<tera-input-number class="w-12" v-model="knobs.currentTimespan.end" />
									</div>
									<div>
										<label>Number of timesteps</label>
										<tera-input-number class="w-12" v-model="knobs.numberOfSteps" />
									</div>
								</span>
								<label>Timepoints</label>
								<code>{{ stepList.join(', ') }}</code>
								<label>Tolerance</label>
								<div class="input-tolerance fadein animation-ease-in-out animation-duration-350">
									<tera-input-number v-model="knobs.tolerance" />
									<Slider v-model="knobs.tolerance" :min="0" :max="1" :step="0.01" class="w-full mr-2" />
								</div>
							</section>
						</AccordionTab>
					</Accordion>
				</main>
			</template>
		</tera-slider-panel>
		<tera-slider-panel
			:tabName="DrilldownTabs.Notebook"
			v-model:is-open="isSliderOpen"
			header="Raw output"
			content-width="420px"
		>
			<template #content>
				<v-ace-editor
					v-model:value="notebookText"
					lang="json"
					theme="chrome"
					style="flex-grow: 1; width: 100%; height: 100%"
					class="ace-editor"
					:options="{ showPrintMargin: false }"
				/>
			</template>
		</tera-slider-panel>
		<template #preview>
			<tera-drilldown-preview
				title="Validation results"
				v-model:output="selectedOutputId"
				@update:selection="onSelection"
				:options="outputs"
				is-selectable
			>
				<tera-progress-spinner v-if="showSpinner" :font-size="2" is-centered style="height: 100%">
					{{ props.node.state.currentProgress }}%
				</tera-progress-spinner>
				<template v-else-if="!isEmpty(node.state.runId)">
					<header class="flex align-items-start">
						<div>
							<h4>{{ validatedModelConfiguration?.name }}</h4>
							<span class="secondary-text">Output generated date</span>
						</div>
						<div class="btn-group">
							<Button label="Add to report" outlined severity="secondary" disabled />
							<Button label="Save for reuse" outlined severity="secondary" disabled />
						</div>
					</header>
					<Accordion multiple :active-index="[0, 1, 2, 3]">
						<AccordionTab header="Summary"> Summary text </AccordionTab>
						<AccordionTab>
							<template #header> State variables<i class="pi pi-info-circle" /> </template>
							<template v-if="stateCharts[0]">
								<vega-chart
									v-for="(stateChart, index) in selectedStateCharts"
									:key="index"
									:visualization-spec="stateChart"
									:are-embed-actions-visible="false"
									expandable
								/>
							</template>
							<span class="ml-4" v-else> No boxes were generated. </span>
							<span class="ml-4" v-if="isEmpty(selectedStateCharts)">
								To view state charts select some in the Output settings.
							</span>
						</AccordionTab>
						<AccordionTab>
							<template #header>Parameters<i class="pi pi-info-circle" /></template>
							<vega-chart
								:visualization-spec="parameterCharts"
								:are-embed-actions-visible="false"
								expandable
								@chart-click="onParameterChartClick"
							/>
						</AccordionTab>
						<AccordionTab header="Diagram">
							<tera-model-diagram v-if="model" :model="model" />
						</AccordionTab>
					</Accordion>
					<template v-if="model && validatedModelConfiguration && configuredMmt">
						<tera-initial-table
							:model="model"
							:model-configuration="validatedModelConfiguration"
							:model-configurations="[]"
							:mmt="configuredMmt"
							:mmt-params="mmtParams"
							:feature-config="{ isPreview: true }"
						/>
						<tera-parameter-table
							:model="model"
							:model-configuration="validatedModelConfiguration"
							:model-configurations="[]"
							:mmt="configuredMmt"
							:mmt-params="mmtParams"
							:feature-config="{ isPreview: true }"
						/>
						<Accordion :active-index="0" v-if="!isEmpty(calibratedConfigObservables)">
							<AccordionTab v-if="!isEmpty(calibratedConfigObservables)" header="Observables">
								<tera-observables
									class="pl-4"
									:model="model"
									:mmt="configuredMmt"
									:observables="calibratedConfigObservables"
									:feature-config="{ isPreview: true }"
								/>
							</AccordionTab>
						</Accordion>
					</template>
				</template>
				<tera-operator-placeholder v-else class="h-full" :node="node" />
			</tera-drilldown-preview>
		</template>
		<template #sidebar-right>
			<tera-slider-panel
				v-model:is-open="isOutputSettingsPanelOpen"
				direction="right"
				class="input-config"
				header="Output settings"
				content-width="360px"
			>
				<template #content>
					<div class="output-settings-panel">
						<h5>State variables</h5>
						<tera-chart-control
							class="w-full"
							:chart-config="{
								selectedRun: 'fixme',
								selectedVariable: selectedStateSettings.map((s) => s.selectedVariables[0])
							}"
							multi-select
							:show-remove-button="false"
							:variables="stateOptions"
							@configuration-change="updateSelectedStates"
						/>
						<tera-chart-settings-item
							v-for="settings of chartSettings.filter((setting) => setting.type === ChartSettingType.VARIABLE)"
							:key="settings.id"
							:settings="settings"
							@open="activeChartSettings = settings"
							@remove="removeChartSetting"
						/>
						<hr />
						<h5>Parameters</h5>
						<tera-chart-control
							class="w-full"
							:chart-config="{
								selectedRun: 'fixme',
								selectedVariable: selectedParameterSettings.map((s) => s.selectedVariables[0])
							}"
							multi-select
							:show-remove-button="false"
							:variables="parameterOptions"
							@configuration-change="updateSelectedParameters"
						/>
						<tera-chart-settings-item
							v-for="settings of chartSettings.filter(
								(setting) => setting.type === ChartSettingType.DISTRIBUTION_COMPARISON
							)"
							:key="settings.id"
							:settings="settings"
							@open="activeChartSettings = settings"
							@remove="removeChartSetting"
						/>
						<hr />
					</div>
					<div class="flex align-items-center gap-2 ml-4 mb-3">
						<Checkbox v-model="onlyShowLatestResults" binary @change="renderCharts" />
						<label>Only show furthest results</label>
					</div>
					<div class="flex align-items-center gap-2 ml-4 mb-4">
						<Checkbox v-model="focusOnModelChecks" binary @change="updateStateCharts" />
						<label>Focus on model checks</label>
					</div>
				</template>
			</tera-slider-panel>
		</template>
	</tera-drilldown>
</template>

<script setup lang="ts">
import { isEmpty, cloneDeep } from 'lodash';
import { computed, ref, watch } from 'vue';
import { logger } from '@/utils/logger';
import Button from 'primevue/button';
import TeraInputNumber from '@/components/widgets/tera-input-number.vue';
import Slider from 'primevue/slider';
import MultiSelect from 'primevue/multiselect';
import Checkbox from 'primevue/checkbox';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import InputSwitch from 'primevue/inputswitch';
import VegaChart from '@/components/widgets/VegaChart.vue';

import '@/ace-config';
import { VAceEditor } from 'vue3-ace-editor';

import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import TeraSliderPanel from '@/components/widgets/tera-slider-panel.vue';
import TeraObservables from '@/components/model/model-parts/tera-observables.vue';
import TeraInitialTable from '@/components/model/petrinet/tera-initial-table.vue';
import TeraParameterTable from '@/components/model/petrinet/tera-parameter-table.vue';
import TeraModelDiagram from '@/components/model/petrinet/tera-model-diagram.vue';

import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import TeraToggleableInput from '@/components/widgets/tera-toggleable-input.vue';

import TeraChartControl from '@/components/workflow/tera-chart-control.vue';
import TeraChartSettingsItem from '@/components/widgets/tera-chart-settings-item.vue';

import type {
	FunmanInterval,
	FunmanPostQueriesRequest,
	Model,
	ModelParameter,
	TimeSpan,
	ModelConfiguration,
	Observable
} from '@/types/Types';
import {
	type ProcessedFunmanResult,
	type FunmanConstraintsResponse,
	processFunman,
	makeQueries
} from '@/services/models/funman-service';
import { createFunmanStateChart, createFunmanParameterCharts } from '@/services/charts';
import { WorkflowNode, WorkflowOutput } from '@/types/workflow';
import { getRunResult } from '@/services/models/simulation-service';
import type { MiraModel, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import { emptyMiraModel, makeConfiguredMMT } from '@/model-representation/mira/mira';
import {
	getAsConfiguredModel,
	getModelConfigurationById,
	getModelIdFromModelConfigurationId
} from '@/services/model-configurations';
import { useToastService } from '@/services/toast';
import { pythonInstance } from '@/python/PyodideController';
import TeraConstraintGroupForm from '@/components/workflow/ops/funman/tera-constraint-group-form.vue';
import { DrilldownTabs, ChartSetting, ChartSettingType } from '@/types/common';
import { stringToLatexExpression, getModelByModelConfigurationId, getMMT } from '@/services/model';
import { displayNumber } from '@/utils/number';
import { removeChartSettingById, updateChartSettingsBySelectedVariables } from '@/services/chart-settings';
import { FunmanOperationState, Constraint, ConstraintType, CompartmentalConstraint } from './funman-operation';

const props = defineProps<{
	node: WorkflowNode<FunmanOperationState>;
}>();

const emit = defineEmits(['select-output', 'update-state', 'close']);

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

const requestParameters = ref<any[]>([]);
const configuredModel = ref<Model | null>();

const stateIds = ref<string[]>([]);
const parameterIds = ref<string[]>([]);
const observableIds = ref<string[]>([]);

const selectedOutputId = ref<string>();
const outputs = computed(() => {
	if (!isEmpty(props.node.outputs)) {
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

const variablesOfInterest = ref();
const onToggleVariableOfInterest = (event: any[]) => {
	variablesOfInterest.value = event;
	const namesOfInterest = event.map((d) => d.name);
	requestParameters.value.forEach((d) => {
		d.label = namesOfInterest.includes(d.name) ? 'all' : 'any';
	});
	const state = cloneDeep(props.node.state);
	state.requestParameters = cloneDeep(requestParameters.value);
	emit('update-state', state);
};

const stepList = computed(() => {
	const { start, end } = knobs.value.currentTimespan;
	const steps = knobs.value.numberOfSteps;

	const stepSize = (end - start) / steps;
	return [start, ...Array.from({ length: steps - 1 }, (_, i) => Number(((i + 1) * stepSize).toFixed(3))), end];
});

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
			parameters: requestParameters.value.map(({ disabled, ...rest }) => rest), // Remove the disabled property from the request (it's only used for UI)
			structure_parameters: [
				{
					name: 'schedules',
					schedules: [{ timepoints: stepList.value }]
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

	const response = await makeQueries(request, originalModelId);

	// Setup the in-progress id
	const state = cloneDeep(props.node.state);
	state.inProgressId = response.id;
	emit('update-state', state);
};

const addConstraintForm = () => {
	const state = cloneDeep(props.node.state);
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
	const state = cloneDeep(props.node.state);
	state.constraintGroups.splice(index, 1);
	emit('update-state', state);
};

const updateConstraintGroupForm = (index: number, key: string, value: any) => {
	const state = cloneDeep(props.node.state);

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

	const state = cloneDeep(props.node.state);
	knobs.value.numberOfSteps = state.numSteps;
	knobs.value.currentTimespan = cloneDeep(state.currentTimespan);
	knobs.value.tolerance = state.tolerance;
	knobs.value.compartmentalConstraint = state.compartmentalConstraint;

	if (configuredModel.value.semantics?.ode.parameters) {
		setRequestParameters(configuredModel.value.semantics?.ode.parameters);
		variablesOfInterest.value = requestParameters.value.filter((d: any) => d.label === 'all');
	} else {
		toast.error('', 'Provided model has no parameters');
	}

	state.requestParameters = cloneDeep(requestParameters.value);
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
		const name = ele.id;

		const param = {
			name,
			label: (labelMap.get(name) as string) ?? 'any',
			interval: { lb: ele.value, ub: ele.value },
			disabled: false
		};

		if (ele.distribution) {
			param.interval = {
				lb: ele.distribution.parameters.minimum,
				ub: ele.distribution.parameters.maximum
			};
		} else {
			param.disabled = true; // Disable if constant
		}

		return param;
	});
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
		const state = cloneDeep(props.node.state);
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

// Notebook
const notebookText = ref('');
/*
// Output panel/settings //
*/
let processedFunmanResult: ProcessedFunmanResult | null = null;
let constraintsResponse: FunmanConstraintsResponse[] = [];
let mmt: MiraModel = emptyMiraModel();
let funmanResult: any = {};

// Model configuration stuff
const model = ref<Model | null>(null);
const validatedModelConfiguration = ref<ModelConfiguration | null>(null);
const configuredMmt = ref<MiraModel | null>(null);
const mmtParams = ref<MiraTemplateParams>({});
const calibratedConfigObservables = ref<Observable[]>([]);

const stateCharts = ref<any>([{}]);
const selectedStateCharts = computed(() => {
	const selectedStateIds = selectedStateSettings.value.map((setting) => setting.selectedVariables[0]);
	return stateCharts.value.filter((chart) => selectedStateIds.includes(chart.id));
});
const parameterCharts = ref<any>({});

const stateOptions = ref<string[]>([]);
const parameterOptions = ref<string[]>([]);
const onlyShowLatestResults = ref(false);
const focusOnModelChecks = ref(false);

const isOutputSettingsPanelOpen = ref(false);
const activeChartSettings = ref<ChartSetting | null>(null);
const chartSettings = computed(() => props.node.state.chartSettings ?? []);
const selectedStateSettings = computed(() =>
	chartSettings.value.filter((setting) => setting.type === ChartSettingType.VARIABLE)
);
const selectedParameterSettings = computed(() =>
	chartSettings.value.filter((setting) => setting.type === ChartSettingType.DISTRIBUTION_COMPARISON)
);

let selectedBoxId: number = -1;

// Once a parameter tick is chosen, its corresponding line on the state chart will be highlighted
function onParameterChartClick(eventData: any) {
	// If a tick is clicked it will have a boxId, if the bar is clicked then we reset (show all lines)
	selectedBoxId = eventData.boxId ?? -1;
	updateStateCharts();
}

function updateStateCharts() {
	if (!processedFunmanResult) return;
	const trajectories = processedFunmanResult.trajectories;
	stateCharts.value = funmanResult.model.petrinet.model.states.map(({ id }) =>
		createFunmanStateChart(trajectories, constraintsResponse, id, focusOnModelChecks.value, selectedBoxId)
	);
}

function updateParameterCharts() {
	if (!processedFunmanResult) return;
	const selectedParameterIds = selectedParameterSettings.value.map((setting) => setting.selectedVariables[0]);
	const distributionParameters = funmanResult.request.parameters.filter(
		// TODO: This first conditional may change as funman will return constants soon
		(d: any) => d.interval.lb !== d.interval.ub && selectedParameterIds.includes(d.name)
	);
	if (processedFunmanResult.boxes) {
		parameterCharts.value = createFunmanParameterCharts(distributionParameters, processedFunmanResult.boxes);
	}
}

function updateSelectedStates(event) {
	emit('update-state', {
		...props.node.state,
		chartSettings: updateChartSettingsBySelectedVariables(
			chartSettings.value,
			ChartSettingType.VARIABLE,
			event.selectedVariable
		)
	});
}

function updateSelectedParameters(event) {
	emit('update-state', {
		...props.node.state,
		chartSettings: updateChartSettingsBySelectedVariables(
			chartSettings.value,
			ChartSettingType.DISTRIBUTION_COMPARISON,
			event.selectedVariable
		)
	});
	updateParameterCharts(); // Rerender when we remove/add charts since they are all contained in the same visualization
}

function removeChartSetting(chartId: string) {
	const chartType = chartSettings.value.find((setting) => setting.id === chartId)?.type;
	emit('update-state', {
		...props.node.state,
		chartSettings: removeChartSettingById(chartSettings.value, chartId)
	});
	if (chartType === ChartSettingType.DISTRIBUTION_COMPARISON) {
		updateParameterCharts();
	}
}

async function renderCharts() {
	processedFunmanResult = processFunman(funmanResult, onlyShowLatestResults.value);

	updateStateCharts();
	updateParameterCharts();

	// For displaying model/model configuration
	// Model will be the same on runId change, no need to fetch it again
	if (!model.value) {
		model.value = await getModelByModelConfigurationId(funmanResult.modelConfiguration.id);
		if (!model.value) {
			logger.error('Failed to fetch model');
			return;
		}
		const response = await getMMT(model.value);
		if (response) {
			mmt = response.mmt;
			mmtParams.value = response.template_params;
		}
	}

	configuredMmt.value = makeConfiguredMMT(mmt, funmanResult.modelConfiguration);
	calibratedConfigObservables.value = funmanResult.modelConfiguration.observableSemanticList.map(
		({ referenceId, states, expression }) => ({
			id: referenceId,
			name: referenceId,
			states,
			expression
		})
	);
}

watch(
	() => props.node.state.runId,
	async () => {
		if (!props.node.state.runId) return;
		const rawFunmanResult = await getRunResult(props.node.state.runId, 'validation.json');
		if (!rawFunmanResult) {
			logger.error('Failed to fetch funman result');
			return;
		}
		notebookText.value = rawFunmanResult;
		funmanResult = JSON.parse(rawFunmanResult);
		constraintsResponse = funmanResult.request.constraints;
		validatedModelConfiguration.value = funmanResult.modelConfiguration;

		stateOptions.value = funmanResult.model.petrinet.model.states.map(({ id }) => id);
		parameterOptions.value = funmanResult.request.parameters
			.filter((d: any) => d.interval.lb !== d.interval.ub)
			.map(({ name }) => name);

		// Initialize default output settings
		const state = cloneDeep(props.node.state);
		state.chartSettings = updateChartSettingsBySelectedVariables([], ChartSettingType.VARIABLE, stateOptions.value);
		state.chartSettings = updateChartSettingsBySelectedVariables(
			state.chartSettings,
			ChartSettingType.DISTRIBUTION_COMPARISON,
			parameterOptions.value
		);
		emit('update-state', state);

		renderCharts();
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

.pi-info-circle {
	margin-left: var(--gap-2);
}

.secondary-text {
	color: var(--text-color-subdued);
	font-size: var(--font-caption);
}

.notebook-section:deep(main) {
	gap: var(--gap-small);
	position: relative;
}

code {
	background-color: var(--gray-50);
	color: var(--text-color-subdued);
	border-radius: var(--border-radius);
	border: 1px solid var(--surface-border);
	padding: var(--gap-2);
	overflow-wrap: break-word;
	font-size: var(--font-caption);
	max-height: 10rem;
	overflow: auto;
}

.timespan {
	display: flex;
	align-items: end;
	gap: var(--gap-1);
	overflow: auto;

	& > div {
		display: flex;
		flex-direction: column;
		gap: var(--gap-2);
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

.p-accordion {
	padding: 0 var(--gap-2);
}

.output-settings-panel {
	padding: var(--gap-4);
	display: flex;
	flex-direction: column;
	gap: var(--gap-2);
	hr {
		border: 0;
		border-top: 1px solid var(--surface-border-alt);
		width: 100%;
	}
}

/* Override grid template so output expands when sidebar is closed */
.overlay-container:deep(section.scale main) {
	grid-template-columns: auto 1fr;
}
</style>
