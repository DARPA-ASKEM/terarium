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
						<Button
							v-if="knobs.selectedCompareOption !== CompareValue.RANK"
							class="ml-auto"
							size="small"
							label="Run"
							@click="onRun"
						/>
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
								v-for="(card, i) in knobs.criteriaOfInterestCards"
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
							<label class="mt-1"> (This mapping just assists in making the WIS table appear)</label>
							<DataTable v-if="groundTruthDatasetIndex !== -1" class="mt-2" :value="knobs.mapping">
								<!--Put ground truth dataset in the first column-->
								<Column
									v-for="dataset in [
										datasets[groundTruthDatasetIndex],
										...datasets.filter((dataset) => dataset.id !== knobs.selectedGroundTruthDatasetId)
									]"
									:key="dataset.id"
									:header="dataset.name"
								>
									<template #body="{ data }">
										<Dropdown
											v-if="dataset.id"
											class="mapping-dropdown"
											placeholder="Variable"
											filter
											v-model="data[dataset.id]"
											:options="mappingOptions[dataset.id]"
											@change="constructWisTable"
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
				<div class="px-2">For best behavior ensure that datasets have the same amount of timepoints</div>
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
							<vega-chart
								v-if="rankingCharts.rankingResultsChart"
								:visualization-spec="rankingCharts.rankingResultsChart"
								:are-embed-actions-visible="false"
								expandable
							/>
						</AccordionTab>
						<AccordionTab header="Ranking criteria">
							<vega-chart
								v-for="(rankingCriteriaChart, index) in rankingCharts.rankingCriteriaCharts"
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
								<header class="flex justify-content-between mb-2">
									<h5>Weighted interval score (WIS)</h5>
									<div class="flex gap-4">
										<tera-checkbox
											v-model="calculateWisByPercentage"
											label="Calculate by percentage"
											@change="constructWisTable"
										/>
										<tera-checkbox v-model="checkConsistency" label="Check consistency" @change="constructWisTable" />
									</div>
								</header>
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
												<div>
													{{ displayNumber(data[field]) }}
													<template v-if="calculateWisByPercentage">%</template>
												</div>
												<!-- TODO: I don't think there are errors based on the WIS code but they're in the design
												  <div v-if="showATEErrors" class="error ml-auto">
													± {{ displayNumber(data[`${field}_error`]) }}
												</div> -->
											</div>
										</template>
									</Column>
									<Column field="overall" header="Overall" sortable>
										<template #body="{ data, field }">
											<div class="flex gap-2">
												<div>{{ displayNumber(data[field]) }}</div>
												<template v-if="calculateWisByPercentage">%</template>
												<!-- TODO: I don't think there are errors based on the WIS code but they're in the design
												<div v-if="showATEErrors" class="error ml-auto">
													± {{ displayNumber(data['overall_error']) }}
												</div> -->
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
						:get-chart-labels="getChartLabels"
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
									constructWisTable();
								}
							"
						>
							<!-- plot options -->
							<div class="plot-options">
								<p>How do you want to plot the values?</p>
								<div v-for="option in plotOptions" class="flex align-items-center gap-2" :key="option.value">
									<RadioButton v-model="knobs.selectedPlotType" :value="option.value" name="plotValues" />
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
import { logger } from '@/utils/logger';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import { WorkflowNode } from '@/types/workflow';
import TeraSliderPanel from '@/components/widgets/tera-slider-panel.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import { DrilldownTabs, ChartSettingType } from '@/types/common';
import { onMounted, ref, computed, onBeforeUnmount } from 'vue';
import Button from 'primevue/button';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Dropdown from 'primevue/dropdown';
import Divider from 'primevue/divider';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import { Dataset, InterventionPolicy, Model, ModelConfiguration } from '@/types/Types';
import TeraCheckbox from '@/components/widgets/tera-checkbox.vue';
import RadioButton from 'primevue/radiobutton';
import VegaChart from '@/components/widgets/VegaChart.vue';
import { deleteAnnotation } from '@/services/chart-annotation';
import TeraChartSettings from '@/components/widgets/tera-chart-settings.vue';
import TeraChartSettingsPanel from '@/components/widgets/tera-chart-settings-panel.vue';
import TeraChartSettingsQuantiles from '@/components/widgets/tera-chart-settings-quantiles.vue';
import { useChartSettings } from '@/composables/useChartSettings';
import { useDrilldownChartSize } from '@/composables/useDrilldownChartSize';
import { useCharts, type ChartData } from '@/composables/useCharts';
import { DataArray } from '@/services/models/simulation-service';
import { mean, stddev, computeQuantile, getWeightedIntervalScore } from '@/utils/stats';
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
import { initialize, buildChartData } from './compare-datasets-utils';

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

