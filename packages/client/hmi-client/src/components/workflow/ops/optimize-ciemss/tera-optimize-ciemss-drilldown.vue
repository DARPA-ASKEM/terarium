<template>
	<tera-drilldown
		:node="node"
		:menu-items="menuItems"
		@update:selection="onSelection"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
	>
		<section :tabName="OptimizeTabs.Wizard" class="ml-4 mr-2 pt-3">
			<tera-drilldown-section>
				<template #header-controls-left>
					The model configuration will be optimized with the following settings
				</template>
				<template #header-controls-right>
					<Button :disabled="isRunDisabled" label="Run" icon="pi pi-play" @click="runOptimize" />
					<tera-pyciemss-cancel-button class="mr-auto" :simulation-run-id="cancelRunId" />
				</template>
				<section class="form-section">
					<h5>Success criteria <i v-tooltip="criteriaTooltip" class="pi pi-info-circle" /></h5>
					<tera-optimize-criterion-group-form
						v-for="(cfg, index) in node.state.constraintGroups"
						:key="selectedOutputId + ':' + index"
						:index="index"
						:criterion="cfg"
						:model-state-and-obs-options="modelStateAndObsOptions"
						@update-self="(config) => updateCriterionGroupForm(index, config)"
						@delete-self="() => deleteCriterionGroupForm(index)"
					/>
					<Button
						icon="pi pi-plus"
						class="p-button-sm p-button-text w-max"
						label="Add new criterion"
						@click="addCriterionGroupForm"
					/>
				</section>
				<section class="form-section">
					<h5>
						Intervention policy
						<i v-tooltip="interventionPolicyToolTip" class="pi pi-info-circle" />
					</h5>
					<template v-for="(cfg, idx) in props.node.state.interventionPolicyGroups">
						<tera-static-intervention-policy-group
							v-if="cfg.intervention?.staticInterventions && cfg.intervention?.staticInterventions.length > 0"
							:key="idx"
							:config="cfg"
							@update-self="(config) => updateInterventionPolicyGroupForm(idx, config)"
						/>
					</template>
					<template v-for="(cfg, idx) in props.node.state.interventionPolicyGroups">
						<tera-dynamic-intervention-policy-group
							v-if="cfg.intervention?.dynamicInterventions && cfg.intervention?.dynamicInterventions.length > 0"
							:key="idx"
							:config="cfg"
							@update-self="(config) => updateInterventionPolicyGroupForm(idx, config)"
						/>
					</template>
				</section>
				<section class="form-section">
					<h5>
						Optimization settings
						<i v-tooltip="optimizeSettingsToolTip" class="pi pi-info-circle" />
					</h5>
					<div class="input-row">
						<div class="label-and-input">
							<label>Start time</label>
							<tera-input disabled type="number" model-value="0" />
						</div>
						<div class="label-and-input">
							<label>End time</label>
							<tera-input type="number" v-model="knobs.endTime" />
						</div>
					</div>
					<div>
						<Button
							v-if="!showAdditionalOptions"
							class="p-button-sm p-button-text"
							label="Show additional options"
							@click="toggleAdditionalOptions"
						/>
						<Button
							v-if="showAdditionalOptions"
							class="p-button-sm p-button-text"
							label="Hide additional options"
							@click="toggleAdditionalOptions"
						/>
					</div>
					<div v-if="showAdditionalOptions">
						<div class="input-row">
							<div class="label-and-input">
								<label>Number of samples to simulate model</label>
								<div>
									<tera-input type="number" v-model="knobs.numSamples" />
								</div>
							</div>
							<div class="label-and-input">
								<label>Solver method</label>
								<Dropdown
									class="p-inputtext-sm"
									disabled
									:options="['dopri5', 'euler']"
									v-model="knobs.solverMethod"
									placeholder="Select"
								/>
							</div>
						</div>
						<div class="input-row">
							<h3>Optimizer options</h3>
						</div>
						<div class="input-row">
							<div class="label-and-input">
								<label>Algorithm</label>
								<tera-input disabled model-value="basinhopping" />
							</div>
							<div class="label-and-input">
								<label>Minimizer method</label>
								<tera-input disabled model-value="COBYLA" />
							</div>
							<div class="label-and-input">
								<label>Maxiter</label>
								<tera-input v-model="knobs.maxiter" />
							</div>
							<div class="label-and-input">
								<label>Maxfeval</label>
								<tera-input v-model="knobs.maxfeval" />
							</div>
						</div>
					</div>
				</section>
				<section class="form-section">
					<h5>
						Output settings
						<i v-tooltip="outputSettingsToolTip" class="pi pi-info-circle" />
					</h5>

					<!--Summary-->
					<h5>Summary</h5>
					<tera-checkbox
						v-model="summaryCheckbox"
						inputId="generate-summary"
						label="Auto-generate operation summary"
						subtext="Automatically generates a brief summary of the inputs and outputs."
						disabled
					/>
					<Divider />

					<!--Success Criteria-->
					<h5>
						Success Criteria
						<i v-tooltip="outputSettingsToolTip" class="pi pi-info-circle" />
					</h5>
					<tera-checkbox
						v-model="successDisplayChartsCheckbox"
						inputId="success-criteria-display-charts"
						label="Display chart(s)"
						subtext="Turn this on to generate an interactive chart of the success criteria conditions."
						disabled
					/>
					<Divider />

					<!--Interventions-->
					<h5>
						Interventions
						<i v-tooltip="outputSettingsToolTip" class="pi pi-info-circle" />
					</h5>
					<MultiSelect
						v-model="knobs.selectedInterventionVariables"
						:options="_.keys(preProcessedInterventionsData)"
						placeholder="What do you want to see?"
						filter
					/>
					<tera-checkbox
						v-model="interventionsDisplayChartsCheckbox"
						inputId="interventions-display-charts"
						label="Display chart(s)"
						disabled
					/>
					<Divider />

					<!--Simulation plots-->
					<h5>
						Simulation plots
						<i v-tooltip="outputSettingsToolTip" class="pi pi-info-circle" />
					</h5>
					<MultiSelect
						v-model="knobs.selectedSimulationVariables"
						:options="simulationChartOptions"
						placeholder="What do you want to see?"
						filter
					/>
					<tera-checkbox
						v-model="simulationDisplayChartsCheckbox"
						inputId="sim-plots-display-charts"
						label="Display chart(s)"
						disabled
					/>
				</section>
			</tera-drilldown-section>
		</section>
		<section :tabName="OptimizeTabs.Notebook" class="ml-4 mr-2 pt-3">
			<p>Under construction. Use the wizard for now.</p>
		</section>
		<template #preview>
			<tera-drilldown-preview
				title="Simulation output"
				:options="outputs"
				v-model:output="selectedOutputId"
				@update:selection="onSelection"
				:is-loading="showSpinner"
				is-selectable
				:class="{ 'failed-run': optimizationResult.success === 'False' }"
			>
				<tera-operator-output-summary v-if="node.state.summaryId && !showSpinner" :summary-id="node.state.summaryId" />

				<!-- Optimize result.json display: -->
				<div v-if="optimizationResult && displayOptimizationResultMessage" class="result-message-grid">
					<span class="flex flex-row">
						<h6>Response</h6>
						<Button
							icon="pi pi-times"
							text
							rounded
							size="small"
							class="ml-auto p-button-text"
							@click="displayOptimizationResultMessage = !displayOptimizationResultMessage"
						/>
					</span>
					<div v-for="(value, key) in optimizationResult" :key="key" class="result-message-row">
						<div class="label">{{ key }}:</div>
						<div class="value">{{ formatJsonValue(value) }}</div>
					</div>
				</div>
				<SelectButton
					:model-value="outputViewSelection"
					@change="if ($event.value) outputViewSelection = $event.value;"
					:options="outputViewOptions"
					option-value="value"
				>
					<template #option="{ option }">
						<i :class="`${option.icon} p-button-icon-left`" />
						<span class="p-button-label">{{ option.value }}</span>
					</template>
				</SelectButton>
				<tera-notebook-error v-bind="node.state.optimizeErrorMessage" />
				<tera-notebook-error v-bind="node.state.simulateErrorMessage" />
				<template v-if="runResults[knobs.postForecastRunId] && runResults[knobs.preForecastRunId]">
					<section v-if="outputViewSelection === OutputView.Charts" ref="outputPanel">
						<Accordion multiple :active-index="[0, 1, 2]">
							<AccordionTab header="Success criteria">
								<ul>
									<li v-for="(constraint, i) in node.state.constraintGroups" :key="i">
										<h5>{{ constraint.name }}</h5>
										<vega-chart
											v-if="riskResults[knobs.postForecastRunId]"
											:visualization-spec="
												createOptimizeChart(
													riskResults[knobs.postForecastRunId],
													constraint.targetVariable,
													constraint.threshold,
													constraint.isMinimized
												)
											"
										/>
									</li>
								</ul>
							</AccordionTab>
							<AccordionTab header="Interventions">
								<ul>
									<li v-for="(_, key) of knobs.selectedInterventionVariables" :key="`intervention_${key}`">
										<vega-chart are-embed-actions-visible :visualization-spec="preparedInterventionsCharts[key]" />
									</li>
								</ul>
							</AccordionTab>
							<AccordionTab header="Simulation plots">
								<ul>
									<li v-for="(_, key) of knobs.selectedSimulationVariables" :key="`simulation_${key}`">
										<vega-chart are-embed-actions-visible :visualization-spec="preparedCharts[key]" />
									</li>
								</ul>
							</AccordionTab>
						</Accordion>
					</section>
					<div v-else-if="outputViewSelection === OutputView.Data">
						<tera-dataset-datatable
							v-if="simulationRawContent[knobs.postForecastRunId]"
							:rows="10"
							:raw-content="simulationRawContent[knobs.postForecastRunId]"
						/>
					</div>
				</template>
			</tera-drilldown-preview>
		</template>
		<template #footer>
			<tera-save-dataset-from-simulation
				:simulation-run-id="knobs.postForecastRunId"
				:showDialog="showSaveDataDialog"
				@dialog-hide="showSaveDataDialog = false"
			/>
		</template>
	</tera-drilldown>
	<Dialog v-model:visible="showModelModal" modal header="Save as new model configuration" class="save-dialog w-4">
		<div class="label-and-input">
			<label> Model config name</label>
			<tera-input v-model="modelConfigName" />
		</div>
		<div class="label-and-input">
			<label> Model config description</label>
			<tera-input v-model="modelConfigDesc" />
		</div>
		<Button
			:disabled="modelConfigName === ''"
			outlined
			label="Save as a new model configuration"
			@click="saveModelConfiguration"
		/>
	</Dialog>
