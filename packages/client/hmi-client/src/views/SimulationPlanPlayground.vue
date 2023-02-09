<script lang="ts">
import _ from 'lodash';
import { defineComponent } from 'vue';
import * as d3 from 'd3';
import graphScaffolder, { IGraph } from '@graph-scaffolder/index';
import { parseSimulationPlan } from '@/services/simulation';
import { runDagreLayout, D3SelectionINode, D3SelectionIEdge } from '@/services/graph';
import logger from '@/utils/logger';

const plan = {
	InPort: [
		{ in_port_box: 1, in_port_type: 'Simplex' },
		{ in_port_box: 2, in_port_type: 'Simplex' },
		{ in_port_box: 3, in_port_type: 'Simplex' },
		{ in_port_box: 3, in_port_type: 'Simplex' },
		{ in_port_box: 5, in_port_type: 'Petri' },
		{ in_port_box: 5, in_port_type: 'Number' },
		{ in_port_box: 5, in_port_type: 'Number' },
		{ in_port_box: 5, in_port_type: 'Simplex' },
		{ in_port_box: 7, in_port_type: 'Petri' },
		{ in_port_box: 7, in_port_type: 'Number' },
		{ in_port_box: 7, in_port_type: 'Number' },
		{ in_port_box: 7, in_port_type: 'Simplex' },
		{ in_port_box: 8, in_port_type: 'Trajectory' },
		{ in_port_box: 9, in_port_type: 'Trajectory' },
		{ in_port_box: 10, in_port_type: 'Number' },
		{ in_port_box: 10, in_port_type: 'Number' }
	],
	Wire: [
		{ src: 4, tgt: 5, wire_value: null },
		{ src: 1, tgt: 3, wire_value: null },
		{ src: 1, tgt: 8, wire_value: null },
		{ src: 9, tgt: 16, wire_value: null },
		{ src: 5, tgt: 13, wire_value: null },
		{ src: 7, tgt: 14, wire_value: null },
		{ src: 6, tgt: 9, wire_value: null },
		{ src: 2, tgt: 4, wire_value: null },
		{ src: 3, tgt: 12, wire_value: null },
		{ src: 8, tgt: 15, wire_value: null }
	],
	OuterOutPort: [
		{
			outer_out_port_type: 'Number'
		}
	],
	InWire: [
		{ in_src: 1, in_tgt: 1, in_wire_value: null },
		{ in_src: 2, in_tgt: 2, in_wire_value: null },
		{ in_src: 3, in_tgt: 6, in_wire_value: null },
		{ in_src: 4, in_tgt: 7, in_wire_value: null },
		{ in_src: 3, in_tgt: 10, in_wire_value: null },
		{ in_src: 4, in_tgt: 11, in_wire_value: null }
	],
	OuterInPort: [
		{ outer_in_port_type: 'Simplex' },
		{ outer_in_port_type: 'Simplex' },
		{ outer_in_port_type: 'Number' },
		{ outer_in_port_type: 'Number' }
	],
	OutWire: [
		{
			out_src: 10,
			out_tgt: 1,
			out_wire_value: null
		}
	],
	Box: [
		{ value: 'multinomial', box_type: 'Catlab.WiringDiagrams.DirectedWiringDiagrams.Box{Symbol}' },
		{ value: 'multinomial', box_type: 'Catlab.WiringDiagrams.DirectedWiringDiagrams.Box{Symbol}' },
		{ value: 'ptwisesum', box_type: 'Catlab.WiringDiagrams.DirectedWiringDiagrams.Box{Symbol}' },
		{ value: 'P', box_type: 'Catlab.WiringDiagrams.DirectedWiringDiagrams.Box{Symbol}' },
		{ value: 'solve', box_type: 'Catlab.WiringDiagrams.DirectedWiringDiagrams.Box{Symbol}' },
		{ value: 'P', box_type: 'Catlab.WiringDiagrams.DirectedWiringDiagrams.Box{Symbol}' },
		{ value: 'solve', box_type: 'Catlab.WiringDiagrams.DirectedWiringDiagrams.Box{Symbol}' },
		{ value: 'measure', box_type: 'Catlab.WiringDiagrams.DirectedWiringDiagrams.Box{Symbol}' },
		{ value: 'measure', box_type: 'Catlab.WiringDiagrams.DirectedWiringDiagrams.Box{Symbol}' },
		{ value: 'difference', box_type: 'Catlab.WiringDiagrams.DirectedWiringDiagrams.Box{Symbol}' }
	],
	OutPort: [
		{ out_port_box: 1, out_port_type: 'Simplex' },
		{ out_port_box: 2, out_port_type: 'Simplex' },
		{ out_port_box: 3, out_port_type: 'Simplex' },
		{ out_port_box: 4, out_port_type: 'Petri' },
		{ out_port_box: 5, out_port_type: 'Trajectory' },
		{ out_port_box: 6, out_port_type: 'Petri' },
		{ out_port_box: 7, out_port_type: 'Trajectory' },
		{ out_port_box: 8, out_port_type: 'Number' },
		{ out_port_box: 9, out_port_type: 'Number' },
		{ out_port_box: 10, out_port_type: 'Number' }
	],
	PassWire: []
};

