<template>
	<tera-drilldown
		:node="node"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
	>
		<template #sidebar>
			<tera-slider-panel
				class="input-config"
				v-model:is-open="isInputSettingsOpen"
				header="Compare settings"
				content-width="440px"
			>
				<template #content>
					<tera-drilldown-section class="px-3">
						<Button class="ml-auto" size="small" label="Run" @click="onRun" />
						<label>What do you want to compare?</label>
						<Dropdown
							v-model="knobs.selectedCompareOption"
							:options="compareOptions"
							option-label="label"
							option-value="value"
						/>
						<!-- Pascale asked me to hide this until the feature is implemented -->
						<!-- <tera-checkbox
							class="pt-2"
							v-model="isSimulationsFromSameModel"
							label="All simulations are from the same model"
							disabled
						/> -->
						<div class="mb-4" />
						<template v-if="knobs.selectedCompareOption === CompareValue.IMPACT">
							<label> Select simulation to use as a baseline (optional) </label>
							<Dropdown
								v-model="knobs.selectedDataset"
								:options="datasets"
								option-label="name"
								option-value="id"
								:loading="isFetchingDatasets"
								placeholder="Optional"
								@change="generateChartData"
							/>
							<div class="mb-4" />
							<label>Comparison tables</label>
							<tera-checkbox v-model="isATESelected" label="Average treatment effect (ATE)" />
						</template>
						<div class="mb-4" />
						<!-- Pascale asked me to omit this timepoint selector, but I'm keeping it here until we are certain it's not needed -->
						<!--
						<label class="mt-2">Timepoint column</label>
						<Dropdown
							v-model="timepointHeaderName"
							:options="commonHeaderNames"
							placeholder="Select timepoint header"
							@change="generateChartData"
						/>
						<div class="mb-4" />
						-->
						<template v-if="knobs.selectedCompareOption === CompareValue.RANK">
							<label>Specify criteria of interest:</label>
							<tera-criteria-of-interest-card
								v-for="(card, i) in node.state.criteriaOfInterestCards"
								:key="i"
								:card="card"
								@delete="deleteCriteria(i)"
								@update="(e) => updateCriteria(e, i)"
							/>
							<div>
								<Button
									class="pt-2"
									text
									icon="pi pi-plus"
									size="small"
									label="Add new criteria"
									@click="addCriteria"
								/>
							</div>
						</template>
					</tera-drilldown-section>
				</template>
			</tera-slider-panel>
		</template>

		<tera-drilldown-section :tabName="DrilldownTabs.Wizard">
			<div ref="outputPanel">
				<Accordion multiple :active-index="activeIndices">
					<AccordionTab header="Summary"> </AccordionTab>
					<AccordionTab header="Variables">
						<template v-for="setting of selectedVariableSettings" :key="setting.id">
							<vega-chart
								:visualization-spec="variableCharts[setting.id]"
								:are-embed-actions-visible="false"
								expandable
							/>
						</template>
					</AccordionTab>
					<AccordionTab header="Comparison table"> </AccordionTab>
				</Accordion>
			</div>
		</tera-drilldown-section>

		<tera-drilldown-section :tabName="DrilldownTabs.Notebook"> </tera-drilldown-section>

		<template #sidebar-right>
			<tera-slider-panel
				class="input-config"
				v-model:is-open="isOutputSettingsOpen"
				header="Output settings"
				content-width="360px"
			>
				<template #overlay>
					<tera-chart-settings-panel
						:annotations="
							[ChartSettingType.VARIABLE, ChartSettingType.VARIABLE_COMPARISON].includes(
								activeChartSettings?.type as ChartSettingType
							)
								? getChartAnnotationsByChartId(activeChartSettings?.id ?? '')
								: undefined
						"
						:active-settings="activeChartSettings"
						:generate-annotation="generateAnnotation"
						@delete-annotation="deleteAnnotation"
						@update-settings="updateActiveChartSettings"
						@close="setActiveChartSettings(null)"
					/>
				</template>
				<template #content>
					<div class="output-settings-panel">
						<tera-chart-settings
							:title="'Variables over time'"
							:settings="chartSettings"
							:type="ChartSettingType.VARIABLE"
							:select-options="variableNames"
							:selected-options="selectedVariableSettings.map((s) => s.selectedVariables[0])"
							@open="setActiveChartSettings($event)"
							@remove="removeChartSettings"
							@selection-change="updateChartSettings"
						>
							<!-- plot options -->
							<div class="plot-options">
								<p class="mb-2">How do you want to plot the values?</p>
								<div v-for="option in plotOptions" class="flex align-items-center" :key="option.value">
									<RadioButton
										v-model="knobs.selectedPlotType"
										:value="option.value"
										name="plotValues"
										@change="generateChartData"
									/>
									<label class="pl-2 py-1" :for="option.value">{{ option.label }}</label>
								</div>
							</div>
						</tera-chart-settings>
					</div>
				</template>
			</tera-slider-panel>
		</template>
	</tera-drilldown>
