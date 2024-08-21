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
export const mae = (arr1: any[], arr2: any[]) => {
	const firstTimes = arr1.map((ele) => ele.timestamp);
	const secondTimes = arr2.map((ele) => ele.timestamp);
	const sharedTimes = firstTimes.filter((ele) => secondTimes.includes(ele));

	// FIXME: THIS IS GROSS
	const error = _.meanBy(sharedTimes, (time) => {
		const firstValue = arr1.find((ele) => ele.timestamp === time).value;
		const secondValue = arr2.find((ele) => ele.timestamp === time).value;
		return Math.abs(firstValue - secondValue);
	});
	return error;
};
