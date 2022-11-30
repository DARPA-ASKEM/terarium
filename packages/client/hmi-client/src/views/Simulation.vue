<script setup lang="ts">
import { onMounted, watch } from 'vue';
import { useRoute } from 'vue-router';
import {
	runDagreLayout,
	D3SelectionINode,
	D3SelectionIEdge,
	BaseComputionGraph,
	pathFn
} from '@/services/graph';
import { parseSimulationPlan2IGraph } from '@/services/simulation';
import API from '@/api/api';
import { curveBasis } from 'd3';

interface NodeData {
	boxType: string;
	label: string;
}
interface EdgeData {}

class SimulationPlanRenderer extends BaseComputionGraph<NodeData, EdgeData> {
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
		const pFn = pathFn.curve(curveBasis);
		selection
			.append('path')
			.attr('d', (d) => pFn(d.points))
			.style('fill', 'none')
			.style('stroke', '#000')
			.style('stroke-width', 2)
			.attr('marker-end', `url(#${this.EDGE_ARROW_ID})`);
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

const route = useRoute();

onMounted(async () => {
	const divElement = (document.querySelector('.simulation-plan') ?? [0]) as HTMLDivElement;
	const renderer = new SimulationPlanRenderer({
		el: divElement,
		useAStarRouting: true,
		runLayout: runDagreLayout
	});

	// Kick off watcher once the renderer is in place
	watch(
		() => route.params.simulationId,
		async (simulationId) => {
			if (!simulationId) return;

			// FIXME: siwtch to different simulation run result
			console.log('simulation id changed to', simulationId);
			const response = await API.get(`/simulations/plans/${simulationId}`);

			const newPlan = parseSimulationPlan2IGraph(response.data.content);
			newPlan.width = 500;
			newPlan.height = 500;
			const graphData = runDagreLayout<NodeData, EdgeData>(newPlan);
			await renderer.setData(graphData);
			await renderer.render();
		},
		{ immediate: true }
	);

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