</template>

<script setup lang="ts">
import _, { cloneDeep, Dictionary } from 'lodash';
import { computed, onMounted, ref, watch } from 'vue';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import teraInput from '@/components/widgets/tera-input.vue';
import SelectButton from 'primevue/selectbutton';
import Dialog from 'primevue/dialog';
import TeraDatasetDatatable from '@/components/dataset/tera-dataset-datatable.vue';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraSaveDatasetFromSimulation from '@/components/dataset/tera-save-dataset-from-simulation.vue';
import TeraPyciemssCancelButton from '@/components/pyciemss/tera-pyciemss-cancel-button.vue';
import TeraOperatorOutputSummary from '@/components/operator/tera-operator-output-summary.vue';
import {
	createModelConfiguration,
	getAsConfiguredModel,
	getModelConfigurationById
} from '@/services/model-configurations';
import {
	convertToCsvAsset,
	getRunResult,
	getRunResultCSV,
	makeOptimizeJobCiemss,
	parsePyCiemssMap
} from '@/services/models/simulation-service';
import {
	CsvAsset,
	Intervention,
	InterventionPolicy,
	ModelConfiguration,
	OptimizeInterventions,
	OptimizeQoi,
	OptimizeRequestCiemss
} from '@/types/Types';
import { logger } from '@/utils/logger';
import { drilldownChartSize, nodeMetadata } from '@/components/workflow/util';
import { WorkflowNode } from '@/types/workflow';

