import _ from 'lodash';

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

// Get the mean absolute error between two arrays.
// Assume each array is formed: [ { timestamp: value }, { timestamp: value } ...]
export const mae = (map1: Map<number, number>, map2: Map<number, number>) => {
	const sharedTimes = [...map1.keys()].filter((key) => map2.has(key));
	const error = _.meanBy(sharedTimes, (time) => {
		const firstValue = map1.get(time) as number;
		const secondValue = map2.get(time) as number;
		return Math.abs(firstValue - secondValue);
	});
	return error;
};