</template>

<script setup lang="ts">
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import { WorkflowNode, WorkflowPortStatus } from '@/types/workflow';
import TeraSliderPanel from '@/components/widgets/tera-slider-panel.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import { DrilldownTabs, ChartSettingType } from '@/types/common';
import { onMounted, ref, watch, computed } from 'vue';
import Button from 'primevue/button';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Dropdown from 'primevue/dropdown';
import { Dataset } from '@/types/Types';
import { getDataset, getDatasetResultCSV, mergeResults } from '@/services/dataset';
import TeraCheckbox from '@/components/widgets/tera-checkbox.vue';
import RadioButton from 'primevue/radiobutton';
import { cloneDeep } from 'lodash';
import VegaChart from '@/components/widgets/VegaChart.vue';
import { deleteAnnotation } from '@/services/chart-settings';
import TeraChartSettings from '@/components/widgets/tera-chart-settings.vue';
import TeraChartSettingsPanel from '@/components/widgets/tera-chart-settings-panel.vue';
import { useChartSettings } from '@/composables/useChartSettings';
import { useDrilldownChartSize } from '@/composables/useDrilldownChartSize';
import { useCharts, type ChartData } from '@/composables/useCharts';
import { renameFnGenerator } from '@/components/workflow/ops/calibrate-ciemss/calibrate-utils';
import { DataArray, parsePyCiemssMap, processAndSortSamplesByTimepoint } from '@/services/models/simulation-service';
import TeraCriteriaOfInterestCard from './tera-criteria-of-interest-card.vue';
import {
	blankCriteriaOfInterest,
	CompareDatasetsState,
	CompareValue,
	CriteriaOfInterestCard,
	PlotValue
} from './compare-datasets-operation';

const props = defineProps<{
	node: WorkflowNode<CompareDatasetsState>;
}>();

const emit = defineEmits(['update-state', 'update-status', 'close']);

const compareOptions: { label: string; value: CompareValue }[] = [
	{ label: 'Compare the impact of interventions', value: CompareValue.IMPACT },
	{ label: 'Rank interventions based on multiple charts', value: CompareValue.RANK }
];

const datasets = ref<Dataset[]>([]);
const dataResults = ref<{
	results: DataArray[];
	summaryResults: DataArray[];
	datasetResults: DataArray[];
} | null>(null);

const plotOptions = [
	{ label: 'Raw values', value: PlotValue.VALUE },
	{ label: 'Percent change', value: PlotValue.PERCENTAGE },
	{ label: 'Difference', value: PlotValue.DIFFERENCE }
];

const isInputSettingsOpen = ref(true);
const isOutputSettingsOpen = ref(true);
const activeIndices = ref([0, 1, 2]);

const isFetchingDatasets = ref(false);
const isATESelected = ref(false);

const onRun = () => {
	console.log('run');
};

interface BasicKnobs {
	criteriaOfInterestCards: CriteriaOfInterestCard[];
	selectedCompareOption: CompareValue;
	selectedDataset: string | null;
	selectedPlotType: PlotValue;
}

const knobs = ref<BasicKnobs>({
	criteriaOfInterestCards: [],
	selectedCompareOption: CompareValue.IMPACT,
	selectedDataset: null,
	selectedPlotType: PlotValue.PERCENTAGE
});

