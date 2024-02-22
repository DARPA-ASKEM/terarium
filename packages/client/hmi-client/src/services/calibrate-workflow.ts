import * as d3 from 'd3';

import type { ModelConfiguration, Dataset, CsvAsset, State, DatasetColumn } from '@/types/Types';
import { getModelConfigurationById } from '@/services/model-configurations';
import { downloadRawFile, getDataset } from '@/services/dataset';
import { getEntitySimilarity } from './concept';

export interface CalibrateMap {
	modelVariable: string;
	datasetVariable: string;
}

// Used in the setup of calibration node and drill down
// Takes a model config Id and grabs relevant objects
export const setupModelInput = async (modelConfigId: string | undefined) => {
	if (modelConfigId) {
		const modelConfiguration: ModelConfiguration = await getModelConfigurationById(modelConfigId);
		const modelOptions: State[] = modelConfiguration.configuration.model.states;

		// add observables
		if (modelConfiguration.configuration.semantics?.ode?.observables) {
			modelConfiguration.configuration.semantics.ode.observables.forEach((o) => {
				modelOptions.push(o);
			});
		}

		modelOptions.push({ id: 'timestamp' });
		return { modelConfiguration, modelOptions };
	}
	return {};
};

// Used in the setup of calibration node and drill down
// takes a datasetId and grabs relevant objects
export const setupDatasetInput = async (datasetId: string | undefined) => {
	if (datasetId) {
		// Get dataset:
		const dataset: Dataset | null = await getDataset(datasetId);
		if (dataset === undefined || !dataset) {
			console.log(`Dataset with id:${datasetId} not found`);
			return {};
		}
		const datasetOptions = dataset.columns;
		const filename = dataset?.fileNames?.[0] ?? '';
		// FIXME: We are setting the limit to -1 (i.e. no limit) on the number of rows returned.
		// This is a temporary fix since the datasets could be very large.
		const limit = -1;

		// We are assuming here there is only a single csv file. This may change in the future as the API allows for it.
		const csv = (await downloadRawFile(datasetId, filename, limit)) as CsvAsset;
		// datasetValue.value = csvAsset.value?.csv.map((row) => row.join(',')).join('\n');

		if (csv?.headers) {
			csv.headers = csv.headers.map((header) => header.trim());
		}

		return { filename, csv, datasetOptions };
	}
	return {};
};

export const renderLossGraph = (
	element: HTMLElement,
	data: any[],
	options: { width: number; height: number }
) => {
	const marginTop = 10;
	const marginBottom = 20;
	const marginLeft = 50;
	const marginRight = 20;

	const { width, height } = options;
	const elemSelection = d3.select(element);
	let svg: any = elemSelection.select('svg');
	if (svg.empty()) {
		svg = elemSelection.append('svg').attr('width', width).attr('height', height);
		svg.append('g').append('path').attr('class', 'line');
	}
	const group = svg.select('g');
	const path = group.select('.line');

	const [minX, maxX] = d3.extent(data, (d) => d.iter);
	const [minY, maxY] = d3.extent(data, (d) => d.loss);

	const xScale = d3
		.scaleLinear()
		.domain([minX, maxX])
		.range([marginLeft, width - marginRight]);
	const yScale = d3
		.scaleLinear()
		.domain([Math.min(minY, 0), maxY])
		.range([height - marginBottom, marginTop]);

	const pathFn = d3
		.line()
		.x((d: any) => xScale(d.iter))
		.y((d: any) => yScale(d.loss))
		.curve(d3.curveBasis);

	// Update the data and path
	path.datum(data).attr('d', pathFn(data)).style('stroke', '#888').style('fill', 'none');

	// Add x-axis
	const xAxis = d3.axisBottom(xScale).ticks(5);
	let xAxisGroup = svg.select('.x-axis');
	if (xAxisGroup.empty()) {
		xAxisGroup = svg.append('g').attr('class', 'x-axis');
	}
	xAxisGroup.attr('transform', `translate(0, ${height - marginBottom})`).call(xAxis);

	// Add y-axis
	const yAxis = d3.axisLeft(yScale).ticks(3).tickFormat(d3.format('.1e'));
	let yAxisGroup = svg.select('.y-axis');
	if (yAxisGroup.empty()) {
		yAxisGroup = svg.append('g').attr('class', 'y-axis');
	}
	yAxisGroup.attr('transform', `translate(${marginLeft}, 0)`).call(yAxis);
};

export const autoCalibrationMapping = async (
	modelOptions: State[],
	datasetOptions: DatasetColumn[]
) => {
	const result = [] as CalibrateMap[];
	const allModelGroundings: string[] = [];
	const allDataGroundings: string[] = [];
	const acceptableDistance = 0.5;
	// Get all model groundings
	modelOptions.forEach((state) => {
		if (state.grounding?.identifiers) {
			Object.entries(state.grounding?.identifiers)
				.map((ele) => ele.join(':'))
				.forEach((e) => allModelGroundings.push(e));
		}
	});
	// Get all data column groundings
	datasetOptions.forEach((col) => {
		if (col.metadata?.groundings?.identifiers) {
			Object.entries(col.metadata?.groundings?.identifiers)
				.map((ele) => ele.join(':'))
				.forEach((ele) => allDataGroundings.push(ele));
		}
	});

	// take out duplicates:
	const distinctModelGroundings = [...new Set(allModelGroundings)];
	const distinctDataGroundings = [...new Set(allDataGroundings)];
	const allSimilarity = await getEntitySimilarity(distinctModelGroundings, distinctDataGroundings);
	if (!allSimilarity) return result;

	const filteredSim = allSimilarity.filter((ele) => ele.similarity < acceptableDistance);
	filteredSim.forEach((sim) => {
		// Find all states assosiated with this sim
		const validStates = modelOptions.filter((state) => {
			if (state.grounding?.identifiers) {
				const modelTemp = Object.entries(state.grounding?.identifiers);
				const modelGroundingList = modelTemp.map((ele) => ele.join(':'));
				return modelGroundingList.includes(sim.source);
			}
			return false;
		});
		// Find all columns assosiated with this sim
		const validCols = datasetOptions.filter((col) => {
			const dataGroundingList = Object.keys(col.metadata?.groundings?.identifiers);
			return dataGroundingList.includes(sim.target);
		});
		// For all states and columns that have short distances throw them into results
		validStates.forEach((state) => {
			validCols.forEach((col) => {
				result.push({ modelVariable: state.id, datasetVariable: col.name });
			});
		});
	});

	// due to a state and a column having potential for multiple pairwise matches, lets remove duplicates from results.
	const distinctResults = result.filter(
		(value, index) =>
			index ===
			result.findIndex(
				(obj) =>
					obj.datasetVariable === value.datasetVariable && obj.modelVariable === value.modelVariable
			)
	);

	return distinctResults;
};
