import _ from 'lodash';
import { DataArray } from '@/services/models/simulation-service';

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
	return { result, resultSummary, error: computeMeanAbsoluteError(result) };
};

export const computeMeanAbsoluteError = (result: DataArray) => {
	const errors: DataArray = [];
	if (result.length === 0) return errors;

	const relevantVariables = Object.keys(result[0])
		.filter((key) => key.includes(':pre'))
		.map((key) => key.replace(':pre', ''));
	const groupedBySampleId = _.groupBy(result, 'sample_id');

	Object.entries(groupedBySampleId).forEach(([sampleId, values]) => {
		const item = { sample_id: Number(sampleId) };
		relevantVariables.forEach((variable) => {
			item[variable] = _.meanBy(values, (value) => Math.abs(value[`${variable}:pre`] - value[variable]));
		});
		errors.push(item);
	});
	return errors;
};
