import _ from 'lodash';
import { DataArray, parsePyCiemssMap } from '@/services/models/simulation-service';
import { CalibrateMap } from '@/services/calibrate-workflow';
import { mae } from '@/utils/stats';
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
export function getErrorData(groundTruth: DataArray, simulationData: DataArray, mapping: CalibrateMap[]) {
	const errors: DataArray = [];
	const pyciemssMap = parsePyCiemssMap(simulationData[0]);
	const datasetTimeCol = mapping.find((ele) => ele.modelVariable === 'timestamp')?.datasetVariable;
	if (!datasetTimeCol) {
		console.error('No dataset time column found to getErrorData');
		return errors;
	}

	const datasetVariables = mapping.map((ele) => ele.datasetVariable);
	const relevantGroundTruthColumns = Object.keys(groundTruth[0]).filter(
		(variable) => datasetVariables.includes(variable) && variable !== datasetTimeCol
	);
	if (relevantGroundTruthColumns.length === 0) return errors;

	const simulationDataGrouped = _.groupBy(simulationData, 'sample_id');

	Object.entries(simulationDataGrouped).forEach(([sampleId, entries]) => {
		const resultRow = { sample_id: Number(sampleId) };
		relevantGroundTruthColumns.forEach((columnName) => {
			entries.forEach((entry) => {
				// Ensure the simulation data maps to the same as the ground truth:
				entry[datasetTimeCol] = entry.timepoint_id;
				entry[columnName] = entry[pyciemssMap[columnName]];
			});

			const meanAbsoluteError = mae(groundTruth, entries, datasetTimeCol, columnName);
			resultRow[columnName] = meanAbsoluteError;
		});
		errors.push(resultRow);
	});
	console.log(errors);
	return errors;
}
