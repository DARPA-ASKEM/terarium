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
							@change="outputPanelBehavior"
						/>
						<tera-checkbox
							class="mt-2 mb-4"
							v-model="areSimulationsFromSameModel"
							label="All simulations are from the same model"
							disabled
						/>
						<template v-if="knobs.selectedCompareOption === CompareValue.SCENARIO">
							<label> Select simulation to use as a baseline </label>
							<Dropdown
								v-model="knobs.selectedBaselineDatasetId"
								:options="datasets"
								option-label="name"
								option-value="id"
								:loading="isFetchingDatasets"
								placeholder="Optional"
								@change="onChangeImpactComparison"
							/>
						</template>
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
						<div class="flex flex-column gap-2" v-else-if="knobs.selectedCompareOption === CompareValue.RANK">
							<label>Specify criteria of interest:</label>
							<tera-criteria-of-interest-card
								v-for="(card, i) in node.state.criteriaOfInterestCards"
								:key="i"
								:card="card"
								:variables="variableNames"
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
						</div>
						<template v-else-if="knobs.selectedCompareOption === CompareValue.ERROR">
							<label> Select dataset representing the ground truth </label>
							<Dropdown
								v-model="knobs.selectedGroundTruthDatasetId"
								:options="datasets"
								option-label="name"
								option-value="id"
								:loading="isFetchingDatasets"
								placeholder="Dataset"
							/>
							<label class="mt-2"> Map column names for each input </label>
							<DataTable class="mt-2" :value="knobs.mapping">
								<Column v-if="datasets[groundTruthDatasetIndex]?.id" :header="datasets[groundTruthDatasetIndex].name">
									<template #body="{ data }">
										<Dropdown
											class="mapping-dropdown"
											v-model="data[datasets?.[groundTruthDatasetIndex].id as string]"
											:options="
												datasets[groundTruthDatasetIndex].columns
													?.map((ele) => ele.name)
													.filter((ele) => ele?.includes('mean'))
											"
											placeholder="Variable"
										/>
									</template>
								</Column>
								<Column
									v-for="dataset in datasets.filter(({ id }) => id !== knobs.selectedGroundTruthDatasetId)"
									:key="dataset.id"
									:header="dataset.name"
								>
									<template #body="{ data }">
										<Dropdown
											v-if="dataset.id"
											class="mapping-dropdown"
											placeholder="Variable"
											v-model="data[dataset.id]"
											:options="
												dataset.columns
													?.filter((ele) => ele.fileName === getFileName(dataset))
													?.map((ele) => ele.name)
													.filter(
														(ele) =>
															!ele?.includes('median') &&
															!ele?.includes('std') &&
															!ele?.includes('min') &&
															!ele?.includes('max')
													)
											"
										/>
									</template>
								</Column>
								<Column field="deleteRow">
									<template #header>
										<span class="column-header"></span>
									</template>
									<template #body="{ index }">
										<Button class="p-button-sm p-button-text" icon="pi pi-trash" @click="deleteMapRow(index)" />
									</template>
								</Column>
							</DataTable>
							<div class="flex justify-content-between mt-2">
								<Button class="p-button-sm p-button-text" icon="pi pi-plus" label="Add mapping" @click="addMapping" />
								<!-- TODO: Automapping
									 <Button
										class="p-button-sm p-button-text"
										icon="pi pi-sparkles"
										label="Auto map"@change="updateMapping()"
										@click="getAutoMapping"
									/> -->
							</div>
						</template>
					</tera-drilldown-section>
				</template>
			</tera-slider-panel>
		</template>

		<tera-drilldown-section :tabName="DrilldownTabs.Wizard">
			<div ref="outputPanel" class="p-2">
				<Accordion multiple :active-index="activeIndices">
					<AccordionTab header="Summary"> </AccordionTab>
					<template
						v-if="
							knobs.selectedCompareOption === CompareValue.SCENARIO ||
							knobs.selectedCompareOption === CompareValue.ERROR
						"
					>
						<AccordionTab header="Variables">
							<template v-for="setting of selectedVariableSettings" :key="setting.id">
								<vega-chart
									:visualization-spec="variableCharts[setting.id]"
									:are-embed-actions-visible="true"
									expandable
								/>
							</template>
							<div v-if="selectedVariableSettings.length === 0" class="empty-state-chart">
								<img
									src="@assets/svg/operator-images/simulate-deterministic.svg"
									alt="Select a variable"
									draggable="false"
									height="80px"
								/>
								<p class="text-center">Select variables of interest in the output panel</p>
							</div>
						</AccordionTab>
					</template>
					<AccordionTab
						v-if="knobs.selectedCompareOption === CompareValue.SCENARIO && showATETable"
						header="Impact of intervention metrics"
					>
						<p class="mb-3">
							The average treatment effect (ATE) estimates the impact of a policy on the outcome in a given population;
							it is the mean difference in the outcome between those who were and weren't treated. Larger values of ATE
							are better, meaning a very impactful policy. Reference:
							<a target="_blank" rel="noopener noreferrer" href="https://pmc.ncbi.nlm.nih.gov/articles/PMC6794006/">
								https://pmc.ncbi.nlm.nih.gov/articles/PMC6794006/
							</a>
						</p>
						<DataTable :value="ateTable">
							<Column field="policyName" header="Intervention policy" />
							<Column
								v-for="variableName in ateVariableHeaders"
								:header="variableName"
								:field="variableName"
								sortable
								:key="variableName"
							>
								<template #body="{ data, field }">
									<div class="flex gap-2" v-if="data[field]">
										<div>{{ displayNumber(data[field]) }}</div>
										<div v-if="showATEErrors" class="error ml-auto">± {{ displayNumber(data[`${field}_error`]) }}</div>
									</div>
								</template>
							</Column>
							<Column field="overall" header="Overall" sortable>
								<template #body="{ data, field }">
									<div class="flex gap-2">
										<div>{{ displayNumber(data[field]) }}</div>
										<div v-if="showATEErrors" class="error ml-auto">± {{ displayNumber(data['overall_error']) }}</div>
									</div>
								</template>
							</Column>
						</DataTable>
					</AccordionTab>
					<template v-else-if="knobs.selectedCompareOption === CompareValue.RANK">
						<AccordionTab header="Ranking results">
							<p class="mb-3">
								This score represents the number of standard deviations away from the mean outcome. A higher score means
								the scenario outcome meets the criteria of interest more. The dark line (zero) is the mean outcome
								across all scenarios.
							</p>
							<vega-chart :visualization-spec="rankingResultsChart" :are-embed-actions-visible="false" expandable />
						</AccordionTab>
						<AccordionTab header="Ranking criteria">
							<vega-chart
								v-for="(rankingCriteriaChart, index) in rankingCriteriaCharts"
								:visualization-spec="rankingCriteriaChart"
								:are-embed-actions-visible="false"
								:key="index"
								expandable
							/>
						</AccordionTab>
					</template>
					<template v-else-if="knobs.selectedCompareOption === CompareValue.ERROR">
						<AccordionTab header="Model error metrics" v-if="showWIS || showMAE">
							<template v-if="showWIS">
								<h5>Weighted interval score (WIS)</h5>
								<p class="mb-3">
									The weighted interval score (WIS) measures the accuracy of a probabilistic forecasts relative to
									observations (i.e. ground truth). <b>Low WIS values are better</b>, meaning the forecast performed
									well and assigned high probability to observed outcomes. Reference:
									<a
										target="_blank"
										rel="noopener noreferrer"
										href="https://journals.plos.org/ploscompbiol/article?id=10.1371/journal.pcbi.1008618"
									>
										https://journals.plos.org/ploscompbiol/article?id=10.1371/journal.pcbi.1008618
									</a>
								</p>
								<DataTable :value="wisTable">
									<Column field="modelName" header="Model" />
									<Column
										v-for="variableName in ateVariableHeaders"
										:header="variableName"
										:field="variableName"
										sortable
										:key="variableName"
									>
										<template #body="{ data, field }">
											<div class="flex gap-2" v-if="data[field]">
												<div>{{ displayNumber(data[field]) }}</div>
												<div v-if="showATEErrors" class="error ml-auto">
													± {{ displayNumber(data[`${field}_error`]) }}
												</div>
											</div>
										</template>
									</Column>
									<Column field="overall" header="Overall" sortable>
										<template #body="{ data, field }">
											<div class="flex gap-2">
												<div>{{ displayNumber(data[field]) }}</div>
												<div v-if="showATEErrors" class="error ml-auto">
													± {{ displayNumber(data['overall_error']) }}
												</div>
											</div>
										</template>
									</Column>
								</DataTable>
							</template>
							<template v-if="showMAE">
								<h5>Mean average error (MAE)</h5>
							</template>
						</AccordionTab>
					</template>
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
				<template #overlay v-if="knobs.selectedCompareOption === CompareValue.SCENARIO">
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
				<template
					#content
					v-if="
						knobs.selectedCompareOption === CompareValue.SCENARIO || knobs.selectedCompareOption === CompareValue.ERROR
					"
				>
					<div class="output-settings-panel">
						<tera-chart-settings
							:title="'Variables over time'"
							:settings="chartSettings"
							:type="ChartSettingType.VARIABLE"
							:select-options="variableNames"
							:selected-options="selectedVariableNames"
							@open="setActiveChartSettings($event)"
							@remove="removeChartSettings"
							@selection-change="
								($event) => {
									updateChartSettings($event, ChartSettingType.VARIABLE);
									constructATETable();
									// constructWisTable();
								}
							"
						>
							<!-- plot options -->
							<div class="plot-options">
								<p>How do you want to plot the values?</p>
								<div v-for="option in plotOptions" class="flex align-items-center gap-2" :key="option.value">
									<RadioButton
										v-model="knobs.selectedPlotType"
										:value="option.value"
										name="plotValues"
										@change="onChangeImpactComparison"
									/>
									<label :for="option.value">{{ option.label }}</label>
								</div>
							</div>
						</tera-chart-settings>
						<template v-if="knobs.selectedCompareOption === CompareValue.SCENARIO">
							<Divider />
							<tera-chart-settings-quantiles :settings="chartSettings" @update-options="updateQauntilesOptions" />
							<Divider />
							<h5>Impact of intervention metrics</h5>
							<tera-checkbox v-model="showATETable" label="Average treatment effect (ATE)" />
							<tera-checkbox v-if="showATETable" v-model="showATEErrors" label="Show errors" />
						</template>
						<template v-if="knobs.selectedCompareOption === CompareValue.ERROR">
							<Divider />
							<h5>Model error metrics</h5>
							<tera-checkbox v-model="showWIS" label="Weighted interval score (WIS)" />
							<tera-checkbox v-model="showMAE" label="Mean average error (MAE)" />
						</template>
					</div>
				</template>
			</tera-slider-panel>
		</template>
	</tera-drilldown>
