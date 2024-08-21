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

	Note: This will only compare rows with the same timestep value.
*/
export async function getErrorData2(groundTruth: DataArray, simulationData: DataArray, mapping: CalibrateMap[]) {
	const errors: DataArray = [];
	const pyciemssMap = await parsePyCiemssMap(simulationData[0]);
	const datasetTimeCol = mapping.find((ele) => ele.modelVariable === 'timestamp')?.datasetVariable;
	if (!datasetTimeCol) {
		console.error('No dataset time column found to getErrorData');
		return errors;
	}

	const datasetVariables = mapping.map((ele) => ele.datasetVariable);
	const relevantGroundTruthColumns = Object.keys(groundTruth[0]).filter(
		(variable) => datasetVariables.includes(variable) && variable !== datasetTimeCol
	);
	const truthTimestamps = groundTruth.map((ele) => ele[datasetTimeCol]);
	const simulationTimestamps = simulationData.map((ele) => ele.timepoint_id);
	const relevantTimestamps = truthTimestamps.filter((ele) => simulationTimestamps.includes(ele));
	if (relevantGroundTruthColumns.length === 0) return errors;

	// Filter out timepoints that are not shared.
	// group on sampleID
	const simulationDataGrouped = _.groupBy(
		simulationData.filter((ele) => relevantTimestamps.includes(ele.timepoint_id)),
		'sample_id'
	);

	// Helper function, takes in time and model variable.
	// Returns the corresponding groundTruth value.
	const getTruthValue = (time: number, modelVariable: string) => {
		const map = mapping.find((ele) => ele.modelVariable === modelVariable);
		if (!map) return NaN;
		const truth = groundTruth[time][map.datasetVariable];
		if (truth === undefined) return NaN; // Cant just say !truth or 0 will return NaN
		return truth;
	};

	Object.entries(simulationDataGrouped).forEach(([sampleId, simData]) => {
		const item = { sample_id: Number(sampleId) };
		relevantGroundTruthColumns.forEach((relevantColumn) => {
			item[relevantColumn] = _.meanBy(simData, (simRow) =>
				Math.abs(getTruthValue(simRow.timepoint_id, relevantColumn) - simRow[pyciemssMap[relevantColumn]])
			);
		});
		errors.push(item);
	});
	console.log(errors);
	return errors;
}

/**
 *
 * @param groundTruth
 * @param simulationData
 * @param mapping
 * transform data, utilize mae, return mean aboslute error for charts.
 */
export async function getErrorData(groundTruth: DataArray, simulationData: DataArray, mapping: CalibrateMap[]) {
	const errors: DataArray = [];
	const pyciemssMap = await parsePyCiemssMap(simulationData[0]);
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
	const groundTruthReMapped: any[] = [];

	relevantGroundTruthColumns.forEach((columnName) => {
		// Remap groundTruthReMapped into form [RelevantColumn: [{timestamp: X, value: Y}, ...], revevantColumn2: ...]
		groundTruth.forEach((ele) => {
			const timestamp = ele[datasetTimeCol];
			const value = ele[columnName];
			if (!groundTruthReMapped[columnName]) {
				groundTruthReMapped[columnName] = [];
			}
			// x[columnName][timestamp] = value;
			groundTruthReMapped[columnName].push({
				timestamp,
				value
			});
		});
		Object.entries(simulationDataGrouped).forEach(([sampleId, entries]) => {
			const transformedEntries: any[] = entries.map((entry) => ({
				timestamp: entry.timepoint_id,
				value: entry[pyciemssMap[columnName]]
			}));

			const meanAbsoluteError = mae(groundTruthReMapped[columnName], transformedEntries);
			const item = { sample_id: Number(sampleId) };
			item[columnName] = meanAbsoluteError;
			errors.push(item);
		});
	});

	console.log(errors);
	return errors;
}
