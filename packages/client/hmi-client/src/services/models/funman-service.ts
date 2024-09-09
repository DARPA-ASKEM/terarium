import { logger } from '@/utils/logger';
import API from '@/api/api';
import type { FunmanPostQueriesRequest } from '@/types/Types';
import * as d3 from 'd3';
import { Dictionary, groupBy } from 'lodash';

// Partially typing Funman response
interface FunmanBound {
	lb: number;
	ub: number;
}
export interface FunmanBox {
	id?: string;
	label: string;
	bounds: Record<string, FunmanBound>;
	explanation: any;
	schedule: any;
	points: any;
}
export interface FunmanProcessedData {
	boxes: FunmanBox[];
	points: any[];
	states: string[];
	trajs: any[];
}

export interface RenderOptions {
	width: number;
	height: number;

	constraints?: any[];
	click?: Function;
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

export const processFunman = (result: any) => {
	// List of states and parameters
	const states = result.model.petrinet.model.states.map((s) => s.id);
	const params = result.model.petrinet.semantics.ode.parameters.map((p) => p.id);
	const timesteps = result.request.structure_parameters.at(0).schedules.at(0).timepoints;

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
					.filter((key) => !params.includes(key) && key !== 'timestep' && key.split('_')[0] !== 'assume')
					.reduce((obj, key) => {
						obj[key] = point.values[key];
						return obj;
					}, {});

				timesteps.forEach((t) => {
					let pushFlag = true;
					const traj: any = {
						boxId,
						label: box.label,
						pointId: point.id,
						timestep: t,
						n: point.values.timestep // how many actual points
					};
					states.forEach((s) => {
						// Only push states that have a timestep key pair
						if (Object.prototype.hasOwnProperty.call(filteredVals, `${s}_${t}`)) {
							traj[s] = filteredVals[`${s}_${t}`];
						} else {
							pushFlag = false;
						}
					});
					if (pushFlag) {
						trajs.push(traj);
					}
				});
			}); // foreach point
		} // box.points
	}); // foreach box

	return { boxes, points, states, trajs } as FunmanProcessedData;
};

interface FunmanBoundingBox {
	id: string;
	x1: number;
	y1: number;
	x2: number;
	y2: number;
}
export const getBoxes = (processedData: FunmanProcessedData, param1: string, param2: string, boxType: string) => {
	const result: FunmanBoundingBox[] = [];

	const temp = processedData.boxes
		.filter((d: any) => d.label === boxType)
		.map((box: any) => ({ id: box.id, timestep: box.timestep.ub }));
	if (temp.length === 0) return [];

	// grab latest step
	const step = temp.sort((a, b) => b.timestep - a.timestep)[0].timestep;

	// console.group(boxType);
	// console.log('boxes', processedData.boxes);
	// console.log('step', step);
	// console.groupEnd();

	processedData.boxes
		.filter((d: any) => d.label === boxType)
		.filter((d: any) => d.timestep.ub === step)
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
	selectedBoxId: string,
	options: RenderOptions
) => {
	const width = options.width;
	const height = options.height;
	const topMargin = 10;
	const rightMargin = 30;
	const leftMargin = 35;
	const bottomMargin = 30;
	const { trajs, states } = processedData;

	const elemSelection = d3.select(element);
	d3.select(element).selectAll('*').remove();
	const svg = elemSelection.append('svg').attr('width', width).attr('height', height);

	const points: Dictionary<any> = groupBy(trajs, 'boxId');

	// Find max/min across timesteps
	const xDomain = d3.extent(trajs.map((d) => d.timestep)) as [number, number];

	// Find max/min across all state values
	const yDomain = d3.extent(trajs.map((d) => states.filter((s) => s === state).map((s) => d[s])).flat()) as [
		number,
		number
	];

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
		.y((d) => yScale(d.y));

	Object.keys(points).forEach((boxId) => {
		const label = points[boxId][0].label;
		let path = points[boxId].map((p: any) => ({ x: p.timestep, y: p[state] }));

		// FIXME: funman can set the value to 0 at time t, if the trajectory ends at t
		// This makes the linecharts really weird, until this is addressed we will ignore
		// the last point if it is 0
		const n = points[boxId][0].n;
		if (n >= 0) {
			path = path.filter((_d: any, i: number) => i <= n);
		}

		if (path.length > 1) {
			svg
				.append('g')
				.append('path')
				.attr('d', pathFn(path))
				.style('stroke', label === 'true' ? 'teal' : 'orange')
				.style('stroke-width', () => {
					if (selectedBoxId === '' || selectedBoxId === boxId) return 2.0;
					return 1.0;
				})
				.style('opacity', () => {
					if (selectedBoxId === '' || selectedBoxId === boxId) return 0.75;
					return 0.05;
				})
				.style('fill', 'none');

			svg
				.selectAll(`.${boxId}`)
				.data(path)
				.enter()
				.append('circle')
				.attr('cx', (d: any) => xScale(d.x))
				.attr('cy', (d: any) => yScale(d.y))
				.attr('r', 2)
				.style('opacity', () => {
					if (selectedBoxId === '' || selectedBoxId === boxId) return 0.75;
					return 0.05;
				})
				.style('fill', label === 'true' ? 'teal' : 'orange');
		} else if (path.length === 1) {
			svg
				.append('g')
				.append('circle')
				.attr('cx', xScale(path[0].x))
				.attr('cy', yScale(path[0].y))
				.attr('r', 4)
				.style('fill', '#888');
		}
	});

	// Render constraints
	// Since this is variable-vs-time, we can't display constraints that involve more
	// than one variable, and just the selected variable
	if (options.constraints && options.constraints.length > 0) {
		const singleVariableConstraints = options.constraints.filter((d) => d.variable).filter((d) => d.variable === state);

		svg
			.selectAll('.constraint-box')
			.data(singleVariableConstraints)
			.enter()
			.append('rect')
			.classed('constraint-box', true)
			.attr('x', (d) => xScale(d.timepoints?.lb as number))
			.attr('y', (d) => yScale(d.interval?.ub as number))
			.attr('width', (d) => {
				const w = xScale(d.timepoints?.ub as number) - xScale(d.timepoints?.lb as number);
				return w;
			})
			.attr('height', (d) => Math.abs(yScale(d.interval?.ub as number) - yScale(d.interval?.lb as number)))
			.style('fill', 'teal')
			.style('fill-opacity', 0.3);
	}

	return svg;
};

