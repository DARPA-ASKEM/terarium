import _ from 'lodash';
import { DataArray, parsePyCiemssMap } from '@/services/models/simulation-service';
import { CalibrateMap } from '@/services/calibrate-workflow';

export const mean = (numberList: number[]) =>
	numberList.reduce((acc: number, val: number) => acc + val, 0) / numberList.length;

// Lifted from https://stackoverflow.com/questions/7343890/standard-deviation-javascript
export const stddev = (numberList: number[], usePopulation = false) => {
	const avg = mean(numberList);
	return Math.sqrt(
		numberList
			.reduce((acc: number[], val: number) => acc.concat((val - avg) ** 2), [])
			.reduce((acc, val) => acc + val, 0) /
			(numberList.length - (usePopulation ? 0 : 1))
	);
};

// Get the mean absolute error from a provided source truth (input dataset) and a simulation run.
// Utilied in calibration for charts
// Assume that simulationData is in the form of pyciemss
// 	This means states end with _State
//  This means the timestamp column is titled: timepoint_id
// Assume that the mapping is in the calibration form - AKA modelVariables will not have _State
export async function getErrorData(groundTruth: DataArray, simulationData: DataArray, mapping: CalibrateMap[]) {
	const errors: DataArray = [];
	const pyciemssMap = await parsePyCiemssMap(simulationData[0]);
	const timeMap = mapping.find((ele) => ele.modelVariable === 'timestamp');
	const datasetTimeCol = timeMap?.datasetVariable;
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
	const simulationDataGrouped = _.groupBy(simulationData, 'sample_id');

	// Helper function, takes in time and model variable.
	// Returns the corresponding groundTruth value.
	const getTruthValue = (time: number, modelVariable: string) => {
		const map = mapping.find((ele) => ele.modelVariable === modelVariable);
		if (!map) return NaN;
		const truth = groundTruth[time][map.datasetVariable];
		if (!truth) return NaN;
		return truth;
	};

	Object.entries(simulationDataGrouped).forEach(([sampleId, values]) => {
		// only consider values that have corresponding ground truth
		const filteredValues = values.filter((value) => relevantTimestamps.includes(value.timepoint_id));

		const item = { sample_id: Number(sampleId) };
		relevantGroundTruthColumns.forEach((relevantColumn) => {
			item[relevantColumn] = _.meanBy(filteredValues, (value) =>
				Math.abs(getTruthValue(value.timepoint_id, relevantColumn) - value[pyciemssMap[relevantColumn]])
			);
		});
		errors.push(item);
	});
	return errors;
}