</template>

<script setup lang="ts">
import { isEmpty, cloneDeep } from 'lodash';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import { WorkflowNode } from '@/types/workflow';
import TeraSliderPanel from '@/components/widgets/tera-slider-panel.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import { DrilldownTabs, ChartSettingType } from '@/types/common';
import { onMounted, ref, watch, computed } from 'vue';
import Button from 'primevue/button';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Dropdown from 'primevue/dropdown';
import Divider from 'primevue/divider';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import { Dataset, InterventionPolicy, ModelConfiguration } from '@/types/Types';
import TeraCheckbox from '@/components/widgets/tera-checkbox.vue';
import RadioButton from 'primevue/radiobutton';
import VegaChart from '@/components/widgets/VegaChart.vue';
import { deleteAnnotation } from '@/services/chart-settings';
import TeraChartSettings from '@/components/widgets/tera-chart-settings.vue';
import TeraChartSettingsPanel from '@/components/widgets/tera-chart-settings-panel.vue';
import TeraChartSettingsQuantiles from '@/components/widgets/tera-chart-settings-quantiles.vue';
import { useChartSettings } from '@/composables/useChartSettings';
import { useDrilldownChartSize } from '@/composables/useDrilldownChartSize';
import { useCharts, type ChartData } from '@/composables/useCharts';
import { DataArray } from '@/services/models/simulation-service';
import { mean, stddev, quantile } from '@/utils/stats';
import { displayNumber } from '@/utils/number';
import { getFileName } from '@/services/dataset';
import TeraCriteriaOfInterestCard from './tera-criteria-of-interest-card.vue';
import {
	blankCriteriaOfInterest,
	CompareDatasetsState,
	CompareValue,
	CriteriaOfInterestCard,
	PlotValue,
	type CompareDatasetsMap
} from './compare-datasets-operation';
import { generateRankingCharts, generateImpactCharts, initialize } from './compare-datasets-utils';

