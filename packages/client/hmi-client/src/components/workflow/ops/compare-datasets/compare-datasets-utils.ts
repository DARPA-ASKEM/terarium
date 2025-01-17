import { isEmpty } from 'lodash';
import { Dataset } from '@/types/Types';
import { WorkflowPortStatus } from '@/types/workflow';
import { renameFnGenerator } from '@/components/workflow/ops/calibrate-ciemss/calibrate-utils';

import { createRankingInterventionsChart, CATEGORICAL_SCHEME } from '@/services/charts';
import { DATASET_VAR_NAME_PREFIX, getDatasetResultCSV, mergeResults, getDataset } from '@/services/dataset';
import {
	DataArray,
	parsePyCiemssMap,
	processAndSortSamplesByTimepoint,
	getRunResultCSV
} from '@/services/models/simulation-service';
import { getInterventionPolicyById } from '@/services/intervention-policy';
import { getModelConfigurationById } from '@/services/model-configurations';

import { ChartData } from '@/composables/useCharts';

import { PlotValue, TimepointOption, RankOption } from './compare-datasets-operation';

interface DataResults {
	results: DataArray[];
	summaryResults: DataArray[];
	datasetResults: DataArray[];
}

export function isSimulationData(dataset: Dataset) {
	return Boolean(dataset.metadata.simulationId);
}

/**
 * Similar to `renameFnGenerator` but it generates a rename function for the dataset results. Prefix dataset column names with 'data/' to distinguish them from the simulation results
 * @param label
 * @returns
 */
const datasetRenameFnGenerator = (label: string) => (col: string) => {
	if (col === 'timepoint_id' || col === 'sample_id') return col;
	return `${DATASET_VAR_NAME_PREFIX}${col}:${label}`;
};

export async function fetchDatasetResults(datasetList: Dataset[]) {
	// Fetch simulation results
	const results: DataArray[] = (
		await Promise.all(
			datasetList.map((dataset, datasetIndex) => {
				if (!isSimulationData(dataset)) return Promise.resolve([]);
				return getRunResultCSV(dataset.metadata.simulationId, 'result.csv', renameFnGenerator(`${datasetIndex}`));
			})
		)
	).filter((result) => result.length > 0);
	// Fetch simulation summary results
	const summaryResults: DataArray[] = (
		await Promise.all(
			datasetList.map((dataset, datasetIndex) => {
				if (!isSimulationData(dataset)) return Promise.resolve([]);
				return getRunResultCSV(
					dataset.metadata.simulationId,
					'result_summary.csv',
					renameFnGenerator(`${datasetIndex}`)
				);
			})
		)
	).filter((result) => result.length > 0);
	// Fetch non simulation dataset results
	const datasetResults: DataArray[] = (
		await Promise.all(
			datasetList.map((dataset, datasetIndex) => {
				if (isSimulationData(dataset)) return Promise.resolve([]);
				return getDatasetResultCSV(dataset, dataset.fileNames?.[0] ?? '', datasetRenameFnGenerator(`${datasetIndex}`));
			})
		)
	).filter((result) => result.length > 0);
	return {
		results,
		summaryResults,
		datasetResults
	};
}

