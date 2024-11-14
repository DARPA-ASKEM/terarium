import _ from 'lodash';
import { computed, Ref } from 'vue';
import { WorkflowNode } from '@/types/workflow';
import { DataArray, parsePyCiemssMap } from '@/services/models/simulation-service';
import { mergeResults } from '../calibrate-ciemss/calibrate-utils';
import { OptimizeCiemssOperationState } from './optimize-ciemss-operation';

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