const props = defineProps<{
	node: WorkflowNode<CompareDatasetsState>;
}>();

const emit = defineEmits(['update-state', 'close']);

const compareOptions: { label: string; value: CompareValue }[] = [
	{ label: 'Compare scenarios', value: CompareValue.SCENARIO },
	{ label: 'Rank interventions based on multiple criteria', value: CompareValue.RANK },
	{ label: 'Compare model errors', value: CompareValue.ERROR }
];

const datasets = ref<Dataset[]>([]);
console.log(datasets);
const datasetResults = ref<{
	results: DataArray[];
	summaryResults: DataArray[];
	datasetResults: DataArray[];
} | null>(null);
const modelConfigurations = ref<ModelConfiguration[]>([]);
const interventionPolicies = ref<InterventionPolicy[]>([]);
const modelConfigIdToInterventionPolicyIdMap = ref<Record<string, string[]>>({});

const showATETable = ref(true);
const showATEErrors = ref(false);
const ateTable = ref<any[]>([]);
const ateVariableHeaders = ref<string[]>([]);

const showWIS = ref(true);
const wisTable = ref<any[]>([]);
const showMAE = ref(false);

const plotOptions = [
	{ label: 'Raw values', value: PlotValue.VALUE },
	{ label: 'Percent change', value: PlotValue.PERCENTAGE },
	{ label: 'Difference', value: PlotValue.DIFFERENCE }
];

