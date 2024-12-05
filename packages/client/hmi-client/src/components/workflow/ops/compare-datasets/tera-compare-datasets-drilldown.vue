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
					<template v-for="(compareChart, index) in compareCharts">
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
					<tera-drilldown-section class="px-2">
						<label>What values do you want to plot?</label>
						<div v-for="option in plotOptions" class="flex align-items-center" :key="option.value">
							<RadioButton v-model="knobs.selectedPlotValue" :value="option.value" name="plotValues" />
							<label class="pl-2 py-1" :for="option.value">{{ option.label }}</label>
						</div>
					</tera-drilldown-section>
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
import { DrilldownTabs } from '@/types/common';
import { onMounted, ref, watch } from 'vue';
import Button from 'primevue/button';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Dropdown from 'primevue/dropdown';
import { Dataset } from '@/types/Types';
import { getDataset, getRawContent } from '@/services/dataset';
import TeraCheckbox from '@/components/widgets/tera-checkbox.vue';
import RadioButton from 'primevue/radiobutton';
import { isEmpty, cloneDeep } from 'lodash';
import VegaChart from '@/components/widgets/VegaChart.vue';
import { createDatasetCompareChart } from '@/services/charts';
import {
	blankCriteriaOfInterest,
	CompareDatasetsState,
	CompareValue,
	CriteriaOfInterestCard,
	PlotValue
} from './compare-datasets-operation';
import TeraCriteriaOfInterestCard from './tera-criteria-of-interest-card.vue';

// const props =
const props = defineProps<{
	node: WorkflowNode<CompareDatasetsState>;
}>();

const compareOptions: { label: string; value: CompareValue }[] = [
	{ label: 'Compare the impact of interventions', value: CompareValue.IMPACT },
	{ label: 'Rank interventions based on multiple charts', value: CompareValue.RANK }
];

const datasets = ref<Dataset[]>([]);

const commonHeaderNames = ref<string[]>([]);
const timepointHeaderName = ref<string | null>(null);

const plotOptions = [
	{ label: 'Percent change', value: 'percentage' },
	{ label: 'Absolute difference', value: 'value' }
];

const isInputSettingsOpen = ref(true);
const isOutputSettingsOpen = ref(true);
const activeIndices = ref([0, 1, 2]);

const isFetchingDatasets = ref(false);
const isSimulationsFromSameModel = ref(true);
const isATESelected = ref(false);

const compareCharts = ref<any[]>([]);

const onRun = () => {
	console.log('run');
};

const emit = defineEmits(['update-state', 'update-status', 'close']);

interface BasicKnobs {
	criteriaOfInterestCards: CriteriaOfInterestCard[];
	selectedPlotValue: PlotValue;
	selectedCompareOption: CompareValue;
	selectedDataset: string | null;
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
	selectedPlotValue: PlotValue.PERCENTAGE,
	selectedCompareOption: CompareValue.IMPACT,
	selectedDataset: null
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
	if (datasets.value.length <= 1) return;
	compareCharts.value = [];

	const rawContents = await Promise.all(datasets.value.map((dataset) => getRawContent(dataset)));
	const transposedRawContents = rawContents.map((content) => ({ ...content, csv: transposeArrays(content?.csv) }));

	if (isEmpty(commonHeaderNames.value)) {
		const allColumnNames: string[] = [];
		datasets.value.forEach((dataset) => {
			const columnNames = dataset?.columns?.map((column) => column.name) ?? [];
			allColumnNames.push(...columnNames);
		});
		commonHeaderNames.value = findDuplicates(allColumnNames);

		// Convenient assumption that the timepoint header name contains 't'
		timepointHeaderName.value =
			commonHeaderNames.value?.find((name) => name.toLowerCase().includes('t')) ?? commonHeaderNames.value[0];
	}

	if (!timepointHeaderName.value) return;

	const timepointIndex = transposedRawContents[0]?.headers?.indexOf(timepointHeaderName.value);
	if (timepointIndex === undefined) return;

	// Go through every column header
	commonHeaderNames.value?.forEach((headerName) => {
		if (headerName === timepointHeaderName.value) return;

		const headerIndex = transposedRawContents[0]?.headers?.indexOf(headerName);
		if (!headerIndex) return;

		const values: any = [];
		// Find dataset index of the selected dataset
		const selectedIndex = datasets.value.findIndex((dataset) => dataset.id === knobs.value.selectedDataset);
		const referenceColumn = transposedRawContents[selectedIndex].csv[headerIndex]; // aka the baseline dataset

		transposedRawContents.forEach((content, datasetIndex) => {
			const timepoints = content.csv[timepointIndex];
			timepoints.forEach((timepoint: number, rowIndex: number) => {
				const referencePoint = parseFloat(referenceColumn[rowIndex]);
				const currentPoint = parseFloat(content.csv[headerIndex][rowIndex]) + datasetIndex; // Adding datasetIndex is just to temporarily differentiate the datasets
				const absoluteDifference = Math.abs(referencePoint - currentPoint);

				values.push({
					timepoint,
					value: absoluteDifference,
					name: `${headerName}_${datasets.value[datasetIndex].name}`
				});
			});
		});

		compareCharts.value.push(createDatasetCompareChart(values, headerName));
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
</style>
