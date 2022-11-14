import { IGraph } from '@graph-scaffolder/types';

interface SimulationPlan {
	// Ports, indexed by Box
	InPort: { in_port_box: number; in_port_type: string }[];
	OutPort: { out_port_box: number; out_port_type: string }[];

	// Edges data, indexed by InPort and OutPort indices
	Wire: { src: number; tgt: number; wire_value: any }[];

	// Nodes data
	Box: { value: string; box_type: string }[];

	// Not currently parsed - Nov 2022
	PassWire: any;
	InWire: { in_src: number; in_tgt: number; in_wire_value: any }[];
	OutWire: { out_src: number; out_tgt: number; out_wire_value: any }[];

	OuterOutPort: { outer_out_port_type: string }[];
	OuterInPort: { outer_in_port_type: string }[];
}

/**
 * Given a simulation plan wiring diagram, convert into a node/edges graph
 * format.
 *
 * Note currently this only processes: "InPort", "OutPort", "Wire", "Box"
 */
export const parseSimulationPlan = (plan: SimulationPlan) => {
	const nodes = plan.Box.map((d, i) => ({
		id: `${i}`,
		name: d.value,
		boxType: d.box_type
	}));

	const inPortIndex = plan.InPort;
	const outPortIndex = plan.OutPort;

	// eslint-disable-next-line
	const edges = plan.Wire.map((d) => {
		return {
			source: `${outPortIndex[d.src - 1].out_port_box - 1}`,
			target: `${inPortIndex[d.tgt - 1].in_port_box - 1}`
		};
	});
	return { nodes, edges };
};

/**
 * Given a simulation plan wiring diagram, convert to an IGraph representation
 * for the renderer
 */
export const parseSimulationPlan2IGraph = (plan: SimulationPlan) => {
	const graph = parseSimulationPlan(plan);
	const g: IGraph<any, any> = { nodes: [], edges: [] };

	// Initialize
	graph.nodes.forEach((d) => {
		g.nodes.push({
			id: d.id,
			label: d.id, // simulation plan currently allows duplicate labels which create tracking problems in the renderer
			x: 0,
			y: 0,
			width: 40,
			height: 40,
			nodes: [],
			data: {
				boxType: d.boxType,
				label: d.name
			}
		});
	});

	graph.edges.forEach((d, i) => {
		console.log('edge >>', d);
		g.edges.push({
			id: `${i}`,
			source: d.source,
			target: d.target,
			points: [],
			data: {}
		});
	});

	return g;
};
