import _ from 'lodash';
import { DataArray, parsePyCiemssMap, processAndSortSamplesByTimepoint } from '@/services/models/simulation-service';
import { getActiveOutput } from '@/components/workflow/util';
import { CalibrateMap } from '@/services/calibrate-workflow';
import { mae } from '@/utils/stats';
import { WorkflowNode } from '@/types/workflow';
import { computed, Ref } from 'vue';
import { mergeResults } from '@/services/dataset';
import { CalibrationOperationStateCiemss } from './calibrate-operation';

/**
	* Get the mean absolute error from a provided source truth and a simulation run.
	* Utilied in calibration for charts
	* Assume that simulationData is in the form of pyciemss
			states end with _State
			The timestamp column is titled: timepoint_unknown
	* Assume that the mapping is in the calibration form:
			Ground truth will map to datasetVariable
			Simulation data will map to modelVariable AND not include _State
 * transform data, utilize mae, return mean aboslute error for charts.
 * Note: This will only compare rows with the same timestep value.
 */
export function getErrorData(
	groundTruth: DataArray,
	simulationData: DataArray,
	mapping: CalibrateMap[],
	timestampColName: string,
	pyciemssMap: Record<string, string>
) {
	const errors: DataArray = [];
	if (simulationData.length === 0 || groundTruth.length === 0 || !timestampColName || _.isEmpty(pyciemssMap))
		return errors;
	const datasetVariables = mapping.map((ele) => ele.datasetVariable);
	const relevantGroundTruthColumns = Object.keys(groundTruth[0]).filter(
		(variable) => datasetVariables.includes(variable) && variable !== timestampColName
	);
	if (relevantGroundTruthColumns.length === 0) return errors;

	const simulationDataGrouped = _.groupBy(simulationData, 'sample_id');

	Object.entries(simulationDataGrouped).forEach(([sampleId, entries]) => {
		const resultRow = { sample_id: Number(sampleId) };
		relevantGroundTruthColumns.forEach((columnName) => {
			const newEntries = entries.map((entry) => {
				// Ensure the simulation data maps to the same as the ground truth:
				const varName = mapping.find((m) => m.datasetVariable === columnName)?.modelVariable;
				return { [timestampColName]: entry.timepoint_unknown, [columnName]: entry[pyciemssMap[varName as string]] };
			});
			const meanAbsoluteError = mae(groundTruth, newEntries, timestampColName, columnName);
			resultRow[columnName] = meanAbsoluteError;
		});
		errors.push(resultRow);
	});
	return errors;
}

// Get the selected output mapping for the node
export function getSelectedOutputMapping(node: WorkflowNode<CalibrationOperationStateCiemss>) {
	const wfOutputState = getActiveOutput(node)?.state;
	return [
		...(wfOutputState?.mapping || []),
		// special case for timestamp column name mapping
		{ modelVariable: 'timepoint_unknown', datasetVariable: wfOutputState?.timestampColName ?? '' }
	];
}

export function usePreparedChartInputs(
	props: {
		node: WorkflowNode<CalibrationOperationStateCiemss>;
	},
	runResult: Ref<{
		result: DataArray;
		resultPre: DataArray;
		resultSummary: DataArray;
		resultSummaryPre: DataArray;
	} | null>
) {
	const pyciemssMap = computed(() =>
		!runResult.value?.result?.length ? {} : parsePyCiemssMap(runResult.value.result[0])
	);
	const calibrationId = computed(() => props.node.state.calibrationId);
	const datasetVariables = computed(() => getSelectedOutputMapping(props.node).map((m) => m.datasetVariable));

	return computed(() => {
		if (!runResult.value) return null;
		if (!calibrationId.value || _.isEmpty(pyciemssMap.value)) return null;

		// Merge before/after for chart
		const result = mergeResults(runResult.value.resultPre, runResult.value.result);
		const resultSummary = mergeResults(runResult.value.resultSummaryPre, runResult.value.resultSummary);
		const resultGroupByTimepoint = processAndSortSamplesByTimepoint(result);

		// Build lookup map for calibration, include before/after and dataset (observations)
		const translationMap = {};
		Object.keys(pyciemssMap.value).forEach((key) => {
			translationMap[`${pyciemssMap.value[key]}_mean`] = `${key} after calibration`;
			translationMap[`${pyciemssMap.value[key]}_mean:pre`] = `${key} before calibration`;
		});
		datasetVariables.value.forEach((variable) => {
			translationMap[variable] = 'Observations';
		});
		return {
			result,
			resultSummary,
			resultGroupByTimepoint,
			pyciemssMap: pyciemssMap.value,
			translationMap
		};
	});
}