import TeraNotebookError from '@/components/drilldown/tera-notebook-error.vue';
import { useProjects } from '@/composables/project';
import { isSaveDatasetDisabled } from '@/components/dataset/utils';
import { getInterventionPolicyById } from '@/services/intervention-policy';
import TeraCheckbox from '@/components/widgets/tera-checkbox.vue';
import Divider from 'primevue/divider';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import { createOptimizeChart, createOptimizeForecastChart } from '@/services/charts';
import VegaChart from '@/components/widgets/VegaChart.vue';
import MultiSelect from 'primevue/multiselect';
import teraOptimizeCriterionGroupForm from './tera-optimize-criterion-group-form.vue';
import TeraStaticInterventionPolicyGroup from './tera-static-intervention-policy-group.vue';
import TeraDynamicInterventionPolicyGroup from './tera-dynamic-intervention-policy-group.vue';
import {
	blankInterventionPolicyGroup,
	Criterion,
	defaultCriterion,
	InterventionPolicyGroupForm,
	OptimizeCiemssOperationState,
	OptimizationInterventionObjective
} from './optimize-ciemss-operation';

const props = defineProps<{
	node: WorkflowNode<OptimizeCiemssOperationState>;
}>();

const emit = defineEmits(['update-state', 'close', 'select-output']);

