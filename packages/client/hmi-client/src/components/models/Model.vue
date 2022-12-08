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
import { useRouter } from 'vue-router';
import { RouteName } from '@/router/routes';
import Button from '@/components/Button.vue';

const props = defineProps<{
	modelId: string;
}>();

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

const isDescriptionExpanded = ref(false);

// FIXME: update after Dec 8 demo
const router = useRouter();
const goToSimulationPlanPage = () => {
	router.push({ name: RouteName.SimulationRoute });
};
</script>

<template>
	<section class="model">
		<header>
			<h3>{{ model?.name ?? '' }}</h3>
			<Button action @click="goToSimulationPlanPage">Add to new workflow</Button>
		</header>

		<div class="description" :class="{ 'is-expanded': isDescriptionExpanded }">
			<p>{{ model?.description ?? '' }}</p>
			<div class="less-more-button-container">
				<Button @click="isDescriptionExpanded = !isDescriptionExpanded">
					{{ isDescriptionExpanded ? 'Show less' : 'Show more' }}
				</Button>
			</div>
		</div>
		<div v-if="model !== null" ref="graphElement" class="graph-element"></div>
	</section>
</template>

<style scoped>
.model {
	padding: 10px;
	display: flex;
	flex-direction: column;
}

header {
	display: flex;
	margin-bottom: 10px;
	align-items: flex-start;
}

h3 {
	flex: 1;
	min-width: 0;
}

.description {
	position: relative;
}

.description p {
	max-width: 120ch;
	max-height: 6rem;
	overflow: hidden;
}

.description.is-expanded p {
	max-height: none;
}

.description:not(.is-expanded) .less-more-button-container {
	position: absolute;
	bottom: 0;
	left: 0;
	width: 100%;
	background: linear-gradient(#ffffff00, #ffffff);
	padding-top: 3rem;
}

.graph-element {
	width: 100%;
	flex: 1;
	min-height: 0;
	overflow: hidden;
}

/* Let svg dynamically resize when the sidebar opens/closes or page resizes */
:deep(.graph-element svg) {
	width: 100%;
	height: 100%;
}
</style>
