<template>
	<tera-drilldown
		:node="node"
		@update:selection="onSelection"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
	>
		<!-- Wizard tab -->
		<section :tabName="DrilldownTabs.Wizard" class="wizard">
			<tera-slider-panel
				class="input-config"
				v-model:is-open="isSidebarOpen"
				header="Optimize intervention settings"
				content-width="420px"
			>
				<template #content>
					<div class="toolbar">
						<p>Click Run to start optimization.</p>
						<span class="flex gap-2">
							<Button
								label="Reset"
								outlined
								@click="resetState"
								severity="secondary"
								:disabled="_.isEmpty(node.outputs[0].value)"
							/>
							<tera-pyciemss-cancel-button class="mr-auto" :simulation-run-id="cancelRunId" />
							<Button :disabled="isRunDisabled" label="Run" icon="pi pi-play" @click="runOptimize" />
						</span>
					</div>

					<section class="form-section">
						<h5>Success criteria <i v-tooltip="criteriaTooltip" class="pi pi-info-circle info-circle" /></h5>
						<tera-optimize-criterion-group-form
							v-for="(cfg, index) in knobs.constraintGroups"
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
							<i v-tooltip="interventionPolicyToolTip" class="pi pi-info-circle info-circle" />
						</h5>
						<template v-for="(cfg, idx) in knobs.interventionPolicyGroups">
							<tera-static-intervention-policy-group
								v-if="cfg.intervention?.staticInterventions && cfg.intervention?.staticInterventions.length > 0"
								:key="cfg.id || '' + idx"
								:config="cfg"
								@update-self="(config) => updateInterventionPolicyGroupForm(idx, config)"
							/>
						</template>
						<section class="empty-state" v-if="knobs.interventionPolicyGroups.length === 0">
							<!-- TODO: This only works if the user clicks refresh !?!? -->
							<p class="mt-1">No intervention policies have been added.</p>
						</section>
						<template v-for="(cfg, idx) in knobs.interventionPolicyGroups">
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
							<i v-tooltip="optimizeSettingsToolTip" class="pi pi-info-circle info-circle" />
						</h5>
						<div class="input-row pt-1">
							<div class="label-and-input">
								<label>Start time</label>
								<tera-input-number disabled :model-value="0" />
							</div>
							<div class="label-and-input">
								<label>End time</label>
								<tera-input-number v-model="knobs.endTime" />
							</div>
						</div>
						<div class="input-row">
							<div class="label-and-input">
								<label>Preset (optional)</label>
								<Dropdown
									v-model="presetType"
									placeholder="Select an option"
									:options="[CiemssPresetTypes.Fast, CiemssPresetTypes.Normal]"
									@update:model-value="setPresetValues"
								/>
							</div>
						</div>
						<div>
							<Button
								v-if="!showAdditionalOptions"
								icon="pi pi-plus-circle"
								class="p-button-sm p-button-text"
								label="Show additional options"
								@click="toggleAdditionalOptions"
							/>
							<Button
								v-if="showAdditionalOptions"
								icon="pi pi-minus-circle"
								class="p-button-sm p-button-text"
								label="Hide additional options"
								@click="toggleAdditionalOptions"
							/>
						</div>
						<!-- Additional options -->
						<div v-if="showAdditionalOptions" class="additional-options">
							<div class="input-row">
								<div class="label-and-input">
									<label>Number of samples to simulate model</label>
									<div>
										<tera-input-number v-model="knobs.numSamples" />
									</div>
								</div>
								<div class="label-and-input">
									<label><br />Solver method</label>
									<Dropdown
										class="p-inputtext-sm"
										:options="[CiemssMethodOptions.dopri5, CiemssMethodOptions.euler]"
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
									<tera-input-text disabled model-value="basinhopping" />
								</div>
								<div class="label-and-input">
									<label>Minimizer method</label>
									<tera-input-text disabled model-value="COBYLA" />
								</div>
								<div class="label-and-input">
									<label>Maxiter</label>
									<tera-input-number v-model="knobs.maxiter" />
								</div>
								<div class="label-and-input">
									<label>Maxfeval</label>
									<tera-input-number v-model="knobs.maxfeval" />
								</div>
							</div>
						</div>
					</section>
					<section class="form-section">
						<h5 class="mb-1">
							Output settings
							<i v-tooltip="outputSettingsToolTip" class="pi pi-info-circle info-circle" />
						</h5>

						<!--Summary-->
						<tera-checkbox
							v-model="summaryCheckbox"
							inputId="generate-summary"
							label="Auto-generate operation summary"
							subtext="Generates a brief summary of the inputs and outputs."
							disabled
						/>
						<Divider />

						<!--Success Criteria-->
						<h5 class="mb-1">
							Success Criteria
							<i v-tooltip="outputSettingsToolTip" class="pi pi-info-circle info-circle" />
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
						<h5 class="mb-1">
							Interventions
							<i v-tooltip="outputSettingsToolTip" class="pi pi-info-circle info-circle" />
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
						<h5 class="mb-1">
							Simulation plots
							<i v-tooltip="outputSettingsToolTip" class="pi pi-info-circle info-circle" />
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
					<!-- This used to be in the footer -->
					<tera-save-dataset-from-simulation
						:simulation-run-id="knobs.postForecastRunId"
						:showDialog="showSaveDataDialog"
						@dialog-hide="showSaveDataDialog = false"
					/>
				</template>
			</tera-slider-panel>
		</section>

		<!-- Notebook tab -->
		<section :tabName="DrilldownTabs.Notebook" class="notebook-section">
			<p>Under construction. Use the wizard for now.</p>
			<div class="result-message-grid">
				<p class="mb-2">For debugging:</p>
				<div v-for="(value, key) in optimizeRequestPayload" :key="key" class="result-message-row">
					<div class="label">{{ key }}:</div>
					<div class="value">{{ formatJsonValue(value) }}</div>
				</div>
			</div>
		</section>

		<!-- Preview tab -->
		<template #preview>
			<tera-drilldown-section
				class="ml-3 mr-3"
				:class="{ 'failed-run': optimizationResult.success === 'False' ?? 'successful-run' }"
			>
				<template #header-controls-left v-if="optimizedInterventionPolicy?.name">
					<h5>{{ optimizedInterventionPolicy?.name }}</h5>
				</template>
				<template #header-controls-right>
					<Button
						label="Save for re-use"
						severity="secondary"
						outlined
						:disabled="!optimizedInterventionPolicy"
						@click="showSaveInterventionPolicy = true"
					/>
				</template>
				<tera-progress-spinner v-if="showSpinner" :font-size="2" is-centered style="height: 100%">
					<div v-if="node.state.inProgressOptimizeId !== ''">
						{{ props.node.state.currentProgress }}% of maximum iterations complete
					</div>
					<div v-else>Optimize complete. Running simulations</div>
				</tera-progress-spinner>
				<tera-operator-output-summary v-if="node.state.summaryId && !showSpinner" :summary-id="node.state.summaryId" />
				<!-- Optimize result.json display: -->
				<div
					v-if="optimizationResult && displayOptimizationResultMessage && !showSpinner"
					class="result-message-grid mt-2 mb-2"
				>
					<span class="flex flex-row">
						<p class="mt-2">For debugging</p>
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
					v-if="!showSpinner"
					:model-value="outputViewSelection"
					@change="if ($event.value) outputViewSelection = $event.value;"
					:options="outputViewOptions"
					option-value="value"
					class="select-button my-2"
				>
					<template #option="{ option }">
						<i :class="`${option.icon} p-button-icon-left`" />
						<span class="p-button-label">{{ option.value }}</span>
					</template>
				</SelectButton>
				<tera-notebook-error v-bind="node.state.optimizeErrorMessage" />
				<tera-notebook-error v-bind="node.state.simulateErrorMessage" />
				<template v-if="runResults[knobs.postForecastRunId] && runResults[knobs.preForecastRunId] && !showSpinner">
					<section v-if="outputViewSelection === OutputView.Charts" ref="outputPanel">
						<Accordion multiple :active-index="[0, 1, 2]">
							<AccordionTab header="Success criteria">
								<ul>
									<li v-for="(_constraint, key) in knobs.constraintGroups" :key="key">
										<vega-chart
											expandable
											are-embed-actions-visible
											:visualization-spec="preparedSuccessCriteriaCharts[key]"
										/>
									</li>
								</ul>
							</AccordionTab>
							<AccordionTab header="Interventions">
								<ul>
									<li v-for="(_, key) of knobs.selectedInterventionVariables" :key="key">
										<vega-chart
											expandable
											are-embed-actions-visible
											:visualization-spec="preparedForecastCharts.interventionCharts[key]"
										/>
									</li>
								</ul>
							</AccordionTab>
							<AccordionTab header="Simulation plots">
								<ul>
									<li v-for="(_, key) of knobs.selectedSimulationVariables" :key="key">
										<vega-chart
											expandable
											are-embed-actions-visible
											:visualization-spec="preparedForecastCharts.simulationCharts[key]"
										/>
									</li>
								</ul>
							</AccordionTab>
						</Accordion>
					</section>
					<div v-else-if="outputViewSelection === OutputView.Data">
						<tera-dataset-datatable
							v-if="simulationRawContent[knobs.postForecastRunId]"
							:rows="10"
							:raw-content="simulationRawContent[knobs.postForecastRunId] as CsvAsset"
						/>
					</div>
				</template>
			</tera-drilldown-section>
		</template>
	</tera-drilldown>
	<Dialog v-model:visible="showModelModal" modal header="Save as new model configuration" class="save-dialog w-4">
		<div class="label-and-input">
			<label> Model config name</label>
			<tera-input-text v-model="modelConfigName" />
		</div>
		<div class="label-and-input">
			<label> Model config description</label>
			<tera-input-text v-model="modelConfigDesc" />
		</div>
		<Button
			:disabled="modelConfigName === ''"
			outlined
			label="Save as a new model configuration"
			@click="saveModelConfiguration"
		/>
	</Dialog>
	<tera-save-simulation-modal
		:initial-name="optimizedInterventionPolicy?.name"
		:is-visible="showSaveInterventionPolicy"
		:assets="[
			{ id: optimizedInterventionPolicy?.id ?? '', type: AssetType.InterventionPolicy },
			{ id: datasetId, type: AssetType.Dataset }
		]"
		@close-modal="showSaveInterventionPolicy = false"
		@on-save="onSaveForReuse"
		:simulation-id="node.state.postForecastRunId"
	/>