enum OptimizeTabs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}

enum OutputView {
	Charts = 'Charts',
	Data = 'Data'
}

interface BasicKnobs {
	endTime: number;
	numSamples: number;
	solverMethod: string;
	maxiter: number;
	maxfeval: number;
	preForecastRunId: string;
	postForecastRunId: string;
	optimizationRunId: string;
	selectedInterventionVariables: string[];
	selectedSimulationVariables: string[];
}

const knobs = ref<BasicKnobs>({
	endTime: props.node.state.endTime ?? 1,
	numSamples: props.node.state.numSamples ?? 0,
	solverMethod: props.node.state.solverMethod ?? '', // Currently not used.
	maxiter: props.node.state.maxiter ?? 5,
	maxfeval: props.node.state.maxfeval ?? 25,
	preForecastRunId: props.node.state.preForecastRunId ?? '',
	postForecastRunId: props.node.state.postForecastRunId ?? '',
	optimizationRunId: props.node.state.optimizationRunId ?? '',
	selectedInterventionVariables: props.node.state.selectedInterventionVariables ?? [],
	selectedSimulationVariables: props.node.state.selectedSimulationVariables ?? []
});

const criteriaTooltip = 'TODO';
const interventionPolicyToolTip = 'TODO';
const optimizeSettingsToolTip = 'TODO';
const outputSettingsToolTip = 'TODO';

const summaryCheckbox = ref(true);

const successDisplayChartsCheckbox = ref(true);
const interventionsDisplayChartsCheckbox = ref(true);
const simulationDisplayChartsCheckbox = ref(true);

const modelConfigName = ref<string>('');
const modelConfigDesc = ref<string>('');
const showSaveDataDialog = ref<boolean>(false);

const outputPanel = ref(null);
const chartSize = computed(() => drilldownChartSize(outputPanel.value));
const inferredParameters = computed(() => props.node.inputs[1].value);
const cancelRunId = computed(() => props.node.state.inProgressPostForecastId || props.node.state.inProgressOptimizeId);

const isSaveDisabled = computed<boolean>(() =>
	isSaveDatasetDisabled(props.node.state.postForecastRunId, useProjects().activeProject.value?.id)
);

const activePolicyGroups = computed(() => props.node.state.interventionPolicyGroups.filter((ele) => ele.isActive));

const inactivePolicyGroups = computed(() => props.node.state.interventionPolicyGroups.filter((ele) => !ele.isActive));
let pyciemssMap: Record<string, string> = {};

const menuItems = computed(() => [
	{
		label: 'Save as a new model configuration',
		icon: 'pi pi-pencil',
		disabled: modelConfigName.value === '',
		command: () => {
			showModelModal.value = true;
		}
	},
	{
		label: 'Save as new dataset',
		icon: 'pi pi-pencil',
		disabled: isSaveDisabled,
		command: () => {
			showSaveDataDialog.value = true;
		}
	}
]);

const showSpinner = computed<boolean>(
	() => props.node.state.inProgressOptimizeId !== '' || props.node.state.inProgressPostForecastId !== ''
);

const showModelModal = ref(false);
const displayOptimizationResultMessage = ref(true);

const outputs = computed(() => {
	if (!_.isEmpty(props.node.outputs)) {
		return [
			{
				label: 'Select outputs to display in operator',
				items: props.node.outputs
			}
		];
	}
	return [];
});

