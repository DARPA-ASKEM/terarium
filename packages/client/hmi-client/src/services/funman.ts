import * as d3 from 'd3';

export const processFunman = (result: any) => {
	// List of states and parameters
	const states = result.model.petrinet.model.states.map((s) => s.id);
	const params = result.model.petrinet.semantics.ode.parameters.map((p) => p.id);

	// "dataframes"
	const boxes = [['id', 'label', 'timestep', ...params]];
	const points = [['id', 'label', 'box_id', ...params]];
	const trajs = [['box_id', 'point_id', 'timestep_id', 'time', ...states]];

	// Give IDs to all boxes (i) and points (j)
	let i = 0;
	let j = 0;

	const parameterSpace = result.parameter_space;

	[...parameterSpace.true_boxes, ...parameterSpace.false_boxes].forEach((box) => {
		box.id = `box${i}`;
		i++;

		// id, label, timestep, param bounds
		const bounds = params.map((p) => [box.bounds[p].lb, box.bounds[p].ub]);
		boxes.push([box.id, box.label, box.bounds.timestep.ub, ...bounds]);

		Object.values(box.points).forEach((point: any) => {
			point.id = `point${j}`;
			j++;

			// id, label, box_id, param values
			const values = params.map((p) => point.values[p]);
			points.push([point.id, point.label, box.id, ...values]);

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
				// box_id, point_id, timestep_id, time, state values
				const stateVals = states.map((s) => filteredVals[`${s}_${t}`]);
				trajs.push([box.id, point.id, t, filteredVals[`timer_t_${t}`], ...stateVals]);
			});
		});
	});

	return { boxes, points, trajs };
};

export const getBoxes = (
	result: any,
	param1: string,
	param2: string,
	timestep: number,
	boxType: string
) =>
	result.parameter_space[boxType]
		.filter((box: any) => box.bounds.timestep.ub === timestep)
		.map((box: any) => ({
			x1: box.bounds[param1].lb,
			x2: box.bounds[param1].ub,
			y1: box.bounds[param2].lb,
			y2: box.bounds[param2].ub
		}));

export const getTrajectories = (result: any) => processFunman(result).trajs;

export const createBoundaryChart = (element: HTMLElement, payload: any, options: any) => {
	const { width, height, xAxis, yAxis } = options;

	const svg = d3.select(element).attr('width', width).attr('height', height);
	const g = svg.append('g');

	const xScale = d3
		.scaleLinear()
		.domain([0, xAxis]) // input domain
		.range([0, width]); // output range

	const yScale = d3
		.scaleLinear()
		.domain([0, yAxis]) // input domain
		.range([0, height]); // output range

	const drawRects = (data, fill) => {
		g.selectAll('.rect')
			.data(data)
			.enter()
			.append('rect')
			.attr('x', (d: any) => xScale(d.x1))
			.attr('y', (d: any) => yScale(d.y1))
			.attr('width', (d: any) => xScale(d.x2) - xScale(d.x1))
			.attr('height', (d: any) => yScale(d.y2) - yScale(d.y1))
			.attr('stroke', 'black')
			.attr('fill-opacity', 0.5)
			.attr('fill', fill);
	};

	const trueBoxes = getBoxes(payload, 'beta', 'gamma', 7, 'true_boxes');
	const falseBoxes = getBoxes(payload, 'beta', 'gamma', 7, 'false_boxes');

	drawRects(trueBoxes, 'teal');
	drawRects(falseBoxes, 'orange');
};