</template>

<script setup lang="ts">
import _, { cloneDeep, Dictionary } from 'lodash';
import { computed, onMounted, ref, watch } from 'vue';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import SelectButton from 'primevue/selectbutton';
import Dialog from 'primevue/dialog';
import TeraSaveSimulationModal from '@/components/project/tera-save-simulation-modal.vue';
import TeraDatasetDatatable from '@/components/dataset/tera-dataset-datatable.vue';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraSaveDatasetFromSimulation from '@/components/dataset/tera-save-dataset-from-simulation.vue';
import TeraPyciemssCancelButton from '@/components/pyciemss/tera-pyciemss-cancel-button.vue';
import TeraOperatorOutputSummary from '@/components/operator/tera-operator-output-summary.vue';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import { getUnitsFromModelParts, getModelByModelConfigurationId } from '@/services/model';
import { createModelConfiguration, getModelConfigurationById } from '@/services/model-configurations';
import {
	convertToCsvAsset,
	getRunResult,
	getRunResultCSV,
	makeOptimizeJobCiemss,
	parsePyCiemssMap,
	getSimulation,
	CiemssMethodOptions
} from '@/services/models/simulation-service';
import {
	CsvAsset,
	Intervention,
	InterventionPolicy,
	ModelConfiguration,
	Model,
	OptimizeInterventions,
	OptimizeQoi,
	OptimizeRequestCiemss,
	AssetType
} from '@/types/Types';
import { logger } from '@/utils/logger';
import { drilldownChartSize, nodeMetadata } from '@/components/workflow/util';
import { WorkflowNode } from '@/types/workflow';
import TeraSliderPanel from '@/components/widgets/tera-slider-panel.vue';

