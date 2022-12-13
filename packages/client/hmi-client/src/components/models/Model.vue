<script setup lang="ts">
import { IGraph } from '@graph-scaffolder/index';
import { watch, ref } from 'vue';
import {
	runDagreLayout,
	D3SelectionINode,
	D3SelectionIEdge,
	BaseComputionGraph,
	pathFn
} from '@/services/graph';
import { parsePetriNet2IGraph, NodeData, EdgeData, NodeType, getModel } from '@/services/model';
import { Model } from '@/types/Model';
import Button from '@/components/Button.vue';
import { useRouter } from 'vue-router';
import { RouteName } from '@/router/routes';

export interface ModelProps {
	modelId: string;
}

const props = defineProps<ModelProps>();

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

const model = ref<Model | null>(null);
// Whenever selectedModelId changes, fetch model with that ID
watch(
	() => [props.modelId],
	async () => {
		if (props.modelId !== '') {
			const result = await getModel(props.modelId);
			model.value = result.data as Model;
		} else {
			model.value = null;
		}
	},
	{ immediate: true }
);

const graphElement = ref<HTMLDivElement | null>(null);
// Render graph whenever a new model is fetched or whenever the HTML element
//	that we render the graph to changes.
watch([model, graphElement], async () => {
	if (model.value === null || graphElement.value === null) return;
	// Convert petri net into a graph
	const g: IGraph<NodeData, EdgeData> = parsePetriNet2IGraph(model.value.content);
	// Create renderer
	const renderer = new ModelPlanRenderer({
		el: graphElement.value as HTMLDivElement,
		useAStarRouting: true,
		runLayout: runDagreLayout
	});
	// Render graph
	await renderer?.setData(g);
	await renderer?.render();
});

// FIXME: update after Dec 8 demo
const router = useRouter();
const goToSimulationPlanPage = () => {
	router.push({ name: RouteName.SimulationRoute });
};
</script>

<template>
	<section class="model">
		<div class="graph-column">
			<h3>{{ model?.name ?? '' }}</h3>
			<div v-if="model !== null" ref="graphElement" class="graph-element"></div>
		</div>
		<aside>
			<p class="description">{{ model?.description ?? '' }}</p>
			<h4>Parameters</h4>
			<ul v-if="model !== null">
				<li v-for="parameterName in Object.keys(model.parameters)" :key="parameterName">
					<strong>{{ parameterName }}</strong
					>: {{ model.parameters[parameterName] }}
				</li>
			</ul>
			<h4>Workflows</h4>
			<p>Not included in any workflows.</p>
			<Button action @click="goToSimulationPlanPage">Add to new workflow</Button>
		</aside>
	</section>
</template>

<style scoped>
.model {
	margin: 10px;
	display: flex;
}

.graph-column {
	flex: 1;
	min-width: 0;
}

.graph-element {
	width: 1000px;
	height: 1000px;
	background: var(--un-color-black-5);
}

aside {
	width: 400px;
	margin-left: 10px;
	background: var(--un-color-black-5);
	padding: 10px;
}

h3 {
	margin-bottom: 10px;
}

h4 {
	margin-top: 30px;
	margin-bottom: 10px;
}

.description,
ul {
	max-height: 400px;
	overflow-y: auto;
}

li {
	display: block;
}

strong {
	font-weight: bold;
}
</style>
