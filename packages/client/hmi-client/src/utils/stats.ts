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

// Get the mean absolute error between two maps.
// Note this will only compare values that share the same key.
export const mae = (map1: Map<number, number>, map2: Map<number, number>) => {
	const sharedKey = [...map1.keys()].filter((key) => map2.has(key));
	const error = _.meanBy(sharedKey, (key) => {
		const firstValue = map1.get(key) as number;
		const secondValue = map2.get(key) as number;
		return Math.abs(firstValue - secondValue);
	});
	return error;
};
