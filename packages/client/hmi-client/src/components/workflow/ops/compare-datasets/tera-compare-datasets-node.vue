<template>
	<section>
		<NodeCharts
			:prepared-charts="comparisonCharts"
			:is-loading="isFetchingDatasets"
			:node="node"
			:selectedVariableSettings="selectedVariableSettings"
		/>
		<Button v-if="hasAtLeastTwoInputs" label="Open" @click="emit('open-drilldown')" severity="secondary" outlined />
	</section>
</template>

<script setup lang="ts">
import { computed, ref, toRef, watch, onMounted } from 'vue';
import Button from 'primevue/button';
import { isEmpty } from 'lodash';

import { Dataset, InterventionPolicy, ModelConfiguration } from '@/types/Types';
import { getDataset } from '@/services/dataset';
import { getInterventionPolicyById } from '@/services/intervention-policy';
import { getModelConfigurationById } from '@/services/model-configurations';
import { DataArray } from '@/services/models/simulation-service';
import { type WorkflowNode, WorkflowPortStatus } from '@/types/workflow';

import { useCharts, type ChartData } from '@/composables/useCharts';
import { useChartSettings } from '@/composables/useChartSettings';
import NodeCharts from '../node-charts.vue';

import { CompareDatasetsState, PlotValue } from './compare-datasets-operation';

import { fetchDatasetResults, generateImpactCharts, generateRankingCharts } from './compare-datasets-utils';

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
const modelConfigurations = ref<ModelConfiguration[]>([]);
const interventionPolicies = ref<InterventionPolicy[]>([]);
const modelConfigIdToInterventionPolicyIdMap = ref<Record<string, string[]>>({});
const chartData = ref<ChartData | null>(null);
const rankingResultsChart = ref<any>(null);
const rankingCriteriaCharts = ref<any>([]);

const selectedPlotType = computed(() => PlotValue.PERCENTAGE);
const baselineDatasetIndex = computed(() =>
	datasets.value.findIndex((dataset) => dataset.id === props.node.state.selectedDataset)
);
const baselineName = computed(
	() => datasets.value.find((dataset) => dataset.id === props.node.state.selectedDataset)?.name ?? null
);

const { useCompareDatasetCharts } = useCharts(
	props.node.id,
	null,
	null,
	chartData,
	toRef({ width: 180, height: 120 }),
	null,
	null
);
onMounted(() => {
	initialize();
});
const comparisonCharts = useCompareDatasetCharts(
	selectedVariableSettings,
	selectedPlotType,
	baselineName /* may have selected baseline, revisit later */
);
console.log(comparisonCharts.value);
// loading datasets, find way to reduce duplication with dd here
const initialize = async () => {
	const { inputs } = props.node;
	const datasetInputs = inputs.filter(
		(input) => input.type === 'datasetId' && input.status === WorkflowPortStatus.CONNECTED
	);
	const datasetPromises = datasetInputs.map((input) => getDataset(input.value![0]));
	isFetchingDatasets.value = true;
	await Promise.all(datasetPromises).then((ds) => {
		ds.forEach((dataset) => {
			// Add dataset
			if (!dataset) return;
			datasets.value.push(dataset);

			// Collect model configuration id and intervention policy id
			const modelConfigurationId: string | undefined = dataset.metadata?.simulationAttributes?.modelConfigurationId;
			const interventionPolicyId: string | undefined = dataset.metadata?.simulationAttributes?.interventionPolicyId;

			if (!modelConfigurationId) return;
			if (!modelConfigIdToInterventionPolicyIdMap.value[modelConfigurationId]) {
				modelConfigIdToInterventionPolicyIdMap.value[modelConfigurationId] = [];
			}
			if (!interventionPolicyId) return;
			modelConfigIdToInterventionPolicyIdMap.value[modelConfigurationId].push(interventionPolicyId);
		});
	});
	// Fetch the results
	datasetResults.value = await fetchDatasetResults(datasets.value);
	isFetchingDatasets.value = false;

	await generateImpactCharts(chartData, datasets, datasetResults, baselineDatasetIndex, selectedPlotType);
	console.log(chartData.value);
	const modelConfigurationIds = Object.keys(modelConfigIdToInterventionPolicyIdMap.value);
	if (isEmpty(modelConfigurationIds)) return;
	const modelConfigurationPromises = modelConfigurationIds.map((id) => getModelConfigurationById(id));
	await Promise.all(modelConfigurationPromises).then((configs) => {
		modelConfigurations.value = configs.filter((config) => config !== null);
	});

	const interventionPolicyIds = Object.values(modelConfigIdToInterventionPolicyIdMap.value).flat();
	if (isEmpty(interventionPolicyIds)) return;
	const interventionPolicyPromises = interventionPolicyIds.map((id) => getInterventionPolicyById(id));
	await Promise.all(interventionPolicyPromises).then((policies) => {
		interventionPolicies.value = policies.filter((policy) => policy !== null);
	});

	generateRankingCharts(
		rankingCriteriaCharts,
		rankingResultsChart,
		props,
		modelConfigIdToInterventionPolicyIdMap,
		chartData,
		interventionPolicies
	);
};

watch(
	() => props.node.inputs,
	() => {
		if (props.node.inputs.every((input) => input.status === WorkflowPortStatus.CONNECTED)) {
			emit('append-input-port', { type: 'datasetId', label: 'Dataset or Simulation result' });
		}
	},
	{ deep: true }
);

watch(
	() => props.node.state.chartSettings,
	() => {
		console.log(props.node.state.chartSettings);
	}
);
</script>
