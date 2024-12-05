import { WorkflowNode } from '@/types/workflow';
import { computed, Ref } from 'vue';
import { DataArray } from '@/services/models/simulation-service';
import _ from 'lodash';
import { CompareDatasetsState } from './compare-datasets-operation';

export function usePreparedChartInputs(
	props: {
		node: WorkflowNode<CompareDatasetsState>;
	},
	runResult: Ref<{ [runId: string]: DataArray }>,
	runResultSummary: Ref<{ [runId: string]: DataArray }>,
	pyciemssMap: Ref<Record<string, string>>
) {
	return computed(() => {
		const active = props.node.active;
		if (!active) return null;
		const selectedRunId = props.node.outputs.find((o) => o.id === active)?.value?.[0];
		if (!selectedRunId || _.isEmpty(pyciemssMap.value)) return null;

		const result = runResult.value[selectedRunId];
		const resultSummary = runResultSummary.value[selectedRunId];
		const reverseMap: Record<string, string> = {};
		Object.keys(pyciemssMap.value).forEach((key) => {
			reverseMap[pyciemssMap.value[key]] = key;
			reverseMap[`${pyciemssMap.value[key]}_mean`] = key;
			reverseMap[`${pyciemssMap.value[key]}_mean:pre`] = `${key} (baseline)`;
		});
		return { result, resultSummary, translationMap: reverseMap, pyciemssMap: pyciemssMap.value };
	});
}
