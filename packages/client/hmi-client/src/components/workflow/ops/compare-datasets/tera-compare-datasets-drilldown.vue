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
					<tera-drilldown-section class="px-2">
						<Button class="ml-auto" size="small" label="Run" @click="onRun" />
						<label>What do you want to compare?</label>
						<Dropdown
							v-model="knobs.selectedCompareOption"
							:options="compareOptions"
							option-label="label"
							option-value="value"
						/>
						<tera-checkbox
							class="pt-2"
							v-model="isSimulationsFromSameModel"
							label="All simulations are from the same model"
							disabled
						/>

						<template v-if="knobs.selectedCompareOption === CompareValue.IMPACT">
							<label> Select simulation to use as a baseline (optional) </label>
							<Dropdown
								v-model="knobs.selectedDataset"
								:options="datasets"
								option-label="name"
								option-value="id"
								:loading="isFetchingDatasets"
								placeholder="Optional"
								@change="createCharts"
							/>

							<label>Comparison tables</label>
							<tera-checkbox
								v-model="isATESelected"
								label="Average treatment effect (ATE)"
								subtext="Description for ATE."
							/>
						</template>
						<label class="mt-2">Timepoint column</label>
						<Dropdown
							v-model="timepointHeaderName"
							:options="commonHeaderNames"
							placeholder="Select timepoint header"
							@change="createCharts"
						/>
						<template v-if="knobs.selectedCompareOption === CompareValue.RANK">
							<label>Specifiy criteria of interest</label>
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
			<Accordion multiple :active-index="activeIndices">
				<AccordionTab header="Summary"> </AccordionTab>
				<AccordionTab header="Variables">
					<template v-for="(compareChart, index) in selectedCharts">
						<vega-chart
							v-if="!isEmpty(compareChart)"
							:key="index"
							:visualization-spec="compareChart"
							:are-embed-actions-visible="false"
							expandable
						/>
					</template>
				</AccordionTab>
				<AccordionTab header="Comparison table"> </AccordionTab>
			</Accordion>
		</tera-drilldown-section>

		<tera-drilldown-section :tabName="DrilldownTabs.Notebook"> </tera-drilldown-section>

		<template #sidebar-right>
			<tera-slider-panel
				class="input-config"
				v-model:is-open="isOutputSettingsOpen"
				header="Output settings"
				content-width="360px"
			>
				<template #content>
					<div class="output-settings-panel">
						<tera-chart-control
							class="w-full"
							:chart-config="{
								selectedRun: 'fixme',
								selectedVariable: selectedVariableSettings.map((s) => s.selectedVariables[0])
							}"
							multi-select
							:show-remove-button="false"
							:variables="commonHeaderNames"
							@configuration-change="updateChartSettings($event.selectedVariable, ChartSettingType.VARIABLE)"
						/>
						<tera-chart-settings-item
							v-for="settings of chartSettings.filter((setting) => setting.type === ChartSettingType.VARIABLE)"
							:key="settings.id"
							:settings="settings"
							@open="activeChartSettings = settings"
							@remove="removeChartSettings"
						/>
						<label>How do you want to plot the values?</label>
						<div v-for="option in plotOptions" class="flex align-items-center" :key="option.value">
							<RadioButton
								v-model="knobs.selectedPlotType"
								:value="option.value"
								name="plotValues"
								@change="createCharts"
							/>
							<label class="pl-2 py-1" :for="option.value">{{ option.label }}</label>
						</div>
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
import { DrilldownTabs, ChartSettingType, type ChartSetting } from '@/types/common';
import { onMounted, ref, watch, computed } from 'vue';
import Button from 'primevue/button';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Dropdown from 'primevue/dropdown';
import { Dataset } from '@/types/Types';
import { getDataset, getRawContent } from '@/services/dataset';
import TeraCheckbox from '@/components/widgets/tera-checkbox.vue';
import RadioButton from 'primevue/radiobutton';
import { isEmpty, cloneDeep, capitalize, isEqual } from 'lodash';
import VegaChart from '@/components/widgets/VegaChart.vue';
import { createForecastChart, AUTOSIZE } from '@/services/charts';
import TeraChartSettingsItem from '@/components/widgets/tera-chart-settings-item.vue';
import TeraChartControl from '@/components/workflow/tera-chart-control.vue';
import { useChartSettings } from '@/composables/useChartSettings';
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

const commonHeaderNames = ref<string[]>([]);
const timepointHeaderName = ref<string | null>(null);

const plotOptions = [
	{ label: 'Compare trajectories', value: PlotValue.VALUE },
	{ label: 'Percent change', value: PlotValue.PERCENTAGE },
	{ label: 'Difference', value: PlotValue.DIFFERENCE }
];

const isInputSettingsOpen = ref(true);
const isOutputSettingsOpen = ref(true);
const activeIndices = ref([0, 1, 2]);

const isFetchingDatasets = ref(false);
const isSimulationsFromSameModel = ref(true);
const isATESelected = ref(false);

const compareCharts = ref<any[]>([]);

const { activeChartSettings, chartSettings, selectedVariableSettings, removeChartSettings, updateChartSettings } =
	useChartSettings(props, emit);

const selectedCharts = computed(() => {
	const selectedChartIds = selectedVariableSettings.value.map((setting) => setting.selectedVariables[0]);
	return compareCharts.value.filter((chart) => selectedChartIds.includes(chart.title.text));
});

const onRun = () => {
	console.log('run');
};

