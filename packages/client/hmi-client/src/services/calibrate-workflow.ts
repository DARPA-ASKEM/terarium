import * as d3 from 'd3';

import { ModelConfiguration, Dataset, CsvAsset } from '@/types/Types';
import { getModelConfigurationById } from '@/services/model-configurations';
import { downloadRawFile, getDataset } from '@/services/dataset';

// Used in the setup of calibration node and drill down
// Takes a model config Id and grabs relevant objects
export const setupModelInput = async (modelConfigId: string | undefined) => {
	if (modelConfigId) {
		const modelConfiguration: ModelConfiguration = await getModelConfigurationById(modelConfigId);
		// modelColumnNames.value = modelConfig.value.configuration.model.states.map((state) => state.name);
		const modelColumnNameOptions: string[] = modelConfiguration.configuration.model.states.map(
			(state) => state.id
		);

		// add observables
		if (modelConfiguration.configuration.semantics?.ode?.observables) {
			modelConfiguration.configuration.semantics.ode.observables.forEach((o) => {
				modelColumnNameOptions.push(o.id);
			});
		}

		modelColumnNameOptions.push('timestamp');
		return { modelConfiguration, modelColumnNameOptions };
	}
	return {};
};

// Used in the setup of calibration node and drill down
// takes a datasetId and grabs relevant objects
export const setupDatasetInput = async (datasetId: string | undefined) => {
	if (datasetId) {
		// Get dataset:
		const dataset: Dataset | null = await getDataset(datasetId);
		const filename = dataset?.fileNames?.[0] ?? '';
		// FIXME: We are setting the limit to -1 (i.e. no limit) on the number of rows returned.
		// This is a temporary fix since the datasets could be very large.
		const limit = -1;

		// We are assuming here there is only a single csv file. This may change in the future as the API allows for it.
		const csv = (await downloadRawFile(datasetId, filename, limit)) as CsvAsset;
		// datasetValue.value = csvAsset.value?.csv.map((row) => row.join(',')).join('\n');
		return { filename, csv };
	}
	return {};
};

export const getDomain = (data: any[]) => {
	let minX = Number.MAX_VALUE;
	let maxX = Number.MIN_VALUE;
	let minY = Number.MAX_VALUE;
	let maxY = Number.MIN_VALUE;

	data.forEach((d) => {
		minX = Math.min(minX, d.iter);
		maxX = Math.max(maxX, d.iter);
		minY = Math.min(minY, d.loss);
		maxY = Math.max(maxY, d.loss);
	});

	return { minX, maxX, minY, maxY };
};

export const renderLossGraph = (
	element: HTMLElement,
	data: any[],
	options: { width?: number; height: number }
) => {
	const { width, height } = options;
	const elemSelection = d3.select(element);
	let svg: any = elemSelection.select('svg');
	if (svg.empty()) {
		svg = elemSelection
			.append('svg')
			.attr('width', width ?? '100%')
			.attr('height', height);
		svg.append('g').append('path').attr('class', 'line');
	}
	const group = svg.select('g');
	const path = group.select('.line');

	const { minX, maxX, minY, maxY } = getDomain(data);

	const w = width ?? element.clientWidth; // Get the width of the parent element if width not provided

	const xScale = d3.scaleLinear().domain([minX, maxX]).range([0, w]);
	const yScale = d3.scaleLinear().domain([minY, maxY]).range([height, 0]);

	const pathFn = d3
		.line()
		.x((d: any) => xScale(d.iter))
		.y((d: any) => yScale(d.loss))
		.curve(d3.curveBasis);

	// Update the data and path
	path.datum(data).attr('d', pathFn(data)).style('stroke', '#888').style('fill', 'none');
};
