<script setup lang="ts">
import { onMounted } from 'vue';
import { IGraph } from '@graph-scaffolder/index';
import { PetriNet } from '@/utils/petri-net-validator';
import {
	runDagreLayout,
	D3SelectionINode,
	D3SelectionIEdge,
	BaseComputionGraph,
	pathFn
} from '@/services/graph';
import { parsePetriNet2IGraph, NodeData, EdgeData, NodeType } from '@/services/model';
import * as d3 from 'd3';

// This model can be deleted in future. Just used for init graph
const model: PetriNet = {
	T: [{ tname: 'inf' }, { tname: 'recover' }, { tname: 'death' }],
	S: [{ sname: 'S' }, { sname: 'I' }, { sname: 'R' }, { sname: 'D' }],
	I: [
		{ it: 1, is: 1 },
		{ it: 1, is: 2 },
		{ it: 2, is: 2 },
		{ it: 3, is: 2 }
	],
	O: [
		{ ot: 1, os: 2 },
		{ ot: 1, os: 2 },
		{ ot: 2, os: 3 },
		{ ot: 3, os: 4 }
	]
};
class ModelPlanRenderer extends BaseComputionGraph<NodeData, EdgeData> {
	renderNodes(selection: D3SelectionINode<NodeData>) {
		const state = selection.filter((d) => d.data.type === NodeType.State);
		const transitions = selection.filter((d) => d.data.type === NodeType.Transition);

		transitions
			.append('rect')
			.classed('shape', true)
			.attr('width', (d) => d.width)
			.attr('height', (d) => d.height)
			.style('fill', '#88C')
			.style('stroke', '#888');

		state
			.append('circle')
			.classed('shape', true)
			.attr('cx', (d) => d.width * 0.5)
			.attr('cy', (d) => d.height * 0.5)
			.attr('r', (d) => d.width * 0.5)
			.attr('fill', '#f80');

		selection
			.append('text')
			.attr('y', -5)
			.text((d) => d.label);
	}

	renderEdges(selection: D3SelectionIEdge<EdgeData>) {
		selection
			.append('path')
			.attr('d', (d) => pathFn(d.points))
			.style('fill', 'none')
			.style('stroke', '#000')
			.style('stroke-width', 2)
			.attr('marker-end', `url(#${this.EDGE_ARROW_ID})`);
	}
}

onMounted(async () => {
	let renderer: ModelPlanRenderer | null = null;
	const modelDrawnElement = document.getElementById('model-panel') as HTMLDivElement;
	const g: IGraph<NodeData, EdgeData> = parsePetriNet2IGraph(model); // get graph from petri net representation

	renderer = new ModelPlanRenderer({
		el: modelDrawnElement,
		useAStarRouting: true,
		runLayout: runDagreLayout
	});

	// Test on click
	renderer.on(
		'node-click',
		(_eventName: string | symbol, _event, selection: D3SelectionINode<NodeData>) => {
			console.log(selection.datum());
		}
	);

	// write json to model-json and draw model to model-drawn:
	d3.select('#model-json').text(JSON.stringify(model));
	await renderer?.setData(g);
	await renderer?.render();
});
</script>

<template>
	<section class="model">
		<p>Model Renderer</p>
		<div style="display: flex">
			<div id="model-panel" class="model-panel"></div>
			<div id="model-json" class="model-panel"></div>
		</div>
	</section>
</template>

<style scoped>
.model {
	margin: 10px;
}

.model-panel {
	width: 500px;
	height: 500px;
	border: 1px solid var(--un-color-black-40);
}
</style>
