import { CellStatus } from '@/types/ResponsiveMatrix';

// FIXME: add test
export const makeLabels = (
	itemList: any[],
	itemStatusList: CellStatus[],
	microSettings: number[],
	stride: number
) => {
	let labelPosition = 0;
	const results: { value: any; position: number }[] = [];

	for (let i = 0; i < itemStatusList.length; i++) {
		const len = microSettings[itemStatusList[i]];

		if (!(i % stride)) {
			// use the position in the middle
			results.push({
				value: itemList[i],
				position: labelPosition + len / 2
			});
		}
		labelPosition += len;
	}

	// divide the label positions in the array by the sum of all the col lengths
	// to normalize positions between 0 - 1. multiply by 100 to get a percentage
	// value for use in css.
	results.forEach((r) => {
		r.position = (r.position / labelPosition) * 100;
	});

	return results;
};