const getBoxesDomain = (boxes: FunmanBoundingBox[]) => {
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
	_timestep: number,
	selectedBoxId: string,
	options: RenderOptions
) => {
	const { width, height } = options;

	let margin = 30;
	if (height < 100 || width < 100) {
		margin = 5;
	}

	const trueBoxes = getBoxes(processedData, param1, param2, 'true');
	const falseBoxes = getBoxes(processedData, param1, param2, 'false');

	const { minX, maxX, minY, maxY } = getBoxesDomain([...trueBoxes, ...falseBoxes]);

	d3.select(element).selectAll('*').remove();
	const svg = d3.select(element).append('svg').attr('width', width).attr('height', height);
	const g = svg.append('g');

	const xScale = d3
		.scaleLinear()
		.domain([minX, maxX]) // input domain
		.range([margin, width - margin]); // output range

	const yScale = d3
		.scaleLinear()
		.domain([minY, maxY]) // input domain (inverted)
		.range([height - margin, margin]); // output range (inverted)

	g.selectAll('.true-box').data(trueBoxes).enter().append('rect').classed('true-box', true).attr('fill', 'teal');
	g.selectAll('.false-box').data(falseBoxes).enter().append('rect').classed('false-box', true).attr('fill', 'orange');

	g.selectAll<any, FunmanBoundingBox>('rect')
		.attr('x', (d) => xScale(d.x1))
		.attr('y', (d) => yScale(d.y2))
		.attr('width', (d) => xScale(d.x2) - xScale(d.x1))
		.attr('height', (d) => yScale(d.y1) - yScale(d.y2))
		.attr('stroke', '#888')
		.attr('fill-opacity', 0.5)
		.on('click', (_evt: PointerEvent, d: any) => {
			// Invoke callback
			if (options.click) {
				options.click(d);
			}
		});

	if (selectedBoxId !== '') {
		const boundBox = [...trueBoxes, ...falseBoxes].find((box) => box.id === selectedBoxId);
		if (!boundBox) return;

		g.selectAll('.select-marker')
			.data([
				[boundBox.x1, boundBox.y1],
				[boundBox.x2, boundBox.y1],
				[boundBox.x2, boundBox.y2],
				[boundBox.x1, boundBox.y2]
			])
			.enter()
			.append('circle')
			.classed('select-marker', true)
			.attr('cx', (d) => xScale(d[0]))
			.attr('cy', (d) => yScale(d[1]))
			.attr('r', 4)
			.style('stroke', '#888')
			.style('fill', '#bbb');
	}

	if (options.click) {
		g.selectAll<any, FunmanBoundingBox>('rect').style('cursor', 'pointer');
	}

	// Don't render text into tight spaces
	if (height < 100 || width < 100) return;

	// Border box
	g.append('rect')
		.attr('x', margin)
		.attr('y', margin)
		.attr('width', width - 2 * margin)
		.attr('height', height - 2 * margin)
		.attr('stroke', '#888')
		.attr('fill', 'transparent')
		.style('pointer-events', 'none');

	g.selectAll('text')
		.data([...trueBoxes, ...falseBoxes])
		.enter()
		.append('text')
		.attr('x', (d) => xScale(d.x1))
		.attr('y', (d) => 15 + yScale(d.y2))
		.style('stroke', 'none')
		.style('fill', '#333')
		.text((d) => d.id)
		.style('pointer-events', 'none');

	const xaxisH = height - margin + 13;

	// xaxis-label
	g.append('text')
		.attr('x', 0.5 * width)
		.attr('y', xaxisH)
		.text(param1);

	// yaxis-label
	g.append('g')
		.attr('transform', `translate(15, ${0.5 * height})`)
		.append('text')
		.style('text-anchor', 'middle')
		.style('transform', 'rotate(270deg)')
		.text(param2);

	g.append('text').attr('x', margin).attr('y', xaxisH).style('font-size', '12').text(minX);
	g.append('text')
		.attr('x', width - margin)
		.attr('y', xaxisH)
		.style('font-size', '12')
		.text(maxX);

	g.append('text')
		.attr('x', margin - 2)
		.attr('y', xaxisH - 12)
		.style('text-anchor', 'end')
		.style('font-size', '12')
		.text(minY);
	g.append('text')
		.attr('x', margin - 2)
		.attr('y', margin)
		.style('text-anchor', 'end')
		.style('font-size', '12')
		.text(maxY);
};
