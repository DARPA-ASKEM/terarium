import _ from 'lodash';

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
	const error = _.meanBy(sharedKey, (key) => {
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