const isRunDisabled = computed(
	() =>
		!props.node.state.constraintGroups?.at(0)?.targetVariable ||
		props.node.state.interventionPolicyGroups.length === 0 ||
		activePolicyGroups.value.length <= 0
);

const selectedOutputId = ref<string>();

const outputViewSelection = ref(OutputView.Charts);
const outputViewOptions = ref([
	{ value: OutputView.Charts, icon: 'pi pi-image' },
	{ value: OutputView.Data, icon: 'pi pi-list' }
]);
const runResults = ref<{ [runId: string]: any }>({});
const runResultsSummary = ref<{ [runId: string]: any }>({});
const riskResults = ref<{ [runId: string]: any }>({});
const simulationRawContent = ref<{ [runId: string]: CsvAsset | null }>({});
const optimizationResult = ref<any>('');

const modelParameterOptions = ref<string[]>([]);
const modelStateAndObsOptions = ref<string[]>([]);

const simulationChartOptions = computed(() => [...modelParameterOptions.value, ...modelStateAndObsOptions.value]);
const modelConfiguration = ref<ModelConfiguration>();

const showAdditionalOptions = ref(true);

const onSelection = (id: string) => {
	emit('select-output', id);
};

const updateInterventionPolicyGroupForm = (index: number, config: InterventionPolicyGroupForm) => {
	const state = _.cloneDeep(props.node.state);
	if (!state.interventionPolicyGroups) return;

	state.interventionPolicyGroups[index] = config;
	emit('update-state', state);
};

const addCriterionGroupForm = () => {
	const state = _.cloneDeep(props.node.state);
	if (!state.constraintGroups) return;

	state.constraintGroups.push(defaultCriterion);
	emit('update-state', state);
};

const deleteCriterionGroupForm = (index: number) => {
	const state = _.cloneDeep(props.node.state);
	if (!state.constraintGroups) return;

	state.constraintGroups.splice(index, 1);
	emit('update-state', state);
};

const updateCriterionGroupForm = (index: number, config: Criterion) => {
	const state = _.cloneDeep(props.node.state);
	if (!state.constraintGroups) return;

	state.constraintGroups[index] = config;
	emit('update-state', state);
};

const toggleAdditionalOptions = () => {
	showAdditionalOptions.value = !showAdditionalOptions.value;
};

const formatJsonValue = (value) => {
	if (typeof value === 'object' && value !== null) {
		return JSON.stringify(value);
	}
	return value;
};

const initialize = async () => {
	const modelConfigurationId = props.node.inputs[0].value?.[0];
	if (!modelConfigurationId) return;
	modelConfiguration.value = await getModelConfigurationById(modelConfigurationId);
	const model = await getAsConfiguredModel(modelConfiguration.value);

	const policyId = props.node.inputs[2]?.value?.[0];
	if (policyId) {
		getInterventionPolicyById(policyId).then((interventionPolicy) => setInterventionPolicyGroups(interventionPolicy));
	}

	modelParameterOptions.value = model?.semantics?.ode.parameters?.map((ele) => ele.id) ?? [];
	modelStateAndObsOptions.value = model?.model.states.map((state: any) => state.id);

	/** Until supported by pyciemss-service, do not show observables.
	if (model?.semantics?.ode.observables) {
		modelStateAndObsOptions.value.push(...model.semantics.ode.observables.map((observable: any) => observable.id));
	}
	*/
};

const setInterventionPolicyGroups = (interventionPolicy: InterventionPolicy) => {
	const state = _.cloneDeep(props.node.state);
	// If already set + not changed since set, do not reset.
	if (state.interventionPolicyId === interventionPolicy.id) {
		return;
	}
	state.interventionPolicyId = interventionPolicy.id ?? '';
	state.interventionPolicyGroups = []; // Reset prior to populating.
	if (interventionPolicy.interventions && interventionPolicy.interventions.length > 0) {
		interventionPolicy.interventions.forEach((intervention) => {
			const isNotActive = intervention.dynamicInterventions?.length > 0 || intervention.staticInterventions?.length > 1;
			const newIntervention = _.cloneDeep(blankInterventionPolicyGroup);
			newIntervention.intervention = intervention;
			newIntervention.isActive = !isNotActive;
			newIntervention.startTimeGuess = intervention.staticInterventions[0]?.timestep;
			newIntervention.initialGuessValue = intervention.staticInterventions[0]?.value;
			state.interventionPolicyGroups.push(newIntervention);
		});
	}
	emit('update-state', state);
};