interface NodeData {
	boxType: string;
	label: string;
}
interface EdgeData {}

const MARKER_VIEWBOX = '-5 -5 10 10';
const ARROW = 'M 0,-3.25 L 5 ,0 L 0,3.25';
const pathFn = d3
	.line<{ x: number; y: number }>()
	.x((d) => d.x)
	.y((d) => d.y);

class SimulationPlanRenderer extends graphScaffolder.BasicRenderer<NodeData, EdgeData> {
	setupDefs() {
		const svg = d3.select(this.svgEl);

		// Clean up
		svg.select('defs').selectAll('.edge-marker-end').remove();

		// Arrow defs
		svg
			.select('defs')
			.append('marker')
			.classed('edge-marker-end', true)
			.attr('id', 'arrowhead')
			.attr('viewBox', MARKER_VIEWBOX)
			.attr('refX', 2)
			.attr('refY', 0)
			.attr('orient', 'auto')
			.attr('markerWidth', 15)
			.attr('markerHeight', 15)
			.attr('markerUnits', 'userSpaceOnUse')
			.attr('xoverflow', 'visible')
			.append('svg:path')
			.attr('d', ARROW)
			.style('fill', '#000')
			.style('stroke', 'none');
	}

	renderNodes(selection: D3SelectionINode<NodeData>) {
		selection
			.append('rect')
			.classed('shape', true)
			.attr('width', (d) => d.width)
			.attr('height', (d) => d.height)
			.style('fill', '#DDD')
			.style('stroke', '#888');

		selection
			.append('text')
			.attr('y', -5)
			.text((d) => d.data.label);
	}

	renderEdges(selection: D3SelectionIEdge<EdgeData>) {
		selection
			.append('path')
			.attr('d', (d) => pathFn(d.points))
			.style('fill', 'none')
			.style('stroke', '#000')
			.style('stroke-width', 2)
			.attr('marker-end', 'url(#arrowhead)');
	}
}

let g: IGraph<NodeData, EdgeData> = {
	width: 800,
	height: 800,
	nodes: [],
	edges: []
};

export default defineComponent({
	name: 'SimulationPlayground',
	async mounted() {
		const graph = parseSimulationPlan(plan);

		// Initialize
		graph.nodes.forEach((d) => {
			logger.info(`node >> ${d}`);
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
			logger.info(`edge >> ${d}`);
			g.edges.push({
				id: `${i}`,
				source: d.source,
				target: d.target,
				points: [],
				data: {}
			});
		});

		g = runDagreLayout(_.cloneDeep(g));

		const playground = document.getElementById('playground') as HTMLDivElement;
		const renderer = new SimulationPlanRenderer({
			el: playground ?? undefined,
			useAStarRouting: true,
			runLayout: runDagreLayout
		});
		await renderer.setData(g);
		await renderer.render();
	}
});
</script>

<template>
	<section class="playground">
		<div id="playground" class="playground-panel"></div>
	</section>
</template>

<style scoped>
.playground {
	margin: 10px;
}

.playground-panel {
	width: 800px;
	height: 800px;
	border: 1px solid #888;
}
</style>
