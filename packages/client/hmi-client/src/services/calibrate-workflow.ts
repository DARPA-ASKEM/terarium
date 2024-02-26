import * as d3 from 'd3';

import type { ModelConfiguration, Dataset, CsvAsset, State, DatasetColumn } from '@/types/Types';
import { getModelConfigurationById } from '@/services/model-configurations';
import { downloadRawFile, getDataset } from '@/services/dataset';
import { getEntitySimilarity } from './concept';

export interface CalibrateMap {
	modelVariable: string;
	datasetVariable: string;
}

interface Entity {
	id: string;
	groundings?: any[];
}

interface EntityMap {
	source: string;
	target: string;
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

// Takes in 2 lists of generic {id, groundings} and returns the singular
// closest match for each element in list one
export const autoEntityMapping = async (
	sourceEntities: Entity[],
	targetEntities: Entity[],
	acceptableDist?: number
) => {
	const result = [] as EntityMap[];
	const acceptableDistance = acceptableDist ?? 0.5;
	const allSourceGroundings: string[] = [];
	const allTargetGroundings: string[] = [];

	// join all source and target for 1 mira call
	sourceEntities.forEach((ele) => {
		if (ele.groundings) {
			ele.groundings.forEach((grounding) => allSourceGroundings.push(grounding));
		}
	});
	targetEntities.forEach((ele) => {
		if (ele.groundings) {
			ele.groundings.forEach((grounding) => allTargetGroundings.push(grounding));
		}
	});

	// Take out any duplicates
	const distinctSourceGroundings = [...new Set(allSourceGroundings)];
	const distinctTargetGroundings = [...new Set(allTargetGroundings)];
	const allSimilarity = await getEntitySimilarity(
		distinctSourceGroundings,
		distinctTargetGroundings
	);
	if (!allSimilarity) return result;
	// Filter out anything with a similarity too small
	const filteredSimilarity = allSimilarity.filter((ele) => ele.similarity >= acceptableDistance);
	const validSources: any[] = [];
	const validTargets: any[] = [];

	// Join results back with source and target
	filteredSimilarity.forEach((similarity) => {
		// Find all Sources assosiated with this similarity
		sourceEntities.forEach((source) => {
			if (source.groundings && source.groundings.includes(similarity.source)) {
				validSources.push({
					sourceId: source.id,
					sourceKey: similarity.source,
					targetKey: similarity.target,
					distance: similarity.similarity
				});
			}
		});
		// Find all targets assosiated with this similarity
		targetEntities.forEach((target) => {
			if (target.groundings && target.groundings.includes(similarity.target)) {
				validTargets.push({
					targetId: target.id,
					sourceKey: similarity.source,
					targetKey: similarity.target,
					distance: similarity.similarity
				});
			}
		});
	});

	// for each distinct source, find its highest matching target:
	const distinctSource = [...new Set(validSources.map((ele) => ele.sourceId))];
	distinctSource.forEach((distinctSourceId) => {
		let currentDistance = -Infinity;
		let currentTargetId = '';
		validSources.forEach((source) => {
			if (distinctSourceId === source.sourceId) {
				validTargets.forEach((target) => {
					// Note here we are using the source and target groundings as a key
					if (
						source.sourceKey === target.sourceKey &&
						source.targetKey === target.targetKey &&
						target.distance > currentDistance
					) {
						currentDistance = target.distance;
						currentTargetId = target.targetId;
					}
				});
			}
		});
		result.push({ source: distinctSourceId, target: currentTargetId });
	});

	return result;
};

// Takes in a list of states and a list of datasetcolumns.
// Transforms them into generic entities with {id, groundings}
// Uses autoEntityMapping to determine 1:1 mapping
// rewrites result in form {modelVariable, datasetVariable}
export const autoCalibrationMapping = async (
	modelOptions: State[],
	datasetOptions: DatasetColumn[]
) => {
	const result = [] as CalibrateMap[];
	const acceptableDistance = 0.5;
	const sourceEntities: Entity[] = [];
	const targetEntities: Entity[] = [];

	// Fill sourceEntities with modelOptions
	modelOptions.forEach((state) => {
		if (state.grounding?.identifiers) {
			sourceEntities.push({
				id: state.id,
				groundings: Object.entries(state.grounding?.identifiers).map((ele) => ele.join(':'))
			});
		} else {
			sourceEntities.push({ id: state.id });
		}
	});

	// Fill targetEntities with datasetOptions
	datasetOptions.forEach((col) => {
		if (col.metadata?.groundings?.identifiers) {
			targetEntities.push({
				id: col.name,
				groundings: Object.entries(col.metadata?.groundings?.identifiers).map((ele) =>
					ele.join(':')
				)
			});
		} else {
			targetEntities.push({ id: col.name });
		}
	});

	const entityResult = await autoEntityMapping(sourceEntities, targetEntities, acceptableDistance);

	// rename result to CalibrateMap for users of this function
	entityResult.forEach((entity) => {
		result.push({ modelVariable: entity.source, datasetVariable: entity.target });
	});
	return result;
};

// Takes in two lists of states
// Transforms them into generic entities with {id, groundings}
// Uses autoEntityMapping to determine 1:1 mapping
// rewrites result in form {modelOneVariable, modelTwoVariable}
export const autoModelMapping = async (modelOneOptions: State[], modelTwoOptions: State[]) => {
	const result = [] as any[];
	const acceptableDistance = 0.5;
	const sourceEntities: Entity[] = [];
	const targetEntities: Entity[] = [];

	// Fill sourceEntities with modelOneOptions
	modelOneOptions.forEach((state) => {
		if (state.grounding?.identifiers) {
			sourceEntities.push({
				id: state.id,
				groundings: Object.entries(state.grounding?.identifiers).map((ele) => ele.join(':'))
			});
		} else {
			sourceEntities.push({ id: state.id });
		}
	});

	// Fill targetEntities with datasetOptions
	modelTwoOptions.forEach((state) => {
		if (state.grounding?.identifiers) {
			sourceEntities.push({
				id: state.id,
				groundings: Object.entries(state.grounding?.identifiers).map((ele) => ele.join(':'))
			});
		} else {
			sourceEntities.push({ id: state.id });
		}
	});

	const entityResult = await autoEntityMapping(sourceEntities, targetEntities, acceptableDistance);

	// rename result to CalibrateMap for users of this function
	entityResult.forEach((entity) => {
		result.push({ modelOneVariable: entity.source, modelTwoVariable: entity.target });
	});
	return result;
};
