import _ from 'lodash';
import { DataArray } from '@/services/models/simulation-service';
import { CalibrateMap } from '@/services/calibrate-workflow';

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

// Note: We want to have MAE precomputed from the backend, so we can just use instead of computing using this function on the fly.
export const computeMeanAbsoluteError = (
	calibrateResult: DataArray,
	groundTruth: DataArray,
	mapping: CalibrateMap[],
	pyciemssMap: Record<string, string>
) => {
	const errors: DataArray = [];
	if (!calibrateResult.length || !groundTruth.length || !Object.keys(pyciemssMap).length) return errors;
	const modelToDatasetVar = {};
	mapping.forEach((m) => {
		if (pyciemssMap[m.modelVariable]) modelToDatasetVar[pyciemssMap[m.modelVariable]] = m.datasetVariable;
	});
	const relevantVariables = Object.keys(calibrateResult[0]).filter((variable) => modelToDatasetVar[variable]);
	if (relevantVariables.length === 0) return errors;
	const groupedBySampleId = _.groupBy(calibrateResult, 'sample_id');

	const getTruthValue = (time: number, modelVariable: string) => {
		const datasetVariable = modelToDatasetVar[modelVariable];
		// Note:  We assume that ground truth is sorted by time in ascending order.
		const truth = groundTruth[time];
		if (!truth) return NaN;
		return truth[datasetVariable];
	};

	Object.entries(groupedBySampleId).forEach(([sampleId, values]) => {
		// only consider values that have corresponding ground truth
		const filteredValues = values.filter((value) => value.timepoint_id < groundTruth.length);
		const item = { sample_id: Number(sampleId) };
		relevantVariables.forEach((variable) => {
			item[variable] = _.meanBy(filteredValues, (value) =>
				Math.abs(getTruthValue(value.timepoint_id, variable) - value[variable])
			);
		});
		errors.push(item);
	});
	return errors;
};