const addCriteria = () => {
	knobs.value.criteriaOfInterestCards.push(blankCriteriaOfInterest);
};

const deleteCriteria = (index: number) => {
	knobs.value.criteriaOfInterestCards.splice(index, 1);
};

const updateCriteria = (card: Partial<CriteriaOfInterestCard>, index: number) => {
	Object.assign(knobs.value.criteriaOfInterestCards[index], card);
};

const {
	activeChartSettings,
	chartSettings,
	selectedVariableSettings,
	removeChartSettings,
	updateChartSettings,
	updateActiveChartSettings,
	setActiveChartSettings
} = useChartSettings(props, emit);

const outputPanel = ref(null);
const chartSize = useDrilldownChartSize(outputPanel);

const chartData = ref<ChartData | null>(null);
const variableNames = computed(() => {
	if (chartData.value === null) return [];
	const excludes = ['timepoint_id', 'sample_id', 'timepoint_unknown'];
	return Object.keys(chartData.value.pyciemssMap).filter((key) => !excludes.includes(key));
});

const { generateAnnotation, getChartAnnotationsByChartId, useCompareDatasetCharts } = useCharts(
	props.node.id,
	null,
	null,
	chartData,
	chartSize,
	null,
	null
);
const selectedPlotType = computed(() => knobs.value.selectedPlotType);
const baselineDatasetIndex = computed(() =>
	datasets.value.findIndex((dataset) => dataset.id === knobs.value.selectedDataset)
);
const variableCharts = useCompareDatasetCharts(selectedVariableSettings, selectedPlotType, baselineDatasetIndex);

const initialize = async () => {
	const state = cloneDeep(props.node.state);
	knobs.value = Object.assign(knobs.value, state);

	const inputs = props.node.inputs;
	const datasetInputs = inputs.filter(
		(input) => input.type === 'datasetId' && input.status === WorkflowPortStatus.CONNECTED
	);
	const promises = datasetInputs.map((input) => getDataset(input.value![0]));

	isFetchingDatasets.value = true;
	await Promise.all(promises).then((ds) => {
		const filteredDatasets: Dataset[] = ds.filter((dataset) => dataset !== null);
		datasets.value.push(...filteredDatasets);
	});
	// Fetch the results
	dataResults.value = await fetchDatasetResults(datasets.value);
	isFetchingDatasets.value = false;

	if (!knobs.value.selectedDataset) knobs.value.selectedDataset = datasets.value[0]?.id ?? null;

	generateChartData();
};

function isSimulationData(datset: Dataset) {
	// assume if the dataset has these two files, it's a simulation dataset
	return (
		datset.fileNames?.find((name) => name === 'result.csv') &&
		datset.fileNames?.find((name) => name === 'result_summary.csv')
	);
}

async function fetchDatasetResults(datasetList: Dataset[]) {
	// Fetch simulation results
	const results: DataArray[] = (
		await Promise.all(
			datasetList.map((dataset, index) => {
				if (!isSimulationData(dataset)) return Promise.resolve([]);
				return getDatasetResultCSV(dataset, 'result.csv', renameFnGenerator(`${index}`));
			})
		)
	).filter((result) => result.length > 0);
	// Fetch simulation summary results
	const summaryResults: DataArray[] = (
		await Promise.all(
			datasetList.map((dataset, index) => {
				if (!isSimulationData(dataset)) return Promise.resolve([]);
				return getDatasetResultCSV(dataset, 'result_summary.csv', renameFnGenerator(`${index}`));
			})
		)
	).filter((result) => result.length > 0);
	// Fetch non simulation dataset results
	const datasetResults: DataArray[] = (
		await Promise.all(
			datasetList.map((dataset, index) => {
				if (isSimulationData(dataset)) return Promise.resolve([]);
				return getDatasetResultCSV(dataset, dataset.fileNames?.[0] ?? '', renameFnGenerator(`${index}`));
			})
		)
	).filter((result) => result.length > 0);
	return {
		results,
		summaryResults,
		datasetResults
	};
}

