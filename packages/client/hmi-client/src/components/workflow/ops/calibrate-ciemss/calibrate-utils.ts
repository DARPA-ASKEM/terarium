import _ from 'lodash';
import { DataArray, parsePyCiemssMap } from '@/services/models/simulation-service';
import { getActiveOutput } from '@/components/workflow/util';
import { CalibrateMap } from '@/services/calibrate-workflow';
import { mae } from '@/utils/stats';
import { WorkflowNode } from '@/types/workflow';
import { computed, Ref } from 'vue';
import { CalibrationOperationStateCiemss } from './calibrate-operation';
/**
 * A rename function generator for getRunResultCSV. Here the idea
 * to differentiate before and after columns in the run results
 * */
export const renameFnGenerator = (label: string) => (col: string) => {
	if (col === 'timepoint_id' || col === 'sample_id') return col;
	return `${col}:${label}`;
};

/**
 * Merge before and after run and summary results, assume to be equal length and aligned
 * */
export const mergeResults = (
	resultPre: DataArray,
	resultAfter: DataArray,
	resultSummaryPre: DataArray,
	resultSummaryAfter: DataArray
) => {
	const result: DataArray = [];
	const resultSummary: DataArray = [];
	for (let i = 0; i < resultAfter.length; i++) {
		result.push(_.assign(resultAfter[i], resultPre[i]));
	}
	for (let i = 0; i < resultSummaryAfter.length; i++) {
		resultSummary.push(_.assign(resultSummaryAfter[i], resultSummaryPre[i]));
	}
	return { result, resultSummary };
};

/**
	* Get the mean absolute error from a provided source truth and a simulation run.
	* Utilied in calibration for charts
	* Assume that simulationData is in the form of pyciemss
			states end with _State
			The timestamp column is titled: timepoint_id
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
				return { [timestampColName]: entry.timepoint_id, [columnName]: entry[pyciemssMap[varName as string]] };
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
		{ modelVariable: 'timepoint_id', datasetVariable: wfOutputState?.timestampColName ?? '' }
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
	return computed(() => {
		if (!runResult.value) return null;
		const pyciemssMap = computed(() =>
			!runResult.value?.result?.length ? {} : parsePyCiemssMap(runResult.value.result[0])
		);

		const state = props.node.state;
		if (!state.calibrationId || _.isEmpty(pyciemssMap.value)) return null;

		// Merge before/after for chart
		const { result, resultSummary } = mergeResults(
			runResult.value.resultPre,
			runResult.value.result,
			runResult.value.resultSummaryPre,
			runResult.value.resultSummary
		);

		// Build lookup map for calibration, include before/after and dataset (observations)
		const translationMap = {};
		Object.keys(pyciemssMap.value).forEach((key) => {
			translationMap[`${pyciemssMap.value[key]}_mean`] = `${key} after calibration`;
			translationMap[`${pyciemssMap.value[key]}_mean:pre`] = `${key} before calibration`;
		});
		getSelectedOutputMapping(props.node).forEach((mapObj) => {
			translationMap[mapObj.datasetVariable] = 'Observations';
		});
		return {
			result,
			resultSummary,
			pyciemssMap: pyciemssMap.value,
			translationMap
		};
	});
}
