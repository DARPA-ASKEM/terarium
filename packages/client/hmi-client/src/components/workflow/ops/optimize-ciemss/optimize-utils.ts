import _ from 'lodash';
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
		// Filter for all values with timepoint = last timepoint
		const time = resultData.timepoint_id[-1];
		console.log(time);
		data = resultData.filter((ele) => ele.timepoint_id === time)[targetVariable];
	} else if (contextMethod === ContextMethods.max) {
		// all timepoints
		console.log('All timepoints:');
		// result_postoptimize['data'].groupby(['sample_id']).max()['I_state']
		// console.log(Object.entries(groupBy(resultData, 'sample_id')));
		// data = Object.entries(groupBy(resultData, 'sample_id')).map((list) => list[1]);
	} else {
		// should not be hit unless we add more available ContextMethods that we have yet to handle.
		console.error(`The following context method is not handled: ${contextMethod}`);
	}
	console.log(data);
	return data;
}
