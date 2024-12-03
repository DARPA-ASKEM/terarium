import _, { groupBy, max, maxBy } from 'lodash';
import { computed, Ref } from 'vue';
import { WorkflowNode } from '@/types/workflow';
import { DataArray, parsePyCiemssMap } from '@/services/models/simulation-service';
import { mergeResults } from '../calibrate-ciemss/calibrate-utils';
import { ContextMethods, OptimizeCiemssOperationState } from './optimize-ciemss-operation';

export function usePreparedChartInputs(
	props: {
		node: WorkflowNode<OptimizeCiemssOperationState>;
	},
	runResults: Ref<{ [runId: string]: DataArray }>,
	runResultsSummary: Ref<{ [runId: string]: DataArray }>
) {
	return computed(() => {
		const { preForecastRunId, postForecastRunId } = props.node.state;
		if (!postForecastRunId || !preForecastRunId) return null;
		const preResult = runResults.value[preForecastRunId];
		const preResultSummary = runResultsSummary.value[preForecastRunId];
		const postResult = runResults.value[postForecastRunId];
		const postResultSummary = runResultsSummary.value[postForecastRunId];

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

export function setQoIData(resultData: DataArray, targetVariable: string, contextMethod: ContextMethods) {
	console.log('Setting qoi data');
	let data;
	if (contextMethod === ContextMethods.day_average) {
		// last timepoints
		console.log('last timepoint:');
		const lastTime = max(resultData.map((ele) => ele.timepoint_id));
		// Filter for all values with timepoint = last timepoint
		data = resultData.filter((ele) => ele.timepoint_id === lastTime).map((ele) => ele[targetVariable]);
	} else if (contextMethod === ContextMethods.max) {
		// all timepoints
		console.log('All timepoints:');
		// For each sample grab the max value for the given state:
		data = Object.entries(groupBy(resultData, 'sample_id')).map((ele) => maxBy(ele[1], targetVariable));
	} else {
		// should not be hit unless we add more available ContextMethods that we have yet to handle.
		console.error(`The following context method is not handled: ${contextMethod}`);
	}
	console.log(data);
	return data;
}
