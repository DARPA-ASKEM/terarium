<script setup lang="ts">
import { onMounted } from 'vue';
import graphScaffolder from '@graph-scaffolder/index';
import { runDagreLayout, D3SelectionINode, D3SelectionIEdge } from '@/services/graph';
import { parseSimulationPlan2IGraph } from '@/services/simulation';
import * as d3 from 'd3';

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
			.append('rect')
			.classed('shape', true)
			.attr('x', (d) => d.width)
			.attr('width', (d) => d.width)
			.attr('height', (d) => d.height)
			.style('fill', '#48B')
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

// FIXME: Testing plan
const plan = {
	InPort: [
		{ in_port_box: 2, in_port_type: '213' },
		{ in_port_box: 3, in_port_type: '213' }
	],
	OutPort: [
		{ out_port_box: 1, out_port_type: '213' },
		{ out_port_box: 2, out_port_type: '213' }
	],
	Wire: [
		{ src: 1, tgt: 1, wire_value: '123' },
		{ src: 2, tgt: 2, wire_value: '123' }
	],
	InWire: [],
	OutWire: [],
	OuterOutPort: [],
	OuterInPort: [],
	Box: [
		{ value: 'box1', box_type: '123' },
		{ value: 'box2', box_type: '123' },
		{ value: 'box3', box_type: '123' }
	],
	PassWire: null
};

onMounted(async () => {
	const divElement = (document.querySelector('.simulation-plan') ?? [0]) as HTMLDivElement;
	const renderer = new SimulationPlanRenderer({
		el: divElement,
		useAStarRouting: true,
		runLayout: runDagreLayout
	});

	// Test interaction
	renderer.on(
		'node-click',
		(_eventName: string | symbol, _event, selection: D3SelectionINode<NodeData>) => {
			console.log(selection.datum());
		}
	);

	const g = parseSimulationPlan2IGraph(plan);
	// FIXME: Figure out boundary programmatically
	g.width = 500;
	g.height = 500;

	const graphData = runDagreLayout<NodeData, EdgeData>(g);

	await renderer.setData(graphData);
	await renderer.render();
});
</script>

<template>
	<section>
		<div>Simulation page</div>
		<div>Question template</div>
		<div>Simulation Plan: <button type="button">Run simulation</button></div>
		<div class="simulation-plan"></div>
	</section>
</template>

<style scoped>
.simulation-plan {
	width: 400px;
	height: 400px;
	margin: 5px;
	border: 1px solid #888;
}
</style>