const isInputSettingsOpen = ref(true);
const isOutputSettingsOpen = ref(true);
const activeIndices = ref([0, 1, 2]);

const isFetchingDatasets = ref(false);
const areSimulationsFromSameModel = ref(true);

const onRun = () => {
	if (knobs.value.selectedCompareOption === CompareValue.RANK) {
		generateRankingCharts(
			rankingCriteriaCharts,
			rankingResultsChart,
			props.node,
			rankingChartData,
			datasets,
			modelConfigurations,
			interventionPolicies
		);
	} else if (knobs.value.selectedCompareOption === CompareValue.SCENARIO) {
		constructATETable();
	}
};

function onChangeImpactComparison() {
	generateImpactCharts(impactChartData, datasets, datasetResults, baselineDatasetIndex, selectedPlotType);
}

interface BasicKnobs {
	selectedCompareOption: CompareValue;
	criteriaOfInterestCards: CriteriaOfInterestCard[];
	selectedBaselineDatasetId: string | null;
	selectedGroundTruthDatasetId: string | null;
	selectedPlotType: PlotValue;
	mapping: CompareDatasetsMap[];
}

const knobs = ref<BasicKnobs>({
	selectedCompareOption: CompareValue.SCENARIO,
	// Impact
	selectedBaselineDatasetId: null,
	selectedPlotType: PlotValue.PERCENTAGE,
	// Ranking interventions
	criteriaOfInterestCards: [],
	// Compare model errors
	selectedGroundTruthDatasetId: null,
	mapping: []
});

const addCriteria = () => {
	knobs.value.criteriaOfInterestCards.push(cloneDeep(blankCriteriaOfInterest));
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
	setActiveChartSettings,
	updateQauntilesOptions
} = useChartSettings(props, emit);

const selectedVariableNames = computed(() => selectedVariableSettings.value.map((s) => s.selectedVariables[0]));

const outputPanel = ref(null);
const chartSize = useDrilldownChartSize(outputPanel);

const impactChartData = ref<ChartData | null>(null);
const rankingChartData = ref<ChartData | null>(null);
const rankingResultsChart = ref<any>(null);
const rankingCriteriaCharts = ref<any>([]);

