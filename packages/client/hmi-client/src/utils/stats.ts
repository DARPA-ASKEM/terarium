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