import TeraNotebookError from '@/components/drilldown/tera-notebook-error.vue';
import { getInterventionPolicyById } from '@/services/intervention-policy';
import TeraCheckbox from '@/components/widgets/tera-checkbox.vue';
import Divider from 'primevue/divider';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import { createSuccessCriteriaChart, createForecastChart, createInterventionChartMarkers } from '@/services/charts';
import VegaChart from '@/components/widgets/VegaChart.vue';
import MultiSelect from 'primevue/multiselect';
import { mergeResults, renameFnGenerator } from '@/components/workflow/ops/calibrate-ciemss/calibrate-utils';
import TeraInputNumber from '@/components/widgets/tera-input-number.vue';
import { CiemssPresetTypes, DrilldownTabs } from '@/types/common';
import { useConfirm } from 'primevue/useconfirm';
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

const confirm = useConfirm();

const isSidebarOpen = ref(true);

const props = defineProps<{
	node: WorkflowNode<OptimizeCiemssOperationState>;
}>();

const emit = defineEmits(['update-state', 'close', 'select-output']);

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
	constraintGroups: Criterion[];
	interventionPolicyGroups: InterventionPolicyGroupForm[];
}

const knobs = ref<BasicKnobs>({
	endTime: props.node.state.endTime ?? 1,
	numSamples: props.node.state.numSamples ?? 0,
	solverMethod: props.node.state.solverMethod ?? CiemssMethodOptions.dopri5,
	maxiter: props.node.state.maxiter ?? 5,
	maxfeval: props.node.state.maxfeval ?? 25,
	preForecastRunId: props.node.state.preForecastRunId ?? '',
	postForecastRunId: props.node.state.postForecastRunId ?? '',
	optimizationRunId: props.node.state.optimizationRunId ?? '',
	selectedInterventionVariables: props.node.state.selectedInterventionVariables ?? [],
	selectedSimulationVariables: props.node.state.selectedSimulationVariables ?? [],
	constraintGroups: props.node.state.constraintGroups ?? [],
	interventionPolicyGroups: props.node.state.interventionPolicyGroups ?? []
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
const showSaveInterventionPolicy = ref<boolean>(false);

const outputPanel = ref(null);
const chartSize = computed(() => drilldownChartSize(outputPanel.value));
const cancelRunId = computed(() => props.node.state.inProgressPostForecastId || props.node.state.inProgressOptimizeId);

const activePolicyGroups = computed(() => knobs.value.interventionPolicyGroups.filter((ele) => ele.isActive));

const inactivePolicyGroups = computed(() => knobs.value.interventionPolicyGroups.filter((ele) => !ele.isActive));
let pyciemssMap: Record<string, string> = {};

const showSpinner = computed<boolean>(
	() => props.node.state.inProgressOptimizeId !== '' || props.node.state.inProgressPostForecastId !== ''
);

const datasetId = computed(() => {
	if (!selectedOutputId.value) return '';
	const output = props.node.outputs.find((o) => o.id === selectedOutputId.value);
	return output?.value?.[0]?.datasetId ?? '';
});

const showModelModal = ref(false);
const displayOptimizationResultMessage = ref(true);

const isRunDisabled = computed(() => {
	const activeConstraintGroups = knobs.value.constraintGroups.filter((ele) => ele.isActive);
	return (
		activeConstraintGroups.length === 0 ||
		!activeConstraintGroups.every((ele) => ele.targetVariable) ||
		knobs.value.interventionPolicyGroups.length === 0 ||
		activePolicyGroups.value.length <= 0
	);
});

const presetType = computed(() => {
	if (
		knobs.value.numSamples === speedValues.numSamplesToSimModel &&
		knobs.value.solverMethod === speedValues.method &&
		knobs.value.maxiter === speedValues.maxiter &&
		knobs.value.maxfeval === speedValues.maxfeval
	) {
		return CiemssPresetTypes.Fast;
	}
	if (
		knobs.value.numSamples === qualityValues.numSamplesToSimModel &&
		knobs.value.solverMethod === qualityValues.method &&
		knobs.value.maxiter === qualityValues.maxiter &&
		knobs.value.maxfeval === qualityValues.maxfeval
	) {
		return CiemssPresetTypes.Normal;
	}
	return '';
});

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
const optimizeRequestPayload = ref<any>('');
const optimizedInterventionPolicy = ref<InterventionPolicy | null>(null);

const model = ref<Model | null>(null);
const modelParameterOptions = computed<string[]>(() =>
	(model.value?.semantics?.ode?.parameters ?? []).map((p) => p.id)
);
const modelStateAndObsOptions = ref<{ label: string; value: string }[]>([]);

const simulationChartOptions = computed(() => [
	...modelParameterOptions.value,
	...modelStateAndObsOptions.value.map((ele) => ele.label)
]);
const modelConfiguration = ref<ModelConfiguration>();

const showAdditionalOptions = ref(true);

const getUnit = (paramId: string) => {
	if (!model.value) return '';
	return getUnitsFromModelParts(model.value)[paramId] || '';
};

const onSelection = (id: string) => {
	emit('select-output', id);
};

const updateInterventionPolicyGroupForm = (index: number, config: InterventionPolicyGroupForm) => {
	knobs.value.interventionPolicyGroups[index] = config;
};

const addCriterionGroupForm = () => {
	knobs.value.constraintGroups.push(defaultCriterion);
};

const deleteCriterionGroupForm = (index: number) => {
	knobs.value.constraintGroups.splice(index, 1);
};

const updateCriterionGroupForm = (index: number, config: Criterion) => {
	knobs.value.constraintGroups[index] = config;
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

const setPresetValues = (data: CiemssPresetTypes) => {
	if (data === CiemssPresetTypes.Normal) {
		knobs.value.numSamples = qualityValues.numSamplesToSimModel;
		knobs.value.solverMethod = qualityValues.method;
		knobs.value.maxiter = qualityValues.maxiter;
		knobs.value.maxfeval = qualityValues.maxfeval;
	}
	if (data === CiemssPresetTypes.Fast) {
		knobs.value.numSamples = speedValues.numSamplesToSimModel;
		knobs.value.solverMethod = speedValues.method;
		knobs.value.maxiter = speedValues.maxiter;
		knobs.value.maxfeval = speedValues.maxfeval;
	}
};

const speedValues = Object.freeze({
	numSamplesToSimModel: 1,
	method: CiemssMethodOptions.euler,
	maxiter: 0,
	maxfeval: 1
});

const qualityValues = Object.freeze({
	numSamplesToSimModel: 100,
	method: CiemssMethodOptions.dopri5,
	maxiter: 5,
	maxfeval: 25
});

const initialize = async () => {
	const modelConfigurationId = props.node.inputs[0].value?.[0];
	if (!modelConfigurationId) return;
	const results = await Promise.all([
		getModelConfigurationById(modelConfigurationId),
		getModelByModelConfigurationId(modelConfigurationId)
	]);
	modelConfiguration.value = results[0];
	model.value = results[1];

	const policyId = props.node.inputs[1]?.value?.[0];
	if (policyId) {
		// FIXME: This should be done in the node this should not be done in the drill down.
		getInterventionPolicyById(policyId).then((interventionPolicy) => setInterventionPolicyGroups(interventionPolicy));
	}

	const optimizedPolicyId = props.node.state.optimizedInterventionPolicyId;
	if (optimizedPolicyId) {
		optimizedInterventionPolicy.value = await getInterventionPolicyById(optimizedPolicyId);
	}
	const modelStates = model.value?.model.states;
	modelStateAndObsOptions.value = modelStates.map((state: any) => ({
		label: state.id,
		value: `${state.id}_state`
	}));

	// Add obs:
	const modelObs = model.value?.semantics?.ode.observables;
	if (modelObs) {
		modelStateAndObsOptions.value.push(
			...modelObs.map((observable: any) => ({
				label: observable.id,
				value: `${observable.id}_observable`
			}))
		);
	}
};

const setInterventionPolicyGroups = (interventionPolicy: InterventionPolicy) => {
	const state = _.cloneDeep(props.node.state);
	// If already set + not changed since set, do not reset.
	if (
		knobs.value.interventionPolicyGroups.length > 0 &&
		knobs.value.interventionPolicyGroups[0].id === interventionPolicy.id
	) {
		return;
	}
	state.interventionPolicyId = interventionPolicy.id ?? '';

	knobs.value.interventionPolicyGroups = []; // Reset prior to populating.
	if (interventionPolicy.interventions && interventionPolicy.interventions.length > 0) {
		interventionPolicy.interventions.forEach((intervention) => {
			const isNotActive = intervention.dynamicInterventions?.length > 0 || intervention.staticInterventions?.length > 1;
			const newIntervention = _.cloneDeep(blankInterventionPolicyGroup);
			newIntervention.id = interventionPolicy.id;
			newIntervention.intervention = intervention;
			newIntervention.isActive = !isNotActive;
			newIntervention.startTimeGuess = intervention.staticInterventions[0]?.timestep;
			newIntervention.initialGuessValue = intervention.staticInterventions[0]?.value;
			knobs.value.interventionPolicyGroups.push(newIntervention);
		});
	}
	emit('update-state', state);
};

const runOptimize = async () => {
	if (!modelConfiguration.value?.id) {
		logger.error('no model config id provided');
		return;
	}

	setOutputSettingDefaults();

	const paramNames: string[] = [];
	const paramValues: number[] = [];
	const startTime: number[] = [];
	const listBoundsInterventions: number[][] = [];
	const initialGuess: number[] = [];
	const objectiveFunctionOption: string[] = [];

	activePolicyGroups.value.forEach((ele) => {
		// Only allowed to optimize on interventions that arent grouped aka staticInterventions' length is 1
		paramNames.push(ele.intervention.staticInterventions[0].appliedTo);
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
	const interventionType = knobs.value.interventionPolicyGroups[0].optimizationType;

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
	const fixedInterventions: Intervention[] = _.cloneDeep(inactivePolicyGroups.value.map((ele) => ele.intervention));

	const qois: OptimizeQoi[] = [];
	const activeConstraintGroups = knobs.value.constraintGroups.filter((ele) => ele.isActive);
	activeConstraintGroups.forEach((constraintGroup) =>
		qois.push({
			contexts: [constraintGroup.targetVariable],
			method: constraintGroup.qoiMethod,
			riskBound: constraintGroup.threshold,
			isMinimized: constraintGroup.isMinimized
		})
	);

	// riskTolerance to get alpha and divide by 100 to turn into a percent for pyciemss-service.
	const alphas: number[] = activeConstraintGroups.map((ele) => ele.riskTolerance / 100);
	const optimizePayload: OptimizeRequestCiemss = {
		userId: 'no_user_provided',
		engine: 'ciemss',
		modelConfigId: modelConfiguration.value.id,
		timespan: {
			start: 0,
			end: knobs.value.endTime
		},
		optimizeInterventions,
		fixedInterventions,
		qoi: qois,
		boundsInterventions: listBoundsInterventions,
		extra: {
			numSamples: knobs.value.numSamples,
			maxiter: knobs.value.maxiter,
			maxfeval: knobs.value.maxfeval,
			alpha: alphas,
			solverMethod: knobs.value.solverMethod,
			solverStepSize: 1
		}
	};

	// InferredParameters is to link a calibration run to this optimize call.
	optimizePayload.extra.inferredParameters = modelConfiguration.value.simulationId;
	console.log(optimizePayload);
	const optResult = await makeOptimizeJobCiemss(optimizePayload, nodeMetadata(props.node));
	const state = _.cloneDeep(props.node.state);
	state.inProgressOptimizeId = optResult.simulationId;
	state.optimizationRunId = '';
	state.inProgressPostForecastId = '';
	emit('update-state', state);
};

const setOutputSettingDefaults = () => {
	const selectedInterventionVariables: Array<string> = [];
	const selectedSimulationVariables: Array<string> = [];

	if (!knobs.value.selectedInterventionVariables.length) {
		activePolicyGroups.value.forEach((ele) => {
			ele.intervention.staticInterventions.forEach((staticInt) =>
				selectedInterventionVariables.push(staticInt.appliedTo)
			);
			ele.intervention.dynamicInterventions.forEach((dynamicsInt) =>
				selectedInterventionVariables.push(dynamicsInt.appliedTo)
			);
		});
	}

	if (!knobs.value.selectedSimulationVariables.length) {
		knobs.value.constraintGroups.forEach((constraint) => {
			if (constraint.targetVariable) {
				// Use modelStateAndObsOptions to map from value -> label as simulation selection uses S not S_State
				const userSelection = modelStateAndObsOptions.value.find(
					(ele) => ele.value === constraint.targetVariable
				)?.label;
				if (userSelection) {
					selectedSimulationVariables.push(userSelection);
				}
			}
		});
		if (selectedSimulationVariables.length) {
			knobs.value.selectedSimulationVariables = [...new Set(selectedSimulationVariables)];
		}
	}
};

// TODO: utlize with https://github.com/DARPA-ASKEM/terarium/issues/4767
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

	const preResult = await getRunResultCSV(preForecastRunId, 'result.csv', renameFnGenerator('pre'));
	const postResult = await getRunResultCSV(postForecastRunId, 'result.csv');
	pyciemssMap = parsePyCiemssMap(postResult[0]);

	// FIXME: only show the post optimize data for now...
	simulationRawContent.value[knobs.value.postForecastRunId] = convertToCsvAsset(postResult, Object.values(pyciemssMap));
	runResults.value[preForecastRunId] = preResult;
	runResults.value[postForecastRunId] = postResult;

	const preResultSummary = await getRunResultCSV(preForecastRunId, 'result_summary.csv', renameFnGenerator('pre'));
	const postResultSummary = await getRunResultCSV(postForecastRunId, 'result_summary.csv');

	runResultsSummary.value[preForecastRunId] = preResultSummary;
	runResultsSummary.value[postForecastRunId] = postResultSummary;

	optimizationResult.value = await getRunResult(knobs.value.optimizationRunId, 'optimize_results.json');
	optimizeRequestPayload.value = (await getSimulation(knobs.value.optimizationRunId))?.executionPayload || '';
};

const preProcessedInterventionsData = computed<Dictionary<Intervention[]>>(() => {
	// Combine before and after interventions
	const combinedInterventions = [
		...knobs.value.interventionPolicyGroups.flatMap((group) => group.intervention),
		...(optimizedInterventionPolicy.value?.interventions || [])
	];

	// Group by appliedTo
	return _.groupBy(combinedInterventions, 'appliedTo');
});

onMounted(async () => {
	initialize();
});

const preparedSuccessCriteriaCharts = computed(() => {
	const postForecastRunId = props.node.state.postForecastRunId;

	return knobs.value.constraintGroups
		.filter((ele) => ele.isActive)
		.map((constraint) =>
			createSuccessCriteriaChart(
				riskResults.value[postForecastRunId],
				constraint.targetVariable,
				constraint.threshold,
				constraint.isMinimized,
				constraint.riskTolerance,
				{
					title: constraint.name,
					width: chartSize.value.width,
					height: chartSize.value.height,
					xAxisTitle: 'Number of samples',
					yAxisTitle: `${constraint.isMinimized ? 'Max' : 'Min'} value of ${constraint.targetVariable} at all timepoints`,
					legend: true
				}
			)
		);
});

// Creates forecast charts for interventions and simulation charts, based on the selected variables
const preparedForecastCharts = computed(() => {
	const charts: { interventionCharts: any[]; simulationCharts: any[] } = {
		interventionCharts: [],
		simulationCharts: []
	};
	const preForecastRunId = knobs.value.preForecastRunId;
	const postForecastRunId = knobs.value.postForecastRunId;
	if (!postForecastRunId || !preForecastRunId) return charts;
	const preResult = runResults.value[preForecastRunId];
	const preResultSummary = runResultsSummary.value[preForecastRunId];
	const postResult = runResults.value[postForecastRunId];
	const postResultSummary = runResultsSummary.value[postForecastRunId];

	if (!postResult || !postResultSummary || !preResultSummary || !preResult) return charts;

	// Merge before/after for chart
	const { result, resultSummary } = mergeResults(postResult, preResult, postResultSummary, preResultSummary);

	const chartOptions = {
		width: chartSize.value.width,
		height: chartSize.value.height,
		legend: true,
		xAxisTitle: getUnit('_time') || 'Time',
		yAxisTitle: getUnit('') || '',
		title: '',
		colorscheme: ['#AAB3C6', '#1B8073'],
		translationMap: {}
	};

	const translationMap = (variable: string) => ({
		[`${pyciemssMap[variable]}_mean:pre`]: `${variable} before optimization`,
		[`${pyciemssMap[variable]}_mean`]: `${variable} after optimization`
	});

	// intervention chart spec
	charts.interventionCharts = knobs.value.selectedInterventionVariables.map((variable) => {
		const options = _.cloneDeep(chartOptions);
		options.translationMap = translationMap(variable);
		options.yAxisTitle = getUnit(variable);

		const forecastChart = createForecastChart(
			{
				data: result,
				variables: [`${pyciemssMap[variable]}:pre`, pyciemssMap[variable]],
				timeField: 'timepoint_id',
				groupField: 'sample_id'
			},
			{
				data: resultSummary,
				variables: [`${pyciemssMap[variable]}_mean:pre`, `${pyciemssMap[variable]}_mean`],
				timeField: 'timepoint_id'
			},
			null,
			options
		);
		// add intervention annotations (rules and text)
		forecastChart.layer.push(...createInterventionChartMarkers(preProcessedInterventionsData.value[variable]));
		return forecastChart;
	});

	// simulation chart spec
	charts.simulationCharts = knobs.value.selectedSimulationVariables.map((variable) => {
		const options = _.cloneDeep(chartOptions);
		options.translationMap = translationMap(variable);
		options.yAxisTitle = getUnit(variable);

		return createForecastChart(
			{
				data: result,
				variables: [`${pyciemssMap[variable]}:pre`, pyciemssMap[variable]],
				timeField: 'timepoint_id',
				groupField: 'sample_id'
			},
			{
				data: resultSummary,
				variables: [`${pyciemssMap[variable]}_mean:pre`, `${pyciemssMap[variable]}_mean`],
				timeField: 'timepoint_id'
			},
			null,
			options
		);
	});
	return charts;
});

// refresh policy
const onSaveForReuse = async () => {
	const policyId = props.node.state.optimizedInterventionPolicyId;
	if (policyId) {
		optimizedInterventionPolicy.value = await getInterventionPolicyById(policyId);
	}
};

// reset drilldown state
const resetState = () => {
	confirm.require({
		header: 'Reset to original optimized state',
		message: 'Are you sure you want to reset the state?',
		accept: () => {
			// Retore to the original output port state
			const outputPort = props.node.outputs.find((output) => output.id === selectedOutputId.value);
			if (outputPort) {
				knobs.value = cloneDeep(outputPort.state as OptimizeCiemssOperationState);
			}
		}
	});
};

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
		state.constraintGroups = knobs.value.constraintGroups;
		state.interventionPolicyGroups = knobs.value.interventionPolicyGroups;
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
				knobs.value = cloneDeep(props.node.state);
				setOutputValues();
			}
		}
	},
	{ immediate: true }
);
</script>

