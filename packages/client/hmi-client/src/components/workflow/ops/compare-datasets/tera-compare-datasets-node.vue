<template>
	<section>
		<tera-node-preview
			v-if="node.state.selectedCompareOption !== CompareValue.RANK"
			:node="node"
			:prepared-charts="comparisonCharts"
			:is-loading="isFetchingDatasets"
			:chart-settings="selectedVariableSettings"
			:placeholder="'Attach datasets/simulation outputs to compare'"
		/>
		<template v-else>
			<vega-chart
				v-if="rankingCharts.rankingResultsChart"
				expandable
				are-embed-actions-visible
				:visualization-spec="rankingCharts.rankingResultsChart"
				:interactive="false"
			/>
			<vega-chart
				v-for="(spec, index) in rankingCharts.rankingCriteriaCharts"
				:key="index"
				expandable
				are-embed-actions-visible
				:visualization-spec="spec"
				:interactive="false"
			/>
		</template>
		<Button v-if="hasAtLeastTwoInputs" label="Open" @click="emit('open-drilldown')" severity="secondary" outlined />
	</section>
</template>

<script setup lang="ts">
import { computed, ref, toRef, watch, onMounted } from 'vue';
import Button from 'primevue/button';
import { Dataset, InterventionPolicy, Model, ModelConfiguration } from '@/types/Types';
import { DataArray } from '@/services/models/simulation-service';
import { type WorkflowNode, WorkflowPortStatus } from '@/types/workflow';
import { useCharts, type ChartData } from '@/composables/useCharts';
import { useChartSettings } from '@/composables/useChartSettings';
import VegaChart from '@/components/widgets/VegaChart.vue';
import TeraNodePreview from '../tera-node-preview.vue';
import { CompareDatasetsState, CompareValue, PlotValue } from './compare-datasets-operation';
import { buildChartData, initialize } from './compare-datasets-utils';

const props = defineProps<{
	node: WorkflowNode<CompareDatasetsState>;
}>();

const emit = defineEmits(['append-input-port', 'open-drilldown']);

const hasAtLeastTwoInputs = computed(() => props.node.inputs.filter((input) => input.value).length >= 2);

const { selectedVariableSettings } = useChartSettings(props, emit);
const isFetchingDatasets = ref(false);
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
const impactChartData = computed<ChartData | null>(() =>
	buildChartData(datasets.value, datasetResults.value, baselineDatasetIndex.value, selectedPlotType.value)
);
const rankingChartData = computed<ChartData | null>(() =>
	buildChartData(datasets.value, datasetResults.value, baselineDatasetIndex.value, PlotValue.VALUE)
);

const chartData = computed(() => {
	if (props.node.state.selectedCompareOption === CompareValue.RANK) {
		return rankingChartData.value;
	}
	return impactChartData.value;
});

const selectedPlotType = computed(() => props.node.state.selectedPlotType);
const baselineDatasetIndex = computed(() =>
	datasets.value.findIndex((dataset) => dataset.id === props.node.state.selectedBaselineDatasetId)
);

const criteriaOfInterestCards = computed(() => props.node.state.criteriaOfInterestCards);
const { useCompareDatasetCharts, useInterventionRankingCharts } = useCharts(
	props.node.id,
	computed(() => models.value[0]),
	modelConfigurations,
	chartData,
	toRef({ width: 180, height: 120 }),
	null,
	null
);
const comparisonCharts = useCompareDatasetCharts(
	selectedVariableSettings,
	selectedPlotType,
	baselineDatasetIndex,
	datasets,
	interventionPolicies
);

const rankingCharts = useInterventionRankingCharts(criteriaOfInterestCards, datasets, interventionPolicies);

onMounted(async () => {
	datasets.value = await initialize(
		props.node,
		null,
		isFetchingDatasets,
		datasetResults,
		modelConfigIdToInterventionPolicyIdMap,
		modelConfigurations,
		interventionPolicies,
		models
	);
});

watch(
	() => props.node.inputs,
	async () => {
		if (props.node.inputs.every((input) => input.status === WorkflowPortStatus.CONNECTED)) {
			emit('append-input-port', { type: 'datasetId', label: 'Dataset or Simulation result' });
		}
		datasets.value = await initialize(
			props.node,
			null,
			isFetchingDatasets,
			datasetResults,
			modelConfigIdToInterventionPolicyIdMap,
			modelConfigurations,
			interventionPolicies,
			models
		);
	},
	{ deep: true }
);
</script>
