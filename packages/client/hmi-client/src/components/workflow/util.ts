import { CsvAsset } from '@/types/Types';

export const getGraphDataFromDatasetCSV = (
	dataset: CsvAsset,
	columnVar: string,
	mapping?: { [key: string]: string }[]
) => {
	let v = columnVar;
	// get the dataset variable from the mapping of model variable to dataset variable if there's a mapping
	if (mapping) {
		if (!(mapping.length === 1 && Object.values(mapping[0]).some((val) => !val))) {
			const varMap = mapping.find((m) => m.modelVariable === columnVar);
			if (varMap) {
				v = varMap.datasetVariable;
			}
		}
	}

	// get  the index of the timestamp column
	const tIndex = dataset.headers.indexOf('timestamp');

	// get the index of the variable column
	let colIdx: number;
	for (let i = 0; i < dataset.headers.length; i++) {
		if (dataset.headers[i].trim() === v.trim()) {
			colIdx = i;
			break;
		}
	}

	const graphData = {
		// ignore the first row, it's the header
		data: dataset.csv.slice(1).map((datum: string[]) => ({
			x: +datum[tIndex],
			y: +datum[colIdx]
		})),
		label: `${columnVar} - dataset`,
		fill: false,
		borderColor: '#000000',
		borderDash: [3, 3]
	};

	return graphData;
};