interface BasicKnobs {
	criteriaOfInterestCards: CriteriaOfInterestCard[];
	selectedPlotType: PlotValue;
	selectedCompareOption: CompareValue;
	selectedDataset: string | null;
	chartSettings: ChartSetting[] | null;
}

const addCriteria = () => {
	knobs.value.criteriaOfInterestCards.push(blankCriteriaOfInterest);
};

const deleteCriteria = (index: number) => {
	knobs.value.criteriaOfInterestCards.splice(index, 1);
};

const updateCriteria = (card: Partial<CriteriaOfInterestCard>, index: number) => {
	Object.assign(knobs.value.criteriaOfInterestCards[index], card);
};

const knobs = ref<BasicKnobs>({
	criteriaOfInterestCards: [],
	selectedPlotType: PlotValue.PERCENTAGE,
	selectedCompareOption: CompareValue.IMPACT,
	selectedDataset: null,
	chartSettings: null
});

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
	isFetchingDatasets.value = false;

	if (!knobs.value.selectedDataset) knobs.value.selectedDataset = datasets.value[0]?.id ?? null;

	createCharts();
};

// Following two funcs are util like
function transposeArrays(arrays) {
	if (arrays.length === 0) return [];
	return arrays[0].map((_, colIndex) => arrays.map((row) => row[colIndex]));
}

function findDuplicates(strings: string[]): string[] {
	const duplicates: string[] = [];
	const seen: Record<string, number> = {};

	strings.forEach((str) => {
		if (seen[str]) {
			seen[str]++;
		} else {
			seen[str] = 1;
		}
	});
	Object.keys(seen).forEach((key) => {
		if (seen[key] > 1) {
			duplicates.push(key);
		}
	});
	return duplicates;
}

async function createCharts() {
	// FIXME: Temporary check, useChartSettings updates the state directly but the knobs are not updated
	// If this isn't synced the chartSettings will be reverted to the previous state
	if (!isEqual(props.node.state.chartSettings, knobs.value.chartSettings)) {
		knobs.value.chartSettings = props.node.state.chartSettings;
	}

	if (datasets.value.length <= 1) return;
	compareCharts.value = [];

	const rawContents = await Promise.all(datasets.value.map((dataset) => getRawContent(dataset)));
	const transposedRawContents = rawContents.map((content) => ({ ...content, csv: transposeArrays(content?.csv) }));

	// Collect common header names if not done yet
	if (isEmpty(commonHeaderNames.value)) {
		const allColumnNames: string[] = [];
		transposedRawContents.forEach(({ headers }) => {
			if (headers) allColumnNames.push(...headers);
		});
		commonHeaderNames.value = findDuplicates(allColumnNames);

		// Convenient assumption that the timepoint header name starts with 't'
		timepointHeaderName.value =
			commonHeaderNames.value?.find((name) => name.toLowerCase().slice(0, 1) === 't') ?? commonHeaderNames.value[0];
	}
	if (!timepointHeaderName.value) return;

	// Find index of timepoint column
	const timepointIndex = transposedRawContents[0]?.headers?.indexOf(timepointHeaderName.value);
	if (timepointIndex === undefined || timepointIndex === -1) return;

	// Find dataset index of the selected dataset
	const selectedIndex = datasets.value.findIndex((dataset) => dataset.id === knobs.value.selectedDataset);
	if (selectedIndex === -1) return;

	const { selectedPlotType } = knobs.value;

	// Go through every common header (column loop)
	commonHeaderNames.value?.forEach((headerName) => {
		if (headerName === timepointHeaderName.value) return;

		const headerIndex = transposedRawContents[0]?.headers?.indexOf(headerName);
		if (!headerIndex) return;

		const data: any = [];
		const variableNames: string[] = [];
		const referenceColumn = transposedRawContents[selectedIndex].csv[headerIndex]; // aka the column of baseline dataset we are subtracting from

		transposedRawContents.forEach(({ csv }, datasetIndex) => {
			const timepoints = csv[timepointIndex];
			const columnToSubtract = csv[headerIndex];

			const name = `${headerName}_${datasets.value[datasetIndex].name}`;
			variableNames.push(name);

			referenceColumn.forEach((referencePoint: number, index: number) => {
				let value = 0;
				if (selectedPlotType === PlotValue.DIFFERENCE) {
					value = referencePoint - columnToSubtract[index]; // difference
				} else if (selectedPlotType === PlotValue.PERCENTAGE) {
					value = ((referencePoint - columnToSubtract[index]) / referencePoint) * 100; // percentage
				} else if (selectedPlotType === PlotValue.VALUE) {
					value = parseFloat(columnToSubtract[index]); // trajectory
				}
				if (data[index] === undefined) {
					data.push({
						[name]: value,
						timepoint: parseFloat(timepoints[index])
					});
				} else {
					data[index][name] = value;
				}
			});
		});
		compareCharts.value.push(
			createForecastChart(
				null,
				{
					data,
					variables: variableNames,
					timeField: 'timepoint'
				},
				null,
				{
					title: headerName,
					xAxisTitle: 'Timepoint',
					yAxisTitle: capitalize(selectedPlotType),
					width: 600,
					height: 300,
					legend: true,
					autosize: AUTOSIZE.FIT
				}
			)
		);
	});
}

onMounted(() => {
	initialize();
});

watch(
	() => knobs.value,
	() => {
		const state = cloneDeep(props.node.state);
		Object.assign(state, knobs.value);
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
</style>
