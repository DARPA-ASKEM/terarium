import { logger } from '@/utils/logger';
import API from '@/api/api';
import type { FunmanInterval, FunmanPostQueriesRequest } from '@/types/Types';
import * as d3 from 'd3';
import { ConstraintGroup, ConstraintType } from '@/components/workflow/ops/funman/funman-operation';

// Partially typing Funman response
interface FunmanBound {
	lb: number;
	ub: number;
}
export interface FunmanBox {
	id: string;
	label: string;
	timestep: FunmanBound;
	parameters: Record<string, FunmanBound>;
}

export interface FunmanConstraintsResponse {
	soft: boolean;
	name: string;
	timepoints: FunmanInterval;
	additive_bounds: FunmanInterval;
	variables: string[];
	weights: number[];
	derivative: boolean;
}

export interface ProcessedFunmanResult {
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

export async function makeQueries(body: FunmanPostQueriesRequest, modelId: string) {
	try {
		const resp = await API.post('/funman/queries', body, { params: { 'model-id': modelId } });
		const output = resp.data;
		return output;
	} catch (err) {
		logger.error(err);
		return null;
	}
}

export function generateConstraintExpression(config: ConstraintGroup) {
	const { constraintType, interval, variables, timepoints } = config;
	let expression = '';
	for (let i = 0; i < variables.length; i++) {
		let expressionPart = `${variables[i]}(t)`;
		if (constraintType === ConstraintType.Increasing || constraintType === ConstraintType.Decreasing) {
			expressionPart = `d/dt ${expressionPart}`;
		}
		if (i === variables.length - 1) {
			if (
				constraintType === ConstraintType.LessThan ||
				constraintType === ConstraintType.LessThanOrEqualTo ||
				constraintType === ConstraintType.Decreasing
			) {
				expressionPart += constraintType === ConstraintType.LessThan ? `<` : `\\leq`;
				expressionPart += constraintType === ConstraintType.Decreasing ? '0' : `${interval?.ub ?? 0}`;
			} else {
				expressionPart += constraintType === ConstraintType.GreaterThan ? `>` : `\\geq`;
				expressionPart += constraintType === ConstraintType.Increasing ? '0' : `${interval?.lb ?? 0}`;
			}
		} else {
			expressionPart += ',';
		}
		expression += expressionPart;
	}
	// Adding the "for all in timepoints" in the same expression helps with text alignment
	return `${expression} \\ \\forall \\ t \\in [${timepoints.lb}, ${timepoints.ub}]`;
}

export const processFunman = (result: any) => {
	const stateIds: string[] = result.model.petrinet.model.states.map(({ id }) => id);
	const parameterIds: string[] = result.model.petrinet.semantics.ode.parameters.map(({ id }) => id);
	const timepoints: number[] = result.request.structure_parameters[0].schedules[0].timepoints;

	// We only want boxes that appear at the last timestep
	function getBoxesEndingAtLastTimestep(boxes: any[]) {
		return boxes.filter((box) => box.points[0]?.values.timestep === timepoints.length - 1);
	}
	const trueBoxes: any[] = getBoxesEndingAtLastTimestep(result.parameter_space.true_boxes);
	const falseBoxes: any[] = getBoxesEndingAtLastTimestep(result.parameter_space.false_boxes);

	// "dataframes"
	const points = [['id', 'label', 'box_id', ...parameterIds]];
	const boxes: FunmanBox[] = [];
	const trajs: any[] = [];

	let pointIndex = 0;
	[...trueBoxes, ...falseBoxes].forEach((box, boxIndex) => {
		// Add box
		const boxId = `box${boxIndex}`;
		boxes.push({
			id: boxId,
			label: box.label,
			timestep: box.bounds.timestep,
			parameters: Object.fromEntries(parameterIds.map((p: any) => [p, box.bounds[p]]))
		});

		// Add point
		const point = box.points[0];
		const pointId = `point${pointIndex}`;
		// id, label, box id, ...values of parameter ids
		points.push([pointId, point.label, boxId, ...parameterIds.map((p: any) => point.values[p])]);
		pointIndex++;

		// Get trajectories
		const filteredVals = Object.keys(point.values)
			.filter((key) => !parameterIds.includes(key) && key !== 'timestep' && key.split('_')[0] !== 'assume')
			.reduce((obj, key) => {
				obj[key] = point.values[key];
				return obj;
			}, {});

		timepoints.forEach((t) => {
			let pushFlag = true;
			const traj: any = {
				boxId,
				label: box.label,
				pointId,
				timestep: t,
				n: point.values.timestep // how many actual points
			};
			stateIds.forEach((s) => {
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
	});

	return { boxes, points, states: stateIds, trajs } as ProcessedFunmanResult;
};

interface FunmanBoundingBox {
	id: string;
	x1: number;
	y1: number;
	x2: number;
	y2: number;
}
export const getBoxes = (processedData: ProcessedFunmanResult, param1: string, param2: string, boxType: string) => {
	const result: FunmanBoundingBox[] = [];

	const temp = processedData.boxes
		.filter((d: any) => d.label === boxType)
		.map((box: any) => ({ id: box.id, timestep: box.timestep.ub }));
	if (temp.length === 0) return [];

	// grab latest step
	const step = temp.sort((a, b) => b.timestep - a.timestep)[0].timestep;

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
	processedData: ProcessedFunmanResult,
	param1: string,
	param2: string,
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
