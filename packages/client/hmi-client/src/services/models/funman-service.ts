import { logger } from '@/utils/logger';
import API from '@/api/api';
import { FunmanPostQueriesRequest } from '@/types/Types';
import * as d3 from 'd3';
import { Dictionary, groupBy } from 'lodash';

export interface FunmanProcessedData {
	boxes: any[];
	points: any[];
	states: string[];
	trajs: any[];
}

export interface RenderOptions {
	width: number;
	height: number;
}

export async function getQueries(id: string) {
	try {
		const response = await API.get(`/funman/queries/${id}`);
		return response.data;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

export async function makeQueries(body: FunmanPostQueriesRequest) {
	try {
		const resp = await API.post('/funman/queries', body);
		const output = resp.data;
		return output;
	} catch (err) {
		logger.error(err);
		return null;
	}
}

export async function haltQuery(id: string) {
	try {
		const response = await API.get(`/funman/queries/${id}/halt`);
		return response.data;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

export const processFunman = (result: any) => {
	// List of states and parameters
	const states = result.model.petrinet.model.states.map((s) => s.id);
	const params = result.model.petrinet.semantics.ode.parameters.map((p) => p.id);

	// "dataframes"
	const points = [['id', 'label', 'box_id', ...params]];
	const boxes: any[] = [];
	const trajs: any[] = [];

	// Give IDs to all boxes (i) and points (j)
	let i = 0;
	let j = 0;

	const parameterSpace = result.parameter_space;
	let trueBoxes: any[] = [];
	let falseBoxes: any[] = [];

	if (parameterSpace.true_boxes) {
		trueBoxes = parameterSpace.true_boxes;
	}
	if (parameterSpace.false_boxes) {
		falseBoxes = parameterSpace.false_boxes;
	}

	[...trueBoxes, ...falseBoxes].forEach((box) => {
		const boxId = `box${i}`;
		i++;

		const temp = {
			id: boxId,
			label: box.label,
			timestep: box.bounds.timestep
		};
		params.forEach((p: any) => {
			temp[p] = [box.bounds[p].lb, box.bounds[p].ub];
		});
		boxes.push(temp);

		if (box.points) {
			Object.values(box.points).forEach((point: any) => {
				point.id = `point${j}`;
				j++;

				// id, label, box_id, param values
				const values = params.map((p: any) => point.values[p]);
				points.push([point.id, point.label, boxId, ...values]);

				// Get trajectories
				const filteredVals = Object.keys(point.values)
					.filter(
						(key) => !params.includes(key) && key !== 'timestep' && key.split('_')[0] !== 'assume'
					)
					.reduce((obj, key) => {
						obj[key] = point.values[key];
						return obj;
					}, {});

				const timesteps = [
					...new Set(
						Object.keys(filteredVals).map((key) => {
							const splitKey = key.split('_');
							return +splitKey[splitKey.length - 1]; // timestep
						})
					)
				].sort((a, b) => a - b);

				timesteps.forEach((t) => {
					const traj: any = {
						boxId,
						pointId: point.id,
						timestep: t
					};
					states.forEach((s) => {
						traj[s] = filteredVals[`${s}_${t}`];
					});
					trajs.push(traj);
				});
			}); // foreach point
		} // box.points
	}); // foreach box

	return { boxes, points, states, trajs } as FunmanProcessedData;
};

interface FunmanBox {
	id: string;
	x1: number;
	y1: number;
	x2: number;
	y2: number;
}
export const getBoxes = (
	processedData: FunmanProcessedData,
	param1: string,
	param2: string,
	timestep: number,
	boxType: string
) => {
	const result: FunmanBox[] = [];
	processedData.boxes
		.filter((d: any) => d.label === boxType)
		.filter((d: any) => d.timestep.ub === timestep)
		.forEach((d: any) => {
			result.push({
				id: d.id,
				x1: d[param1][0],
				x2: d[param1][1],
				y1: d[param2][0],
				y2: d[param2][1]
			});
		});

	return result;
};

export const renderFumanTrajectories = (
	element: HTMLElement,
	processedData: FunmanProcessedData,
	state: string,
	options: RenderOptions
) => {
	const width = options.width;
	const height = options.height;
	const topMargin = 10;
	const rightMargin = 30;
	const leftMargin = 30;
	const bottomMargin = 30;
	const { trajs, states } = processedData;

	const elemSelection = d3.select(element);
	d3.select(element).selectAll('*').remove();
	const svg = elemSelection.append('svg').attr('width', width).attr('height', height);

	const points: Dictionary<any> = groupBy(trajs, 'boxId');

	// Find max/min across timesteps
	const xDomain = d3.extent(trajs.map((d) => d.timestep)) as [number, number];

	// Find max/min across all state values
	const yDomain = d3.extent(
		trajs.map((d) => states.filter((s) => s === state).map((s) => d[s])).flat()
	) as [number, number];

	const xScale = d3
		.scaleLinear()
		.domain(xDomain)
		.range([leftMargin, width - rightMargin]);

	const yScale = d3
		.scaleLinear()
		.domain(yDomain)
		.range([height - bottomMargin, topMargin]);

	// Add the x-axis.
	svg
		.append('g')
		.attr('transform', `translate(0,${height - bottomMargin})`)
		.call(
			d3
				.axisBottom(xScale)
				.ticks(width / 60)
				.tickSizeOuter(0)
		);

	// Add the y-axis
	svg
		.append('g')
		.attr('transform', `translate(${leftMargin},0)`)
		.call(
			d3
				.axisLeft(yScale)
				.ticks(height / 60)
				.tickSizeOuter(0)
		);

	// Add label for x-axis
	svg
		.append('text')
		.attr('class', 'x label')
		.attr('text-anchor', 'end')
		.attr('x', width / 2)
		.attr('y', height - 2)
		.text('Timestep');

	const pathFn = d3
		.line<{ x: number; y: number }>()
		.x((d) => xScale(d.x))
		.y((d) => yScale(d.y))
		.curve(d3.curveBasis);

	Object.keys(points).forEach((boxId) => {
		const path = points[boxId].map((p: any) => ({ x: p.timestep, y: p[state] }));
		svg
			.append('g')
			.append('path')
			.attr('d', pathFn(path))
			.style('stroke', '#888')
			.style('fill', 'none');
	});
};

const getBoxesDomain = (boxes: FunmanBox[]) => {
	let minX = Number.MAX_VALUE;
	let maxX = Number.MIN_VALUE;
	let minY = Number.MAX_VALUE;
	let maxY = Number.MIN_VALUE;

	boxes.forEach((box) => {
		minX = Math.min(minX, box.x1);
		maxX = Math.max(maxX, box.x2);
		minY = Math.min(minY, box.y1);
		maxY = Math.max(maxY, box.y2);
	});

	return { minX, maxX, minY, maxY };
};

export const renderFunmanBoundaryChart = (
	element: HTMLElement,
	processedData: FunmanProcessedData,
	param1: string,
	param2: string,
	timestep: number,
	options: RenderOptions
) => {
	const { width, height } = options;

	const trueBoxes = getBoxes(processedData, param1, param2, timestep, 'true');
	const falseBoxes = getBoxes(processedData, param1, param2, timestep, 'false');
	const { minX, maxX, minY, maxY } = getBoxesDomain([...trueBoxes, ...falseBoxes]);

	d3.select(element).selectAll('*').remove();
	const svg = d3.select(element).append('svg').attr('width', width).attr('height', height);
	const g = svg.append('g');

	const xScale = d3
		.scaleLinear()
		.domain([minX, maxX]) // input domain
		.range([0, width]); // output range

	const yScale = d3
		.scaleLinear()
		.domain([minY, maxY]) // input domain (inverted)
		.range([height, 0]); // output range (inverted)

	g.selectAll('.true-box')
		.data(trueBoxes)
		.enter()
		.append('rect')
		.classed('true-box', true)
		.attr('fill', 'teal');

	g.selectAll('.false-box')
		.data(falseBoxes)
		.enter()
		.append('rect')
		.classed('false-box', true)
		.attr('fill', 'orange');

	g.selectAll<any, FunmanBox>('rect')
		.attr('x', (d) => xScale(d.x1))
		.attr('y', (d) => yScale(d.y2))
		.attr('width', (d) => xScale(d.x2) - xScale(d.x1))
		.attr('height', (d) => yScale(d.y1) - yScale(d.y2))
		.attr('stroke', '#888')
		.attr('fill-opacity', 0.5);

	g.selectAll('text')
		.data([...trueBoxes, ...falseBoxes])
		.enter()
		.append('text')
		.attr('x', (d) => xScale(d.x1))
		.attr('y', (d) => 15 + yScale(d.y2))
		.style('stroke', 'none')
		.style('fill', '#333')
		.text((d) => d.id);
};
