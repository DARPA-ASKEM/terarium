import _ from 'lodash';
import { computed, Ref } from 'vue';
import { WorkflowNode } from '@/types/workflow';
import { CiemssMethodOptions, DataArray, processAndSortSamplesByTimepoint } from '@/services/models/simulation-service';
import { ChartData } from '@/composables/useCharts';
import { SimulateCiemssOperationState } from './simulate-ciemss-operation';

export const speedPreset = Object.freeze({
	numSamples: 10,
	method: CiemssMethodOptions.euler,
	stepSize: 0.1
});

export const qualityPreset = Object.freeze({
	numSamples: 100,
	method: CiemssMethodOptions.dopri5
});

export function usePreparedChartInputs(
	props: {
		node: WorkflowNode<SimulateCiemssOperationState>;
	},
	runResult: Ref<{ [runId: string]: DataArray }>,
	runResultSummary: Ref<{ [runId: string]: DataArray }>,
	pyciemssMap: Ref<Record<string, string>>
) {
	const selectedRunId = computed(() => props.node.outputs.find((o) => o.id === props.node.active)?.value?.[0]);

	return computed(() => {
		if (!selectedRunId.value || _.isEmpty(pyciemssMap.value)) return null;

		const result = runResult.value[selectedRunId.value];
		const resultSummary = runResultSummary.value[selectedRunId.value];

		// Process data for uncertainty intervals chart mode
		const resultGroupByTimepoint = processAndSortSamplesByTimepoint(result);

		const reverseMap: Record<string, string> = {};
		Object.keys(pyciemssMap.value).forEach((key) => {
			reverseMap[pyciemssMap.value[key]] = key;
			reverseMap[`${pyciemssMap.value[key]}:pre`] = `${key} (baseline)`;
			reverseMap[`${pyciemssMap.value[key]}_mean`] = key;
			reverseMap[`${pyciemssMap.value[key]}_mean:pre`] = `${key} (baseline)`;
		});
		return {
			result,
			resultSummary,
			translationMap: reverseMap,
			pyciemssMap: pyciemssMap.value,
			resultGroupByTimepoint
		} as ChartData;
	});
}