const runOptimize = async () => {
	if (!modelConfiguration.value?.id) {
		logger.error('no model config id provided');
		return;
	}

	const paramNames: string[] = [];
	const paramValues: number[] = [];
	const startTime: number[] = [];
	const listBoundsInterventions: number[][] = [];
	const initialGuess: number[] = [];
	const objectiveFunctionOption: string[] = [];

	activePolicyGroups.value.forEach((ele) => {
		paramNames.push(ele.intervention.appliedTo);
		paramValues.push(ele.intervention.staticInterventions[0].value);
		startTime.push(ele.intervention.staticInterventions[0].timestep);
		objectiveFunctionOption.push(ele.objectiveFunctionOption);

		if (ele.optimizationType === OptimizationInterventionObjective.startTime) {
			initialGuess.push(ele.startTimeGuess);
			listBoundsInterventions.push([ele.startTime]);
			listBoundsInterventions.push([ele.endTime]);
		} else if (ele.optimizationType === OptimizationInterventionObjective.paramValue) {
			initialGuess.push(ele.initialGuessValue);
			listBoundsInterventions.push([ele.lowerBoundValue]);
			listBoundsInterventions.push([ele.upperBoundValue]);
		} else if (ele.optimizationType === OptimizationInterventionObjective.paramValueAndStartTime) {
			initialGuess.push(ele.startTimeGuess);
			initialGuess.push(ele.initialGuessValue);
			listBoundsInterventions.push([ele.lowerBoundValue]);
			listBoundsInterventions.push([ele.upperBoundValue]);
		} else {
			console.error(`invalid optimization type used:${ele.optimizationType}`);
		}
	});
	// At the moment we only accept one intervention type. Pyciemss, pyciemss-service and this will all need to be updated.
	// https://github.com/DARPA-ASKEM/terarium/issues/3909
	const interventionType = props.node.state.interventionPolicyGroups[0].optimizationType;

	// These are interventions to be optimized over.
	const optimizeInterventions: OptimizeInterventions = {
		interventionType,
		paramNames,
		startTime,
		paramValues,
		initialGuess,
		objectiveFunctionOption
	};

	// These are interventions to be considered but not optimized over.
	const fixedStaticParameterInterventions: Intervention[] = _.cloneDeep(
		inactivePolicyGroups.value.map((ele) => ele.intervention)
	);

	// TODO: https://github.com/DARPA-ASKEM/terarium/issues/3909
	// The method should be a list but pyciemss + pyciemss service is not yet ready for this.
	const qoi: OptimizeQoi = {
		contexts: props.node.state.constraintGroups.map((ele) => ele.targetVariable),
		method: props.node.state.constraintGroups[0].qoiMethod
	};

	const optimizePayload: OptimizeRequestCiemss = {
		userId: 'no_user_provided',
		engine: 'ciemss',
		modelConfigId: modelConfiguration.value.id,
		timespan: {
			start: 0,
			end: knobs.value.endTime
		},
		optimizeInterventions,
		fixedStaticParameterInterventions,
		qoi,
		riskBound: props.node.state.constraintGroups[0].threshold, // TODO: https://github.com/DARPA-ASKEM/terarium/issues/3909
		boundsInterventions: listBoundsInterventions,
		extra: {
			isMinimized: props.node.state.constraintGroups[0].isMinimized,
			numSamples: knobs.value.numSamples,
			maxiter: knobs.value.maxiter,
			maxfeval: knobs.value.maxfeval,
			alpha: props.node.state.constraintGroups[0].riskTolerance / 100, // riskTolerance to get alpha and divide by 100 to turn into a percent for pyciemss-service.
			solverMethod: knobs.value.solverMethod
		}
	};

	// InferredParameters is to link a calibration run to this optimize call.
	if (inferredParameters.value) {
		optimizePayload.extra.inferredParameters = inferredParameters.value[0];
	}

	const optResult = await makeOptimizeJobCiemss(optimizePayload, nodeMetadata(props.node));
	const state = _.cloneDeep(props.node.state);
	state.inProgressOptimizeId = optResult.simulationId;
	state.optimizationRunId = '';
	state.inProgressPostForecastId = '';
	emit('update-state', state);
};