const datasetResults = ref<{
	results: DataArray[];
	summaryResults: DataArray[];
	datasetResults: DataArray[];
} | null>(null);
const models = ref<Model[]>([]);
const modelConfigurations = ref<ModelConfiguration[]>([]);
const interventionPolicies = ref<InterventionPolicy[]>([]);
const modelConfigIdToInterventionPolicyIdMap = ref<Record<string, string[]>>({});

const showATETable = ref(true);
const showATEErrors = ref(false);
const ateTable = ref<any[]>([]);
const ateVariableHeaders = ref<string[]>([]);

const showWIS = ref(true);
const checkConsistency = ref(false);
const wisTable = ref<any[]>([]);
const calculateWisByPercentage = ref(true);
const wisVariableHeaders = ref<string[]>([]);
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
	if (knobs.value.selectedCompareOption === CompareValue.SCENARIO) {
		constructATETable();
	}
};

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

const impactChartData = computed<ChartData | null>(() =>
	buildChartData(datasets.value, datasetResults.value, baselineDatasetIndex.value, selectedPlotType.value)
);
const rankingChartData = computed<ChartData | null>(() =>
	buildChartData(datasets.value, datasetResults.value, baselineDatasetIndex.value, PlotValue.VALUE)
);
const chartData = computed(() => {
	if (knobs.value.selectedCompareOption === CompareValue.RANK) {
		return rankingChartData.value;
	}
	return impactChartData.value;
});

const variableNames = ref<string[]>([]);
const mappingOptions = ref<Record<string, string[]>>({});

const criteriaOfInterestCards = computed(() => knobs.value.criteriaOfInterestCards);

const {
	generateAnnotation,
	getChartLabels,
	getChartAnnotationsByChartId,
	useCompareDatasetCharts,
	useInterventionRankingCharts
} = useCharts(
	props.node.id,
	computed(() => models.value[0]),
	modelConfigurations,
	chartData,
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
	baselineDatasetIndex,
	datasets,
	interventionPolicies
);

const rankingCharts = useInterventionRankingCharts(criteriaOfInterestCards, datasets, interventionPolicies);

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
	constructWisTable();
}

function deleteMapRow(index: number) {
	knobs.value.mapping.splice(index, 1);
	constructWisTable();
}

