import * as d3 from 'd3';

import type { Dataset, CsvAsset } from '@/types/Types';
import { getModelConfigurationById, getObservables } from '@/services/model-configurations';
import { downloadRawFile, getDataset } from '@/services/dataset';
import { getUnitsFromModelParts, getModelByModelConfigurationId, getTypesFromModelParts } from '@/services/model';

export interface CalibrateMap {
	modelVariable: string;
	datasetVariable: string;
}

// Used in the setup of calibration node and drill down
// Takes a model config Id and grabs relevant objects
export const setupModelInput = async (modelConfigId: string | undefined) => {
	if (modelConfigId) {
		const [modelConfiguration, model] = await Promise.all([
			getModelConfigurationById(modelConfigId),
			getModelByModelConfigurationId(modelConfigId)
		]);

		const modelPartUnits = !model ? {} : getUnitsFromModelParts(model);
		const modelPartTypes = !model ? {} : getTypesFromModelParts(model);

		const modelOptions: any[] = model?.model.states;

		getObservables(modelConfiguration).forEach((o) => {
			modelOptions.push(o);
		});

		modelOptions.push({ id: 'timestamp' });

		return { modelConfiguration, modelOptions, modelPartUnits, modelPartTypes };
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
		if (dataset.metadata?.format !== 'netcdf' || !dataset.esgfId) {
			const csv = (await downloadRawFile(datasetId, filename, limit)) as CsvAsset;
			// datasetValue.value = csvAsset.value?.csv.map((row) => row.join(',')).join('\n');

			if (csv?.headers) {
				csv.headers = csv.headers.map((header) => header.trim());
			}

			return { filename, csv, datasetOptions };
		}
		return {};
	}
	return {};
};

export const renderLossGraph = (element: HTMLElement, data: any[], options: { width: number; height: number }) => {
	const marginTop = 16;
	const marginBottom = 45;
	const marginLeft = 70;
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
	path
		.datum(data)
		.attr('d', pathFn(data))
		.style('stroke', '#1B8073')
		.style('stroke-width', '2px')
		.style('fill', 'none');

	// Add x-axis
	const xAxis = d3.axisBottom(xScale).ticks(5);
	let xAxisGroup = svg.select('.x-axis');
	if (xAxisGroup.empty()) {
		xAxisGroup = svg.append('g').attr('class', 'x-axis');
	}
	xAxisGroup
		.attr('transform', `translate(0, ${height - marginBottom})`)
		.call(xAxis)
		.selectAll('text')
		.style('fill', '#6A7682')
		.style('font-family', 'Figtree');
	xAxisGroup.select('.domain').style('stroke', '#EAEAEA');
	xAxisGroup.selectAll('.tick line').style('stroke', '#EAEAEA');
	xAxisGroup
		.append('text')
		.attr('class', 'axis-label')
		.attr('x', width / 2)
		.attr('y', 35)
		.style('text-anchor', 'middle')
		.style('fill', '#101828')
		.style('font-family', 'Figtree')
		.style('font-size', '12')
		.style('font-weight', '600')
		.text('Solver iterations');

	// Add y-axis
	const yAxis = d3.axisLeft(yScale).ticks(3).tickFormat(d3.format('.1e'));
	let yAxisGroup = svg.select('.y-axis');
	if (yAxisGroup.empty()) {
		yAxisGroup = svg.append('g').attr('class', 'y-axis');
	}
	yAxisGroup
		.attr('transform', `translate(${marginLeft}, 0)`)
		.call(yAxis)
		.selectAll('text')
		.style('fill', '#6A7682')
		.style('font-family', 'Figtree');
	yAxisGroup.select('.domain').style('stroke', '#EAEAEA');
	yAxisGroup.selectAll('.tick line').style('stroke', '#EAEAEA');
	yAxisGroup
		.append('text')
		.attr('class', 'axis-label')
		.attr('transform', 'rotate(-90)')
		.attr('y', -50) // Adjust this value to move the label within bounds
		.attr('x', -(height / 2.5))
		.style('text-anchor', 'middle')
		.style('fill', '#101828')
		.style('font-family', 'Figtree')
		.style('font-size', '12')
		.style('font-weight', '600')
		.text('Loss');
};