const saveModelConfiguration = async () => {
	if (!modelConfiguration.value) return;

	if (!knobs.value.optimizationRunId) {
		logger.error('No optimization run to create model configuration from');
	}

	// TODO: use new interventions
	// const optRunId = knobs.value.optimizationRunId;
	// const interventions = await getOptimizedInterventions(optRunId);
	const configClone = cloneDeep(modelConfiguration.value);

	// setInterventions(configClone, interventions);
	configClone.name = modelConfigName.value;
	configClone.description = modelConfigDesc.value;
	const data = await createModelConfiguration(configClone);
	if (!data) {
		logger.error('Failed to create model configuration');
		return;
	}

	logger.success('Created model configuration');
	showModelModal.value = false;
};

const setOutputValues = async () => {
	const preForecastRunId = knobs.value.preForecastRunId;
	const postForecastRunId = knobs.value.postForecastRunId;

	riskResults.value[knobs.value.postForecastRunId] = await getRunResult(knobs.value.postForecastRunId, 'risk.json');

	const preResult = await getRunResultCSV(preForecastRunId, 'result.csv');
	const postResult = await getRunResultCSV(postForecastRunId, 'result.csv');
	pyciemssMap = parsePyCiemssMap(postResult[0]);

	// FIXME: only show the post optimize data for now...
	simulationRawContent.value[knobs.value.postForecastRunId] = convertToCsvAsset(postResult, Object.values(pyciemssMap));
	runResults.value[preForecastRunId] = preResult;
	runResults.value[postForecastRunId] = postResult;

	const preResultSummary = await getRunResultCSV(preForecastRunId, 'result_summary.csv');
	const postResultSummary = await getRunResultCSV(postForecastRunId, 'result_summary.csv');

	runResultsSummary.value[preForecastRunId] = preResultSummary;
	runResultsSummary.value[postForecastRunId] = postResultSummary;

	optimizationResult.value = await getRunResult(knobs.value.optimizationRunId, 'optimize_results.json');
};

const preProcessedInterventionsData = computed<Dictionary<{ name: string; value: number; time: number }[]>>(() => {
	const state = _.cloneDeep(props.node.state);

	// Combine before and after interventions
	const combinedInterventions = [
		...state.interventionPolicyGroups.flatMap((group) =>
			group.intervention.staticInterventions.map((intervention) => ({
				appliedTo: group.intervention.appliedTo,
				name: group.intervention.name,
				value: intervention.value,
				time: intervention.timestep
			}))
		),
		...(state.optimizedInterventionPolicy?.interventions.flatMap((intervention) =>
			intervention.staticInterventions.map((staticIntervention) => ({
				appliedTo: intervention.appliedTo,
				name: intervention.name,
				value: staticIntervention.value,
				time: staticIntervention.timestep
			}))
		) || [])
	];

	// Group by appliedTo and map to exclude 'appliedTo' from final objects
	const groupedAndMapped = _.mapValues(_.groupBy(combinedInterventions, 'appliedTo'), (interventions) =>
		interventions.map(({ name, value, time }) => ({
			name,
			value,
			time
		}))
	);

	return groupedAndMapped;
});

onMounted(async () => {
	initialize();
});

const preparedInterventionsCharts = computed(() => {
	const preForecastRunId = knobs.value.preForecastRunId;
	const postForecastRunId = knobs.value.postForecastRunId;
	if (!postForecastRunId || !preForecastRunId) return [];
	const preResult = runResults.value[preForecastRunId];
	const preResultSummary = runResultsSummary.value[preForecastRunId];
	const postResult = runResults.value[postForecastRunId];
	const postResultSummary = runResultsSummary.value[postForecastRunId];

	return knobs.value.selectedInterventionVariables.map((variable) =>
		createOptimizeForecastChart(
			preResult,
			preResultSummary,
			postResult,
			postResultSummary,
			preProcessedInterventionsData.value[variable],
			{
				width: chartSize.value.width,
				height: chartSize.value.height,
				variables: [pyciemssMap[variable]],
				statisticalVariables: [`${pyciemssMap[variable]}_mean`],
				legend: true,
				groupField: 'sample_id',
				timeField: 'timepoint_id',
				xAxisTitle: 'Time',
				yAxisTitle: variable,
				title: variable
			}
		)
	);
});