const variableNames = computed(() => {
	if (impactChartData.value === null) return [];
	const excludes = ['timepoint_id', 'sample_id', 'timepoint_unknown'];
	return Object.keys(impactChartData.value.pyciemssMap).filter((key) => !excludes.includes(key));
});

const { generateAnnotation, getChartAnnotationsByChartId, useCompareDatasetCharts } = useCharts(
	props.node.id,
	null,
	null,
	impactChartData,
	chartSize,
	null,
	null
);
const selectedPlotType = computed(() => knobs.value.selectedPlotType);
const baselineDatasetIndex = computed(() =>
	datasets.value.findIndex((dataset) => dataset.id === knobs.value.selectedBaselineDatasetId)
);
const variableCharts = useCompareDatasetCharts(
	selectedVariableSettings,
	selectedPlotType,
	datasets,
	modelConfigurations,
	interventionPolicies
);
const groundTruthDatasetIndex = computed(() =>
	datasets.value.findIndex((dataset) => dataset.id === knobs.value.selectedGroundTruthDatasetId)
);

function outputPanelBehavior() {
	if (knobs.value.selectedCompareOption === CompareValue.RANK) {
		isOutputSettingsOpen.value = false;
	} else if (knobs.value.selectedCompareOption === CompareValue.SCENARIO) {
		isOutputSettingsOpen.value = true;
	}
}

function constructATETable() {
	ateTable.value = [];
	ateVariableHeaders.value = [];

	const variableToTypeMap: Record<string, string> = {};
	const means: Record<string, number> = {};
	const meanErrors: Record<string, number> = {};

	datasetResults.value?.summaryResults.forEach((summaryResult) => {
		Object.keys(summaryResult[0]).forEach((key) => {
			if (
				key.includes('_param_') ||
				!key.includes('_mean:') ||
				// Skip if the variable is not selected in output settings
				!selectedVariableNames.value.some((variableName) => {
					if (key.includes(variableName)) {
						if (!variableToTypeMap[variableName]) {
							variableToTypeMap[variableName] = key.includes('_observable_state_') ? '_observable_state_' : '_state_';
							ateVariableHeaders.value.push(variableName);
						}
						return true;
					}
					return false;
				})
			) {
				return;
			}
			const values = summaryResult.map((row) => row[key]);
			means[key] = mean(values);
			meanErrors[key] = stddev(values) / Math.sqrt(values.length);
		});
	});

	const meanKeyNames = Object.keys(means);

	datasets.value.forEach((dataset, index) => {
		if (index === baselineDatasetIndex.value) return;

		const ateRow: Record<string, number> = {};
		const ateValues: number[] = [];

		Object.entries(variableToTypeMap).forEach(([variableName, type]) => {
			const key = `${variableName}${type}mean:${index}`;
			if (!meanKeyNames.includes(key)) return;

			const baselineKey = `${key.slice(0, -1)}${baselineDatasetIndex.value}`;

			const ate = means[key] - means[baselineKey];
			const ateError = Math.sqrt(meanErrors[key] ** 2 + meanErrors[baselineKey] ** 2);

			ateRow[variableName] = ate;
			ateRow[`${variableName}_error`] = ateError;

			ateValues.push(ate);
		});
		ateRow.overall = mean(ateValues);
		ateRow.overall_error = stddev(ateValues) / Math.sqrt(ateValues.length);

		ateTable.value.push({ policyName: dataset.name, ...ateRow });
	});
}

function addMapping() {
	const newMapping: CompareDatasetsMap = {};
	datasets.value.forEach(({ id }) => {
		newMapping[id as string] = '';
	});
	knobs.value.mapping.push(newMapping);
}

function deleteMapRow(index: number) {
	knobs.value.mapping.splice(index, 1);
}

// TODO: All WIS functions should probably be moved to a stats or util file and deserve some cleaning up
function computeQuantile(values: number[], quantiles: number[]) {
	// Compute the estimated quantiles from a time-series dataset with many samples.
	//     Parameters
	//     ------------
	//     df: pandas.DataFrame
	//         Dataset with columns named "timepoint_unknown" (contains timepoint values of given outcome variable) and an outcome variable name.
	//     quantiles: iterable
	//         List of alpha values for which quantiles are estimated from the sampled data points.
	const quantileValueMap: Record<number, number> = {};
	quantiles.forEach((q) => {
		quantileValueMap[q] = quantile(values, q);
	});
	return quantileValueMap;
}

