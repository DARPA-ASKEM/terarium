import _, { groupBy, max, minBy, maxBy, sum } from 'lodash';
import { computed, Ref } from 'vue';
import { WorkflowNode } from '@/types/workflow';
import { DataArray, parsePyCiemssMap } from '@/services/models/simulation-service';
import { mergeResults } from '../calibrate-ciemss/calibrate-utils';
import { ContextMethods, Criterion, OptimizeCiemssOperationState } from './optimize-ciemss-operation';

export function usePreparedChartInputs(
	props: {
		node: WorkflowNode<OptimizeCiemssOperationState>;
	},
	runResults: Ref<{ [runId: string]: DataArray }>,
	runResultsSummary: Ref<{ [runId: string]: DataArray }>
) {
	const preForecastRunId = computed(() => props.node.state.preForecastRunId);
	const postForecastRunId = computed(() => props.node.state.postForecastRunId);
	return computed(() => {
		if (!postForecastRunId.value || !preForecastRunId.value) return null;
		const preResult = runResults.value[preForecastRunId.value];
		const preResultSummary = runResultsSummary.value[preForecastRunId.value];
		const postResult = runResults.value[postForecastRunId.value];
		const postResultSummary = runResultsSummary.value[postForecastRunId.value];

		if (!postResult || !postResultSummary || !preResultSummary || !preResult) return null;
		const pyciemssMap = parsePyCiemssMap(postResult[0]);
		if (_.isEmpty(pyciemssMap)) return null;

		// Merge before/after for chart
		const { result, resultSummary } = mergeResults(preResult, postResult, preResultSummary, postResultSummary);

		const translationMap: Record<string, string> = {};
		Object.keys(pyciemssMap).forEach((key) => {
			translationMap[`${pyciemssMap[key]}_mean`] = `${key} after optimization`;
			translationMap[`${pyciemssMap[key]}_mean:pre`] = `${key} before optimization`;
		});

		return {
			result,
			resultSummary,
			translationMap,
			pyciemssMap
		};
	});
}

export function setQoIData(resultData: DataArray, config: Criterion) {
	const targetVar = config.targetVariable;
	let data: number[] = [];
	let averageRisk = 0;
	// Set data:
	if (config.qoiMethod === ContextMethods.day_average) {
		// last timepoints
		// Filter for all values with timepoint = last timepoint
		const lastTimeId = max(resultData.map((ele) => ele.timepoint_id));
		data = resultData.filter((ele) => ele.timepoint_id === lastTimeId).map((ele) => ele[targetVar]);
	} else if (config.qoiMethod === ContextMethods.max) {
		// all timepoints
		// For each sample grab the min or max value for the given state:
		if (config.isMinimized) {
			// Grab the max:
			data = Object.entries(groupBy(resultData, 'sample_id')).map((ele) => maxBy(ele[1], targetVar)?.[targetVar]);
		} else {
			// Grab the min
			data = Object.entries(groupBy(resultData, 'sample_id')).map((ele) => minBy(ele[1], targetVar)?.[targetVar]);
		}
	} else {
		// should not be hit unless we add more available ContextMethods that we have yet to handle.
		console.error(`The following context method is not handled: ${config.qoiMethod}`);
	}
	// Set Risk:
	// Risk value is the average of the top or bottom X% where X is 100 - riskTolerance
	const amountOfRiskIndexes = Math.ceil(((100 - config.riskTolerance) / 100) * data.length);
	if (config.isMinimized) {
		// Get for the top X
		const topX = data.sort().slice(data.length - amountOfRiskIndexes, data.length);
		averageRisk = sum(topX) / topX.length;
	} else {
		// Get bottom X
		const bottomX = data.sort().slice(0, amountOfRiskIndexes);
		averageRisk = sum(bottomX) / bottomX.length;
	}

	return { data, risk: averageRisk };
}
