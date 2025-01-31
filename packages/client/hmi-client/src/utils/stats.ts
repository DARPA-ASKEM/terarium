import { meanBy, isEmpty } from 'lodash';

export type DataArray = Record<string, any>[];

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

// Get the mean absolute error between two maps.
// Note this will only compare values that share the same key.
export const mae = (arr1: DataArray, arr2: DataArray, keyField: string, valueField: string) => {
	const map1: Map<number, number> = new Map();
	const map2: Map<number, number> = new Map();

	// Remap for convenience
	arr1.forEach((ele) => {
		const timestamp = ele[keyField];
		const value = ele[valueField];
		map1.set(timestamp, value);
	});

	arr2.forEach((ele) => {
		const timestamp = ele[keyField];
		const value = ele[valueField];
		map2.set(timestamp, value);
	});

	const sharedKey = [...map1.keys()].filter((key) => map2.has(key));
	const error = meanBy(sharedKey, (key) => {
		const firstValue = map1.get(key) as number;
		const secondValue = map2.get(key) as number;
		return Math.abs(firstValue - secondValue);
	});
	return error;
};

export function quantile(numberList: number[], q: number) {
	if (numberList.length === 0) return 0;

	const pos = (numberList.length - 1) * q;
	const lowerIndex = Math.floor(pos);
	const upperIndex = Math.ceil(pos);

	if (lowerIndex === upperIndex) {
		return numberList[lowerIndex];
	}

	// Linear interpolation between two closest ranks
	const lowerValue = numberList[lowerIndex];
	const upperValue = numberList[upperIndex];

	return lowerValue + (upperValue - lowerValue) * (pos - lowerIndex);
}

// Weighted Interval Score (WIS)
// These are js ports of python functions specified in this issue: https://github.com/orgs/DARPA-ASKEM/projects/3/views/8?pane=issue&itemId=93848937&issue=DARPA-ASKEM%7Cterarium%7C6067
// The original python code: https://github.com/adrian-lison/interval-scoring/blob/master/scoring.py

const DEFAULT_ALPHA_QS = [
	0.01, 0.025, 0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5, 0.55, 0.6, 0.65, 0.7, 0.75, 0.8, 0.85, 0.9, 0.95,
	0.975, 0.99
];

// Compute the estimated quantiles from a time-series dataset with many samples.
export function computeQuantile(
	summaryResult: Record<string, any>,
	variableName: string,
	alphas: number[] = DEFAULT_ALPHA_QS
) {
	const values = summaryResult.map((row) => row[variableName]);
	const quantileValueMap: Record<number, number> = {};
	alphas.forEach((q) => {
		quantileValueMap[q] = quantile(values, q);
	});
	return quantileValueMap;
}

// Compute interval scores (1) for an array of observations and predicted intervals.
// Either a dictionary with the respective (alpha/2) and (1-(alpha/2)) quantiles via q_dict needs to be
// specified or the quantiles need to be specified via q_left and q_right.
export function intervalScore(
	groundTruthObservations: Record<number, number>,
	variableObservations: Record<number, number>,
	alpha: number,
	percent: boolean,
	checkConsistency: boolean,
	leftQuantile: number | null = null,
	rightQuantile: number | null = null
) {
	if (!leftQuantile) {
		leftQuantile = variableObservations[alpha / 2];
	}
	if (!rightQuantile) {
		rightQuantile = variableObservations[1 - alpha / 2];
	}

	console.log('leftQuantile', leftQuantile, 'rightQuantile', rightQuantile);
	if (checkConsistency && leftQuantile > rightQuantile) {
		throw new Error('Left quantile must be smaller than right quantile.');
	}

	const sharpness = rightQuantile - leftQuantile;

	const calibration = Object.values(groundTruthObservations).map((obs) => {
		const leftClip = Math.max(0, leftQuantile - obs);
		const rightClip = Math.max(0, obs - rightQuantile);
		return ((leftClip + rightClip) * 2) / alpha;
	});

	// if (percent) {
	// 	sharpness /= mean(Object.values(groundTruthObservations));
	// 	// sharpness = sharpness / observations.map((obs) => Math.abs(obs));
	// 	calibration = calibration.map((cal, index) => cal / Math.abs(groundTruthObservations[index]));
	// }

	const total = calibration.map((cal) => cal + sharpness);

	return { total, sharpness: [sharpness], calibration };
}

// Compute weighted interval scores for an array of observations and a number of different predicted intervals.
export function weightedIntervalScore(
	groundTruthObservations: Record<number, number>,
	variableObservations: Record<number, number>,
	alphas: number[] = [0.02, 0.05, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9],
	weights: number[] = [],
	percent: boolean = false,
	checkConsistency: boolean = true
) {
	if (isEmpty(weights)) {
		weights = alphas.map((alpha) => alpha / 2);
	}

	// Helper function to weigh scores
	function weighScores(tupleIn: any[], weight: number) {
		return [tupleIn[0].map((d) => d * weight), tupleIn[1].map((d) => d * weight), tupleIn[2].map((d) => d * weight)];
	}

	// Calculate interval scores for each alpha and weight
	const intervalScores = alphas.map((alpha, index) => {
		const { total, sharpness, calibration } = intervalScore(
			groundTruthObservations,
			variableObservations,
			alpha,
			percent,
			checkConsistency
		);
		return weighScores([total, sharpness, calibration], weights[index]);
	});

	console.log('intervalScores', intervalScores);

	// Transpose the result to sum across different alphas
	const summedScores = intervalScores.reduce(
		(acc: any, scoreTuple) => {
			scoreTuple.forEach((score, idx) => {
				acc[idx].push(score);
			});
			return acc;
		},
		[[], [], []]
	);

	console.log('summedScores', summedScores);

	const [totalScores, sharpnessScores, calibrationScores] = summedScores;

	// Sum the scores across all alphas and normalize by the sum of the weights
	const weightSum = weights.reduce((sum, val) => sum + val, 0);

	// Sum column-wise
	function columnWiseSum(array: number[][]) {
		return array[0].map((_, colIndex) => array.reduce((sum, row) => sum + row[colIndex], 0));
	}

	const total = columnWiseSum(totalScores).map((d) => d / weightSum);
	const sharpness = sharpnessScores.reduce((sum, val) => sum + val, 0) / weightSum;
	const calibration = columnWiseSum(calibrationScores).map((d) => d / weightSum);

	console.log('total', total, 'sharpness', sharpness, 'calibration', calibration);

	return { total, sharpness, calibration };
}