function intervalScore(
	groundTruthObservations: Record<number, number>,
	variableObservations: Record<number, number>,
	alpha: number,
	percent: boolean,
	checkConsistency: boolean,
	leftQuantile: number | null = null,
	rightQuantile: number | null = null
) {
	if (!leftQuantile) {
		leftQuantile = variableObservations[alpha / 2];
	}
	if (!rightQuantile) {
		rightQuantile = variableObservations[1 - alpha / 2];
	}

	if (checkConsistency && leftQuantile > rightQuantile) {
		throw new Error('Left quantile must be smaller than right quantile.');
	}

	let sharpness = rightQuantile - leftQuantile;

	let calibration = Object.values(groundTruthObservations).map((obs) => {
		const leftClip = Math.max(0, leftQuantile - obs);
		const rightClip = Math.max(0, obs - rightQuantile);
		return ((leftClip + rightClip) * 2) / alpha;
	});

	if (percent) {
		sharpness /= mean(Object.values(groundTruthObservations));
		// sharpness = sharpness / observations.map((obs) => Math.abs(obs));
		calibration = calibration.map((cal, index) => cal / Math.abs(groundTruthObservations[index]));
	}

	const total = calibration.map((cal) => cal + sharpness);

	return { total, sharpness, calibration };
}

function weightedIntervalScore(
	groundTruthObservations: Record<number, number>,
	variableObservations: Record<number, number>,
	alphas: number[],
	weights: number[],
	percent: boolean = false,
	checkConsistency: boolean = true
) {
	if (isEmpty(weights)) {
		weights = alphas.map((alpha) => alpha / 2);
	}

	// Helper function to weigh scores
	function weighScores(tupleIn, weight) {
		return [tupleIn[0] * weight, tupleIn[1] * weight, tupleIn[2] * weight];
	}

	// Calculate interval scores for each alpha and weight
	const intervalScores = alphas.map((alpha, index) => {
		const { total, sharpness, calibration } = intervalScore(
			groundTruthObservations,
			variableObservations,
			alpha,
			percent,
			checkConsistency
		);
		return weighScores([total, sharpness, calibration], weights[index]);
	});

	// Transpose the result to sum across different alphas
	const summedScores = intervalScores.reduce(
		(acc: any, scoreTuple) => {
			scoreTuple.forEach((score, idx) => {
				acc[idx].push(score);
			});
			return acc;
		},
		[[], [], []]
	);

	const [totalScores, sharpnessScores, calibrationScores] = summedScores;

	// Sum the scores across all alphas and normalize by the sum of the weights
	const total = totalScores.reduce((sum, val) => sum + val, 0) / weights.reduce((sum, val) => sum + val, 0);
	const sharpness = sharpnessScores.reduce((sum, val) => sum + val, 0) / weights.reduce((sum, val) => sum + val, 0);
	const calibration = calibrationScores.reduce((sum, val) => sum + val, 0) / weights.reduce((sum, val) => sum + val, 0);

	return { total, sharpness, calibration };
}