const transformRowValuesRelativeToBaseline = (
	row: Record<string, number>,
	baselineDataIndex: number,
	plotType: PlotValue
) => {
	const transformed: Record<string, number> = {};
	Object.entries(row).forEach(([key, value]) => {
		const [dataKey, datasetIndex] = key.split(':');
		if (datasetIndex === undefined) {
			transformed[key] = value;
			return;
		}
		const baselineValue = row[`${dataKey}:${baselineDataIndex}`];
		if (plotType === PlotValue.DIFFERENCE) {
			transformed[key] = value - baselineValue;
		} else if (plotType === PlotValue.PERCENTAGE) {
			transformed[key] = ((value - baselineValue) / baselineValue) * 100;
		} else if (plotType === PlotValue.VALUE) {
			transformed[key] = value;
		}
	});
	return transformed;
};

/**
 * Removes the dataset index suffix from the variable names and returns a new object with the unique variable names which can be used as an input to the parsePyCiemssMap function.
 * E.g. { 'variableName:0': 1, 'variableName:1': 2 } -> { variableName: 'variableName' }
 * @param obj
 */
const uniqueVarNames = (obj: Record<string, any>) => {
	const newObj = {};
	Object.keys(obj)
		.map((key) => key.split(':')[0])
		.forEach((key) => {
			newObj[key] = key;
		});
	return newObj;
};

/**
 * Build variable name mapping for the non simulation result dataset
 * Prefix the variable name with 'data_' to distinguish it from the simulation result variables
 */
const buildDatasetVarMapping = (dsets: DataArray[]) => {
	const map: Record<string, any> = {};
	dsets.forEach((dataset) => {
		Object.keys(dataset[0]).forEach((key) => {
			const varName = key.split(':')[0];
			map[`data_${varName}`] = varName;
		});
	});
	return map;
};

async function generateChartData() {
	if (datasets.value.length <= 1 || !dataResults.value) return;
	const { results, summaryResults, datasetResults } = dataResults.value;

	// Merge all datasets into single dataset
	const result = mergeResults(...results);
	const summaryResult = mergeResults(...summaryResults, ...datasetResults);

	// Transform the values relative to the baseline dataset
	const resultTransformed = result.map((row) =>
		transformRowValuesRelativeToBaseline(row, baselineDatasetIndex.value, selectedPlotType.value)
	);
	const resultSummaryTransformed = summaryResult.map((row) =>
		transformRowValuesRelativeToBaseline(row, baselineDatasetIndex.value, selectedPlotType.value)
	);

	// Process data for uncertainty intervals chart mode
	const resultGroupByTimepoint = processAndSortSamplesByTimepoint(resultTransformed);

	// Build pyciemss map for display variable name map for the simulation result variables
	const pyciemssMap = parsePyCiemssMap(uniqueVarNames(result[0] ?? {}));
	// Augment the pyciemss map with the dataset variable mapping
	Object.assign(pyciemssMap, buildDatasetVarMapping(datasetResults));

	// Build translation map
	const translationMap = {};
	Object.keys(pyciemssMap).forEach((key) => {
		datasets.value.forEach((dataset, index) => {
			translationMap[`${pyciemssMap[key]}:${index}`] = `${dataset.name}`;
		});
	});

	chartData.value = {
		result: resultTransformed,
		resultSummary: resultSummaryTransformed,
		resultGroupByTimepoint,
		pyciemssMap,
		translationMap,
		numComparableDatasets: datasets.value.length
	};
}

onMounted(() => {
	initialize();
});

watch(
	() => knobs.value,
	() => {
		const state = cloneDeep(props.node.state);
		state.criteriaOfInterestCards = knobs.value.criteriaOfInterestCards;
		state.selectedCompareOption = knobs.value.selectedCompareOption;
		state.selectedDataset = knobs.value.selectedDataset;
		state.selectedPlotType = knobs.value.selectedPlotType;
		emit('update-state', state);
	},
	{ deep: true }
);
</script>

<style scoped>
label {
	padding: var(--gap-2) 0;
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

.plot-options {
	padding: var(--gap-3);
	background: var(--surface-200);
	border-radius: var(--border-radius);
	box-shadow: inset 0 0 6px rgba(0, 0, 0, 0.1);
	margin-bottom: var(--gap-1);
}
</style>
