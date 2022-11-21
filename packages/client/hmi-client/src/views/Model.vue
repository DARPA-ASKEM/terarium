<script setup lang="ts">
import { onMounted, ref, watchEffect } from 'vue';
import graphScaffolder, { IGraph } from '@graph-scaffolder/index';
import { PetriNet } from '@/utils/petri-net-validator';
import { runDagreLayout, D3SelectionINode, D3SelectionIEdge } from '@/services/graph';
import { parsePetriNet2IGraph, NodeData, EdgeData, NodeType } from '@/services/model';
import { getModel } from '@/services/data';
import * as d3 from 'd3';
import { Model } from '@/types/Model';

const MARKER_VIEWBOX = '-5 -5 10 10';
const ARROW = 'M 0,-3.25 L 5 ,0 L 0,3.25';
const pathFn = d3
	.line<{ x: number; y: number }>()
	.x((d) => d.x)
	.y((d) => d.y);

// TODO: replace this mock petri net with the model fetched from the backend
const modelPetriNet: PetriNet = {
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
class ModelPlanRenderer extends graphScaffolder.BasicRenderer<NodeData, EdgeData> {
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
			.attr('marker-end', 'url(#arrowhead)');
	}
}

onMounted(async () => {
	let renderer: ModelPlanRenderer | null = null;
	const modelDrawnElement = document.getElementById('model-panel') as HTMLDivElement;
	const g: IGraph<NodeData, EdgeData> = parsePetriNet2IGraph(modelPetriNet); // get graph from petri net representation

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
	await renderer?.setData(g);
	await renderer?.render();
});

// TODO: let the user choose the model to display
const selectedModelId = ref('1');

const model = ref<Model | null>(null);
watchEffect(async () => {
	// Fetch model with ID selectedModelId
	const result = await getModel(selectedModelId.value);
	model.value = result as Model;
});
</script>

<template>
	<section class="model">
		<div>
			<h3>{{ model?.name ?? '' }}</h3>
			<div class="model-panels">
				<div id="model-panel" class="model-panel"></div>
				<div class="model-panel">{{ JSON.stringify(modelPetriNet) }}</div>
			</div>
		</div>
		<aside>
			<p>{{ model?.description ?? '' }}</p>
			<h4>Parameters</h4>
			<ul v-if="model !== null">
				<li v-for="parameterName in Object.keys(model.parameters)" :key="parameterName">
					<strong>{{ parameterName }}</strong
					>: {{ model.parameters[parameterName] }}
				</li>
			</ul>
		</aside>
	</section>
</template>

<style scoped>
.model {
	margin: 10px;
	display: flex;
}

.model-panels {
	display: flex;
	flex: 1;
	min-width: 0;
}

.model-panel {
	width: 500px;
	height: 500px;
	border: 1px solid var(--un-color-black-40);
}

aside {
	width: 400px;
	margin-left: 10px;
	background: var(--un-color-black-5);
	padding: 10px;
}

h3 {
	font: var(--un-font-h3);
	margin-bottom: 10px;
}

h4 {
	font: var(--un-font-h4);
	margin-top: 30px;
	margin-bottom: 10px;
}

li {
	display: block;
}

strong {
	font-weight: bold;
}
</style>