function constructWisTable() {
	const DEFAULT_ALPHA_QS = [
		0.01, 0.025, 0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5, 0.55, 0.6, 0.65, 0.7, 0.75, 0.8, 0.85, 0.9,
		0.95, 0.975, 0.99
	];

	wisTable.value = [];

	const observationsMap: Record<number, number> = {};
	const variableToTypeMap: Record<string, string> = {};

	datasetResults.value?.summaryResults.forEach((summaryResult) => {
		Object.keys(summaryResult[0]).forEach((key) => {
			if (
				key.includes('_param_') ||
				!key.includes('_mean:') ||
				// Skip if the variable is not selected in output settings
				!selectedVariableNames.value.some((variableName) => {
					if (key.includes(variableName)) {
						if (!variableToTypeMap[variableName]) {
							variableToTypeMap[variableName] = key.includes('_observable_state_') ? '_observable_state_' : '_state_';
							ateVariableHeaders.value.push(variableName);
						}
						return true;
					}
					return false;
				})
			) {
				return;
			}
			const values = summaryResult.map((row) => row[key]);
			const observations = computeQuantile(values, DEFAULT_ALPHA_QS);
			observationsMap[key] = observations;
		});
	});

	const observationsKeyNames = Object.keys(observationsMap);

	datasets.value.forEach((dataset, index) => {
		if (index === groundTruthDatasetIndex.value) return;

		const wisRow: Record<string, number> = {};
		const wisValues: number[] = [];

		Object.entries(variableToTypeMap).forEach(([variableName, type]) => {
			const key = `${variableName}${type}mean:${index}`;
			if (!observationsKeyNames.includes(key)) return;

			let groundTruthKey = `${key.slice(0, -1)}${groundTruthDatasetIndex.value}`;
			// Use mapping to get ground truth key
			if (!observationsKeyNames.includes(groundTruthKey)) {
				let isFound = false;
				knobs.value.mapping.forEach((mapping) => {
					if (
						!isFound &&
						Object.values(mapping).includes(key.slice(0, -2)) &&
						knobs.value.selectedGroundTruthDatasetId
					) {
						groundTruthKey = `${mapping[knobs.value.selectedGroundTruthDatasetId]}:${groundTruthDatasetIndex.value}`;
						isFound = true;
					}
				});
			}

			const wis = weightedIntervalScore(
				observationsMap[groundTruthKey],
				observationsMap[key],
				[0.02, 0.05, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9],
				[],
				true
			);

			wisRow[variableName] = wis.total;
			wisValues.push(wis.total);
		});
		wisRow.overall = mean(wisValues);
		wisTable.value.push({ modelName: dataset.name, ...wisRow });
	});
}

onMounted(async () => {
	const state = cloneDeep(props.node.state);
	knobs.value = Object.assign(knobs.value, state);

	outputPanelBehavior();

	await initialize(
		props.node,
		knobs,
		isFetchingDatasets,
		datasets,
		datasetResults,
		modelConfigIdToInterventionPolicyIdMap,
		impactChartData,
		rankingChartData,
		baselineDatasetIndex,
		selectedPlotType,
		modelConfigurations,
		interventionPolicies,
		rankingCriteriaCharts,
		rankingResultsChart
	);

	constructATETable();

	if (isEmpty(knobs.value.mapping)) addMapping();
	if (false) constructWisTable();
});

watch(
	() => knobs.value,
	() => {
		const state = cloneDeep(props.node.state);
		state.criteriaOfInterestCards = knobs.value.criteriaOfInterestCards;
		state.selectedCompareOption = knobs.value.selectedCompareOption;
		state.selectedBaselineDatasetId = knobs.value.selectedBaselineDatasetId;
		state.selectedGroundTruthDatasetId = knobs.value.selectedGroundTruthDatasetId;
		state.selectedPlotType = knobs.value.selectedPlotType;
		state.mapping = knobs.value.mapping;
		emit('update-state', state);
	},
	{ deep: true }
);
</script>

<style scoped>
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
	display: flex;
	flex-direction: column;
	gap: var(--gap-2);
	padding: var(--gap-3);
	background: var(--surface-200);
	border-radius: var(--border-radius);
	box-shadow: inset 0 0 6px rgba(0, 0, 0, 0.1);
	margin-bottom: var(--gap-1);
}
.empty-state-chart {
	display: flex;
	flex-direction: column;
	flex-grow: 1;
	gap: var(--gap-4);
	justify-content: center;
	align-items: center;
	height: 12rem;
	margin: var(--gap-6);
	padding: var(--gap-4);
	background: var(--surface-100);
	color: var(--text-color-secondary);
	border-radius: var(--border-radius);
}

/* See if this rule should be applied to all tables, it makes a lot of sense */
:deep(th) {
	padding: var(--gap-4);
}

:deep(td) {
	white-space: nowrap;
}

.error {
	color: var(--text-color-secondary);
}

.mapping-dropdown {
	width: 7rem;
}
</style>
