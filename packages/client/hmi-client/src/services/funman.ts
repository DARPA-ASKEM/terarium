import * as d3 from 'd3';

export const processFunman = (result: any) => {
	// List of states and parameters
	const states = result.model.petrinet.model.states.map((s) => s.id);
	const params = result.model.petrinet.semantics.ode.parameters.map((p) => p.id);

	// "dataframes"
	// const boxes = [['id', 'label', 'timestep', ...params]];
	const points = [['id', 'label', 'box_id', ...params]];

	const boxes: any[] = [];
	const trajs: any[] = [];

	// Give IDs to all boxes (i) and points (j)
	let i = 0;
	let j = 0;

	const parameterSpace = result.parameter_space;

	[...parameterSpace.true_boxes, ...parameterSpace.false_boxes].forEach((box) => {
		const boxId = `box${i}`;
		i++;

		// id, label, timestep, param bounds
		// const bounds = params.map((p) => [box.bounds[p].lb, box.bounds[p].ub]);
		// boxes.push([box.id, box.label, box.bounds.timestep.ub, ...bounds]);

		const temp = {
			id: boxId,
			label: box.label,
			timestep: box.bounds.timestep
		};
		params.forEach((p: any) => {
			temp[p] = [box.bounds[p].lb, box.bounds[p].ub];
		});
		boxes.push(temp);

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
					t
				};
				states.forEach((s) => {
					traj[s] = filteredVals[`${s}_${t}`];
				});
				trajs.push(traj);
			});
		});
	});

	return { boxes, points, states, trajs };
};

export const getBoxes = (
	processedData: any,
	param1: string,
	param2: string,
	timestep: number,
	boxType: string
) =>
	processedData.boxes
		.filter((d: any) => d.label === boxType)
		.filter((d: any) => d.timestep.ub === timestep)
		.map((d: any) => ({
			x1: d[param1][0],
			x2: d[param1][1],
			y1: d[param2][0],
			y2: d[param2][1]
		}));

export const renderFumanTrajectories = (
	element: HTMLElement,
	processedResult: any,
	boxId: string,
	options: any
) => {
	const width = options.width || 300;
	const height = options.height || 100;

	const elemSelection = d3.select(element);
	const svg = elemSelection.append('svg').attr('width', width).attr('height', height);

	const group = svg.append('g');

	const { trajs, states } = processedResult;

	const points = trajs.filter((d: any) => d.boxId === boxId);

	// FIXME: domain
	const xScale = d3.scaleLinear().domain([0, 10]).range([0, width]);
	const yScale = d3.scaleLinear().domain([0, 1]).range([height, 0]);

	const pathFn = d3
		.line<{ x: number; y: number }>()
		.x((d) => xScale(d.x))
		.y((d) => yScale(d.y))
		.curve(d3.curveBasis);

	states.forEach((s: string) => {
		const path = points.map((p: any) => ({ x: p.t, y: p[s] }));
		console.log(
			s,
			path.map((p) => p.y)
		);
		group.append('path').attr('d', pathFn(path)).style('stroke', '#888').style('fill', 'none');
	});
};

const getBoxesDomain = (boxes: any[]) => {
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

export const createBoundaryChart = (
	element: HTMLElement,
	processedData: any,
	param1: string,
	param2: string,
	timestep: number,
	options: any
) => {
	const { width, height } = options;

	const trueBoxes = getBoxes(processedData, param1, param2, timestep, 'true');
	const falseBoxes = getBoxes(processedData, param1, param2, timestep, 'false');
	const { minX, maxX, minY, maxY } = getBoxesDomain([...trueBoxes, ...falseBoxes]);

	const svg = d3.select(element).append('svg').attr('width', width).attr('height', height);
	const g = svg.append('g');

	const xScale = d3
		.scaleLinear()
		.domain([minX, maxX]) // input domain
		.range([0, width]); // output range

	const yScale = d3
		.scaleLinear()
		.domain([maxY, minY]) // input domain (inverted)
		.range([0, height]); // output range (inverted)

	const drawRects = (data: any, fill: string) => {
		g.selectAll('.rect')
			.data(data)
			.enter()
			.append('rect')
			.attr('x', (d: any) => xScale(d.x1))
			.attr('y', (d: any) => yScale(d.y2))
			.attr('width', (d: any) => xScale(d.x2) - xScale(d.x1))
			.attr('height', (d: any) => yScale(d.y1) - yScale(d.y2))
			.attr('stroke', 'black')
			.attr('fill-opacity', 0.5)
			.attr('fill', fill);
	};

	drawRects(trueBoxes, 'teal');
	drawRects(falseBoxes, 'orange');
};