// TODO: Investigate sharing similar logic between constructing ate and wis tables since they are very similar
// It may or may not be a good idea
function constructWisTable() {
	if (knobs.value.selectedGroundTruthDatasetId === null) return;
	const selectedGroundTruthDatasetId = knobs.value.selectedGroundTruthDatasetId;

	wisTable.value = [];
	wisVariableHeaders.value = [];

	let isConsistent = true;
	const observationsMap: Record<number, number> = {};
	const variableToTypeMap: Record<string, string> = {};

	const variablesOfInterest = [
		...new Set([...selectedVariableNames.value, ...knobs.value.mapping.map((m) => Object.values(m)).flat()])
	];

	const summaryResults = [
		...(datasetResults.value?.summaryResults ?? []),
		...(datasetResults.value?.datasetResults ?? []) // Regular dataset asset
	];

	summaryResults.forEach((summaryResult) => {
		Object.keys(summaryResult[0]).forEach((key) => {
			if (
				(!key.includes('data/') && (key.includes('_param_') || !key.includes('_mean:'))) ||
				// Skip if the variable is not selected in output settings or attached to the ground truth dataset in your mapping
				!variablesOfInterest.some((variableName) => {
					if (key.includes(variableName)) {
						if (!variableToTypeMap[variableName]) {
							variableToTypeMap[variableName] = key.includes('_observable_state_') ? '_observable_state_' : '_state_';
							wisVariableHeaders.value.push(variableName);
						}
						return true;
					}
					return false;
				})
			) {
				return;
			}
			const observations = computeQuantile(summaryResult, key);
			observationsMap[key] = observations;
		});
	});

	const observationsKeyNames = Object.keys(observationsMap);

	datasets.value.forEach((dataset, index) => {
		if (index === groundTruthDatasetIndex.value) return;

		const wisRow: Record<string, number> = {};
		const wisValues: number[] = [];

		Object.entries(variableToTypeMap).forEach(([variableName, type]) => {
			let key = `${variableName}${type}mean:${index}`;
			if (!observationsKeyNames.includes(key)) {
				key = `data/${variableName}:${index}`; // Check if it's from a regular dataset asset
				if (!observationsKeyNames.includes(key)) {
					return;
				}
			}

			const datasetMapping = knobs.value.mapping.find((m) => Object.values(m).includes(variableName));
			if (!datasetMapping) return;

			const groundTruthVariableName = datasetMapping[selectedGroundTruthDatasetId];
			let groundTruthKey = `${groundTruthVariableName}${type}mean:${groundTruthDatasetIndex.value}`;

			if (!observationsMap[groundTruthKey]) {
				groundTruthKey = `data/${groundTruthVariableName}:${groundTruthDatasetIndex.value}`; // Check if it's from a regular dataset asset
				if (!observationsMap[groundTruthKey]) {
					return;
				}
			}

			const wis = getWeightedIntervalScore(
				observationsMap[groundTruthKey],
				observationsMap[key],
				calculateWisByPercentage.value,
				checkConsistency.value
			);

			if (!isConsistent) {
				isConsistent = wis.isConsistent;
			}

			const totalMean = mean(wis.total);

			// FIXME: For now I am assigning to the value to the ground truth column and the variable column
			// The table columns that end up actually appearing are the variables chosen in the output settings
			// But the ground truth may not necessarily match up with what's chosen in the output settings
			// So this is kind of a lazy solution that'll always work, (later we'll see how we exactly want to sync the mapping and the output settings selector)
			wisRow[groundTruthVariableName] = totalMean;
			wisRow[variableName] = totalMean;

			wisValues.push(totalMean);
		});
		wisRow.overall = mean(wisValues);
		wisTable.value.push({ modelName: dataset.name, ...wisRow });
	});

	if (!isConsistent) {
		logger.error('Left quantile must be smaller than right quantile. Datasets are not ideal for WIS calculation.');
	}
}

onMounted(async () => {
	const state = cloneDeep(props.node.state);
	knobs.value = Object.assign(knobs.value, state);

	outputPanelBehavior();

	datasets.value = await initialize(
		props.node,
		knobs,
		isFetchingDatasets,
		datasetResults,
		modelConfigIdToInterventionPolicyIdMap,
		modelConfigurations,
		interventionPolicies,
		models
	);

	// Prepare variable dropdowns
	let allVariableNames: string[] = [];
	if (impactChartData.value) {
		allVariableNames = Object.keys(impactChartData.value.pyciemssMap);
		variableNames.value = allVariableNames.filter(
			(key) => !['timepoint_id', 'sample_id', 'timepoint_unknown'].includes(key)
		);
	}

	const swappedPyCiemssMap: Record<string, string> = {};
	Object.entries(impactChartData.value?.pyciemssMap ?? {}).forEach(([key, value]) => {
		swappedPyCiemssMap[value] = key;
	});
	const pyciemssNames = Object.keys(swappedPyCiemssMap);

	datasets.value.forEach((dataset) => {
		const datasetId = dataset.id as string;
		mappingOptions.value[datasetId] = [];

		if (!dataset.columns) return;
		dataset.columns.forEach((column) => {
			if (!column.name || column.fileName !== getFileName(dataset)) return;

			let option = '';
			if (pyciemssNames.includes(column.name)) option = swappedPyCiemssMap[column.name];
			else if (pyciemssNames.includes(`data/${column.name}`)) option = swappedPyCiemssMap[`data/${column.name}`];
			if (!option) return;

			mappingOptions.value[datasetId].push(option);
		});
	});

	// Construct tables
	constructATETable();

	if (isEmpty(knobs.value.mapping)) addMapping();

	constructWisTable();
});

onBeforeUnmount(async () => {
	// flush changes
	const clonedState = cloneDeep(props.node.state);
	clonedState.criteriaOfInterestCards = knobs.value.criteriaOfInterestCards;
	clonedState.selectedCompareOption = knobs.value.selectedCompareOption;
	clonedState.selectedBaselineDatasetId = knobs.value.selectedBaselineDatasetId;
	clonedState.selectedGroundTruthDatasetId = knobs.value.selectedGroundTruthDatasetId;
	clonedState.selectedPlotType = knobs.value.selectedPlotType;
	clonedState.mapping = knobs.value.mapping;

	emit('update-state', clonedState);
});
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