<style scoped>
.wizard .toolbar {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: var(--gap-1) var(--gap);
	gap: var(--gap-2);
}

.spinner-message {
	align-items: center;
	align-self: center;
	display: flex;
	flex-direction: column;
	gap: var(--gap-2);
	margin-top: 15rem;
	text-align: center;
}

.info-circle {
	color: var(--text-color-secondary);
	font-size: var(--font-caption);
	margin-left: var(--gap-1);
}
.additional-options {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: var(--gap-1);
	padding: 0 var(--gap-2) var(--gap);
	background: var(--surface-200);
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
}

/* Override grid template so output expands when sidebar is closed */
.overlay-container:deep(section.scale main) {
	grid-template-columns: auto 1fr;
}

.result-message-grid {
	display: flex;
	flex-direction: column;
	gap: var(--gap-0-5);
	/* Adjust the gap between rows as needed */
	font-size: var(--font-caption);
	background-color: var(--surface-50);
	border: solid 1px var(--surface-border-light);
	border-radius: var(--border-radius);
	padding: var(--gap-small);
}
/* Select button icon fix */
.select-button .p-button-icon-left {
	color: var(--text-color-secondary);
}

/* Debugging message styles */
.result-message-row {
	display: flex;
	flex-direction: row;
	gap: var(--gap-small);
	overflow: auto;
}

.label {
	font-weight: var(--font-weight-semibold);
	/* Adjust the width of the label column as needed */
}

.value {
	flex-grow: 1;
}

:deep(.failed-run main .content-container) {
	border: 2px solid var(--error-color);
	border-radius: var(--border-radius-big);
	color: var(--error-color-text);
}

.successful-run {
	border: none;
	border-radius: none;
	color: none;
}

.form-section {
	display: flex;
	flex-direction: column;
	flex-grow: 1;
	gap: var(--gap-1);
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

/* Notebook */
.notebook-section {
	display: flex;
	flex-direction: column;
	gap: var(--gap);
	width: calc(50vw - 4rem);
	padding: var(--gap);
	background: var(--surface-100);
	border-right: 1px solid var(--surface-border-light);
}
</style>