export const transformRowValuesRelativeToBaseline = (
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
export const uniqueVarNames = (obj: Record<string, any>) => {
	const newObj = {};
	Object.keys(obj)
		.map((key) => key.split(':')[0])
		.forEach((key) => {
			newObj[key] = key;
		});
	return newObj;
};

/**
 * Build variable display name mapping for the non simulation result dataset which can be added to the pyciemss map
 */
export const buildDatasetVarMapping = (dsets: DataArray[]) => {
	const map: Record<string, any> = {};
	dsets.forEach((dataset) => {
		Object.keys(dataset[0]).forEach((key) => {
			const varName = key.split(':')[0];
			map[varName.replace(DATASET_VAR_NAME_PREFIX, '')] = varName;
		});
	});
	return map;
};

export function buildChartData(
	datasets: Dataset[],
	dataResults: DataResults | null,
	baselineDatasetIndex: number,
	plotType: PlotValue
): ChartData | null {
	if (datasets.length <= 1 || !dataResults) return null;
	const { results, summaryResults, datasetResults } = dataResults;

	// Merge all datasets into single dataset
	const result = mergeResults(...results);
	const summaryResult = mergeResults(...summaryResults, ...datasetResults);

	// Transform the values relative to the baseline dataset
	const resultTransformed = result.map((row) =>
		transformRowValuesRelativeToBaseline(row, baselineDatasetIndex, plotType)
	);
	const resultSummaryTransformed = summaryResult.map((row) =>
		transformRowValuesRelativeToBaseline(row, baselineDatasetIndex, plotType)
	);

	// Process data for uncertainty intervals chart mode
	const resultGroupByTimepoint = processAndSortSamplesByTimepoint(resultTransformed);

	// Build pyciemss map for display variable name map for the simulation result variables
	const pyciemssMap = parsePyCiemssMap(uniqueVarNames(result[0] ?? {}));
	// Augment the pyciemss map with the dataset variable mapping.
	Object.assign(pyciemssMap, buildDatasetVarMapping(datasetResults));

	// Build translation map
	const translationMap = {};
	Object.keys(pyciemssMap).forEach((key) => {
		datasets.forEach((dataset, index) => {
			translationMap[`${pyciemssMap[key]}:${index}`] = `${dataset.name}`;
			translationMap[`${pyciemssMap[key]}_mean:${index}`] = `${dataset.name}`;
		});
	});

	return {
		result: resultTransformed,
		resultSummary: resultSummaryTransformed,
		resultGroupByTimepoint,
		pyciemssMap,
		translationMap,
		numComparableDatasets: datasets.length
	};
}

export function generateRankingCharts(
	rankingCriteriaCharts,
	rankingResultsChart,
	props,
	modelConfigIdToInterventionPolicyIdMap,
	chartData,
	interventionPolicies
) {
	// Reset charts
	rankingCriteriaCharts.value = [];
	rankingResultsChart.value = null;

	// Might be uneccessary
	const commonInterventionPolicyIds = props.node.state.criteriaOfInterestCards
		.map(({ selectedConfigurationId }) => {
			if (!selectedConfigurationId) return [];
			return modelConfigIdToInterventionPolicyIdMap.value?.[selectedConfigurationId] ?? [];
		})
		.flat();
	const allRankedCriteriaValues: { score: number; name: string }[][] = [];
	const interventionNameColorMap: Record<string, string> = {};

	props.node.state.criteriaOfInterestCards.forEach((card) => {
		if (!card.selectedConfigurationId || !chartData.value) return;

		const pointOfComparison =
			card.timepoint === TimepointOption.FIRST
				? chartData.value.resultSummary[0]
				: chartData.value.resultSummary[chartData.value.resultSummary.length - 1];

		const rankingCriteriaValues: { score: number; name: string }[] = [];

		// let properIndex = 0;
		interventionPolicies.value.forEach((policy, index: number) => {
			// Skip this intervention policy if a configuration is not using it
			if (!policy.id || !policy.name || !commonInterventionPolicyIds.includes(policy.id) || !card.selectedVariable) {
				return;
			}

			// index === properIndex++;

			if (!interventionNameColorMap[policy.name]) {
				interventionNameColorMap[policy.name] = CATEGORICAL_SCHEME[index];
			}

			rankingCriteriaValues.push({
				score: pointOfComparison[`${chartData.value?.pyciemssMap[card.selectedVariable]}_mean:${index}`] ?? 0,
				name: policy.name ?? ''
			});
		});

		const sortedRankingCriteriaValues =
			card.rank === RankOption.MAXIMUM
				? rankingCriteriaValues.sort((a, b) => b.score - a.score)
				: rankingCriteriaValues.sort((a, b) => a.score - b.score);

		rankingCriteriaCharts.value.push(
			createRankingInterventionsChart(
				sortedRankingCriteriaValues,
				interventionNameColorMap,
				card.name,
				card.selectedVariable
			)
		);
		allRankedCriteriaValues.push(sortedRankingCriteriaValues);
	});

	// Sum up the values of the same intervention policy
	const valueMap: Record<string, number> = {};
	allRankedCriteriaValues.flat().forEach(({ score, name }) => {
		if (valueMap[name]) {
			valueMap[name] += score;
		} else {
			valueMap[name] = score;
		}
	});

	const rankingResultsScores: { score: number; name: string }[] = Object.keys(valueMap)
		.map((name) => ({
			name,
			score: valueMap[name]
		}))
		.sort((a, b) => a.score - b.score)
		// Instead of the values, we want to rank by score
		.map((value, index) => ({ ...value, score: index + 1 }));

	rankingResultsChart.value = createRankingInterventionsChart(rankingResultsScores, interventionNameColorMap);
}

export async function generateImpactCharts(
	chartData,
	datasets,
	datasetResults,
	baselineDatasetIndex,
	selectedPlotType
) {
	chartData.value = buildChartData(
		datasets.value,
		datasetResults.value,
		baselineDatasetIndex.value,
		selectedPlotType.value
	);
}

// TODO: this should probably be split up into smaller functions but for now it's at least not duplicated in the node and drilldown
export async function initialize(
	props,
	isFetchingDatasets,
	datasets,
	datasetResults,
	modelConfigIdToInterventionPolicyIdMap,
	chartData,
	baselineDatasetIndex,
	selectedPlotType,
	modelConfigurations,
	interventionPolicies,
	rankingCriteriaCharts,
	rankingResultsChart
) {
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
	const modelConfigurationIds = Object.keys(modelConfigIdToInterventionPolicyIdMap.value);
	if (isEmpty(modelConfigurationIds)) return;
	const modelConfigurationPromises = modelConfigurationIds.map((id) => getModelConfigurationById(id));
	await Promise.all(modelConfigurationPromises).then((configs) => {
		modelConfigurations.value = configs.filter((config) => config !== null);
	});

	const interventionPolicyIds = Object.values(modelConfigIdToInterventionPolicyIdMap.value).flat();
	if (isEmpty(interventionPolicyIds)) return;
	const interventionPolicyPromises = interventionPolicyIds.map((id) => getInterventionPolicyById(`${id}`));
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
}
