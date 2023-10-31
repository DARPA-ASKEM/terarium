export const processFunman = (result) => {
	// List of states and parameters
	const states = result.model.petrinet.model.states.map((s) => s.id);
	const params = result.model.petrinet.semantics.ode.parameters.map((p) => p.id);

	// "dataframes"
	const boxes = [['id', 'label', 'timestep', ...params]];
	const points = [['id', 'label', 'box_id', ...params]];
	const trajs = [['box_id', 'point_id', 'timestep_id', 'time', ...states]];

	// Give IDs to all boxes
	let i = 0;
	let j = 0;

	[...result.parameter_space.true_boxes, ...result.parameter_space.false_boxes].forEach((box) => {
		box.id = `box${i}`;
		i++;

		// id, label, timestep, param bounds
		const bounds = params.map((p) => [box.bounds[p].lb, box.bounds[p].ub]);
		boxes.push([box.id, box.label, box.bounds.timestep.ub, ...bounds]);

		Object.values(box.points).forEach((point) => {
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

			const timesteps = new Set(
				Object.keys(filteredVals).map((key) => {
					const splitKey = key.split('_');
					return +splitKey[splitKey.length - 1]; // timestep
				})
			);

			timesteps.forEach((t) => {
				const stateVals = states.map((s) => filteredVals[`${s}_${t}`]);
				trajs.push([box.id, point.id, t, filteredVals[`timer_t_${t}`], ...stateVals]);
			});
		});
	});

	return { boxes, points, trajs };
};

export const getBoxes = (result, param1, param2, timestep, box_type) =>
	result.parameter_space[box_type]
		.filter((box) => box.bounds.timestep.ub === timestep)
		.map((box) => ({
			x1: box.bounds[param1].lb,
			x2: box.bounds[param1].ub,
			y1: box.bounds[param2].lb,
			y2: box.bounds[param2].ub
		}));

export const getTrajectories = (result) => processFunman(result).trajs;