const preparedCharts = computed(() => {
	const preForecastRunId = knobs.value.preForecastRunId;
	const postForecastRunId = knobs.value.postForecastRunId;
	if (!postForecastRunId || !preForecastRunId) return [];
	const preResult = runResults.value[preForecastRunId];
	const preResultSummary = runResultsSummary.value[preForecastRunId];
	const postResult = runResults.value[postForecastRunId];
	const postResultSummary = runResultsSummary.value[postForecastRunId];

	return knobs.value.selectedSimulationVariables.map((variable) =>
		createOptimizeForecastChart(preResult, preResultSummary, postResult, postResultSummary, [], {
			width: chartSize.value.width,
			height: chartSize.value.height,
			variables: [pyciemssMap[variable]],
			statisticalVariables: [`${pyciemssMap[variable]}_mean`],
			legend: true,
			groupField: 'sample_id',
			timeField: 'timepoint_id',
			xAxisTitle: 'Time',
			yAxisTitle: variable,
			title: variable
		})
	);
});

watch(
	() => knobs.value,
	async () => {
		const state = _.cloneDeep(props.node.state);
		state.endTime = knobs.value.endTime;
		state.numSamples = knobs.value.numSamples;
		state.solverMethod = knobs.value.solverMethod;
		state.maxiter = knobs.value.maxiter;
		state.maxfeval = knobs.value.maxfeval;
		state.preForecastRunId = knobs.value.preForecastRunId;
		state.postForecastRunId = knobs.value.postForecastRunId;
		state.optimizationRunId = knobs.value.optimizationRunId;
		state.selectedInterventionVariables = knobs.value.selectedInterventionVariables;
		state.selectedSimulationVariables = knobs.value.selectedSimulationVariables;
		emit('update-state', state);
	},
	{ deep: true }
);

watch(
	() => props.node.active,
	() => {
		// Update selected output
		if (props.node.active) {
			selectedOutputId.value = props.node.active;
			initialize();
			if (props.node.state.postForecastRunId !== '' && props.node.state.preForecastRunId !== '') {
				// The run has finished
				knobs.value.optimizationRunId = props.node.state.optimizationRunId;
				knobs.value.postForecastRunId = props.node.state.postForecastRunId;
				knobs.value.preForecastRunId = props.node.state.preForecastRunId;
				setOutputValues();
			}
		}
	},
	{ immediate: true }
);
</script>

<style scoped>
.result-message-grid {
	display: flex;
	flex-direction: column;
	gap: var(--gap-0-5); /* Adjust the gap between rows as needed */
	font-size: var(--font-caption);
	background-color: var(--surface-glass);
	border: solid 1px var(--surface-border-light);
	border-radius: var(--border-radius);
	padding: var(--gap-small);
}

.result-message-row {
	display: flex;
	flex-direction: row;
	gap: var(--gap-small);
}

.label {
	font-weight: bold;
	width: 150px; /* Adjust the width of the label column as needed */
}
.value {
	flex-grow: 1;
}

.failed-run {
	border: 2px solid var(--error-color);
	border-radius: var(--border-radius-big);
	color: var(--error-color-text);
}

.form-section {
	background-color: var(--surface-50);
	border-radius: var(--border-radius-medium);
	box-shadow: 0px 0px 4px 0px rgba(0, 0, 0, 0.25) inset;
	display: flex;
	flex-direction: column;
	flex-grow: 1;
	gap: var(--gap-1);
	margin: 0 var(--gap) var(--gap) var(--gap);
	padding: var(--gap);
}

.label-and-input {
	display: flex;
	flex-direction: column;
	gap: var(--gap-2);
}

.input-row {
	width: 100%;
	display: flex;
	flex-direction: row;
	flex-wrap: wrap;
	align-items: center;
	gap: var(--gap-2);
	padding-top: var(--gap);
	& > * {
		flex: 1;
	}
}

.constraint-row {
	display: flex;
	flex-direction: row;
	flex-wrap: wrap;
	align-items: center;
	gap: var(--gap-2);

	& > *:first-child {
		flex: 2;
	}

	& > *:not(:first-child) {
		flex: 1;
	}
}

.center-label {
	align-content: center;
}
</style>
