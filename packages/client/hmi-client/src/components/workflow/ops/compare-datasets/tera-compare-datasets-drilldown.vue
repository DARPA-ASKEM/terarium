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
				content-width="400px"
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
						<div class="flex flex-column gap-2" v-if="knobs.selectedCompareOption === CompareValue.RANK">
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
					</tera-drilldown-section>
				</template>
			</tera-slider-panel>
		</template>

		<tera-drilldown-section :tabName="DrilldownTabs.Wizard">
			<div ref="outputPanel" class="p-2">
				<Accordion multiple :active-index="activeIndices">
					<AccordionTab header="Summary"> </AccordionTab>
					<template v-if="knobs.selectedCompareOption === CompareValue.SCENARIO">
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
						<AccordionTab header="Impact of intervention metrics" v-if="showATETable">
							<p class="mb-3">
								The average treatment effect (ATE) estimates the impact of a policy on the outcome in a given
								population; it is the mean difference in the outcome between those who were and weren't treated. Larger
								values of ATE are better, meaning a very impactful policy. Reference:
								<a target="_blank" rel="noopener noreferrer" href="https://pmc.ncbi.nlm.nih.gov/articles/PMC6794006/">
									https://pmc.ncbi.nlm.nih.gov/articles/PMC6794006/
								</a>
							</p>
							<DataTable :value="ateTable">
								<Column field="policyName" header="Intervention policy" />
								<Column
									v-for="variableName in [...variableNames, 'overall']"
									:field="variableName"
									:header="variableName === 'overall' ? 'Overall' : variableName"
									sortable
									:key="variableName"
								>
									<template #body="{ data, field }">
										<div class="flex gap-2">
											<div>{{ displayNumber(data[field]) }}</div>
											<div v-if="showATEErrors" class="error ml-auto">
												± {{ displayNumber(data[`${field}_error`]) }}
											</div>
										</div>
									</template>
								</Column>
							</DataTable>
						</AccordionTab>
					</template>
					<template v-else>
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
				<template #content v-if="knobs.selectedCompareOption === CompareValue.SCENARIO">
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
						<Divider />
						<tera-chart-settings-quantiles :settings="chartSettings" @update-options="updateQauntilesOptions" />
						<Divider />
						<h5>Impact of intervention metrics</h5>
						<tera-checkbox v-model="showATETable" label="Average treatment effect (ATE)" />
						<tera-checkbox v-if="showATETable" v-model="showATEErrors" label="Show errors" />
					</div>
				</template>
			</tera-slider-panel>
		</template>
	</tera-drilldown>
</template>

<script setup lang="ts">
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
import { Dataset, InterventionPolicy, ModelConfiguration } from '@/types/Types';
import TeraCheckbox from '@/components/widgets/tera-checkbox.vue';
import RadioButton from 'primevue/radiobutton';
import { cloneDeep } from 'lodash';
import VegaChart from '@/components/widgets/VegaChart.vue';
import { deleteAnnotation } from '@/services/chart-settings';
import TeraChartSettings from '@/components/widgets/tera-chart-settings.vue';
import TeraChartSettingsPanel from '@/components/widgets/tera-chart-settings-panel.vue';
import TeraChartSettingsQuantiles from '@/components/widgets/tera-chart-settings-quantiles.vue';
import { useChartSettings } from '@/composables/useChartSettings';
import { useDrilldownChartSize } from '@/composables/useDrilldownChartSize';
import { useCharts, type ChartData } from '@/composables/useCharts';
import { DataArray } from '@/services/models/simulation-service';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import { mean, stddev } from '@/utils/stats';
import { displayNumber } from '@/utils/number';
import TeraCriteriaOfInterestCard from './tera-criteria-of-interest-card.vue';
import {
	blankCriteriaOfInterest,
	CompareDatasetsState,
	CompareValue,
	CriteriaOfInterestCard,
	PlotValue
} from './compare-datasets-operation';
import { generateRankingCharts, generateImpactCharts, initialize } from './compare-datasets-utils';

const props = defineProps<{
	node: WorkflowNode<CompareDatasetsState>;
}>();

const emit = defineEmits(['update-state', 'close']);

const compareOptions: { label: string; value: CompareValue }[] = [
	{ label: 'Compare scenarios', value: CompareValue.SCENARIO },
	{ label: 'Rank interventions based on multiple criteria', value: CompareValue.RANK }
];

const datasets = ref<Dataset[]>([]);
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
	generateRankingCharts(
		rankingCriteriaCharts,
		rankingResultsChart,
		props.node,
		rankingChartData,
		datasets,
		modelConfigurations,
		interventionPolicies
	);
};

function onChangeImpactComparison() {
	generateImpactCharts(impactChartData, datasets, datasetResults, baselineDatasetIndex, selectedPlotType);
}

interface BasicKnobs {
	criteriaOfInterestCards: CriteriaOfInterestCard[];
	selectedCompareOption: CompareValue;
	selectedBaselineDatasetId: string | null;
	selectedPlotType: PlotValue;
}

const knobs = ref<BasicKnobs>({
	criteriaOfInterestCards: [],
	selectedCompareOption: CompareValue.SCENARIO,
	selectedBaselineDatasetId: null,
	selectedPlotType: PlotValue.PERCENTAGE
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
const variableCharts = useCompareDatasetCharts(selectedVariableSettings, selectedPlotType, baselineDatasetIndex);

function outputPanelBehavior() {
	if (knobs.value.selectedCompareOption === CompareValue.RANK) {
		isOutputSettingsOpen.value = false;
	} else if (knobs.value.selectedCompareOption === CompareValue.SCENARIO) {
		isOutputSettingsOpen.value = true;
	}
}

function constructATETable() {
	const means: Record<string, number> = {};
	const meanErrors: Record<string, number> = {};

	datasetResults.value?.summaryResults.forEach((summaryResult) => {
		Object.keys(summaryResult[0]).forEach((key) => {
			if (!key.includes('_mean:')) return;
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

		variableNames.value.forEach((variableName) => {
			const potentialKeys = [
				`${variableName}_state_mean:${index}`,
				`persistent_${variableName}_param_mean:${index}`,
				`${variableName}_observable_state_mean:${index}`
			];

			potentialKeys.forEach((key) => {
				if (!ateRow[variableName] && meanKeyNames.includes(key)) {
					const baselineKey = `${key.slice(0, -1)}${baselineDatasetIndex.value}`;

					const ate = means[key] - means[baselineKey];
					const ateError = Math.sqrt(meanErrors[key] ** 2 + meanErrors[baselineKey] ** 2);

					ateRow[variableName] = ate;
					ateRow[`${variableName}_error`] = ateError;

					ateValues.push(ate);
				}
			});
		});
		ateRow.overall = mean(ateValues);
		ateRow.overall_error = stddev(ateValues) / Math.sqrt(ateValues.length);

		ateTable.value.push({ policyName: dataset.name, ...ateRow });
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
});

watch(
	() => knobs.value,
	() => {
		const state = cloneDeep(props.node.state);
		state.criteriaOfInterestCards = knobs.value.criteriaOfInterestCards;
		state.selectedCompareOption = knobs.value.selectedCompareOption;
		state.selectedBaselineDatasetId = knobs.value.selectedBaselineDatasetId;
		state.selectedPlotType = knobs.value.selectedPlotType;
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
</style>
