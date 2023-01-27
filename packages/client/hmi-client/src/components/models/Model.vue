<script setup lang="ts">
import { IGraph } from '@graph-scaffolder/index';
import { watch, ref, computed, onMounted } from 'vue';
import {
	runDagreLayout,
	D3SelectionINode,
	D3SelectionIEdge,
	BaseComputionGraph,
	pathFn
} from '@/services/graph';
import { parsePetriNet2IGraph, NodeData, EdgeData, NodeType, getModel } from '@/services/model';
import { getRelatedArtifacts } from '@/services/provenance';
import { useRouter } from 'vue-router';
import { RouteName } from '@/router/routes';
import Button from 'primevue/button';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import * as textUtil from '@/utils/text';
import { isModel, isDataset, isXDDArticle } from '@/utils/data-util';
import { isEmpty } from 'lodash';
import { Model } from '@/types/Model';
import { ResultType } from '@/types/common';
import { XDDArticle } from '@/types/XDD';
import { ProvenanceType } from '@/types/Provenance';
import { Dataset } from '@/types/Dataset';

export interface ModelProps {
	assetId: string;
	highlight?: string;
}

const props = defineProps<ModelProps>();
const relatedTerariumArtifacts = ref<ResultType[]>([]);
const model = ref<Model | null>(null);

const relatedTerariumModels = computed(
	() => relatedTerariumArtifacts.value.filter((d) => isModel(d)) as Model[]
);
const relatedTerariumDatasets = computed(
	() => relatedTerariumArtifacts.value.filter((d) => isDataset(d)) as Dataset[]
);
const relatedTerariumDocuments = computed(
	() => relatedTerariumArtifacts.value.filter((d) => isXDDArticle(d)) as XDDArticle[]
);

const fetchRelatedTerariumArtifacts = async () => {
	if (model.value) {
		const results = await getRelatedArtifacts(props.assetId, ProvenanceType.ModelRevision);
		relatedTerariumArtifacts.value = results;
	} else {
		relatedTerariumArtifacts.value = [];
	}
};

// Highlight strings based on props.highlight
function highlightSearchTerms(text: string | undefined): string {
	if (!!props.highlight && !!text) {
		return textUtil.highlight(text, props.highlight);
	}
	return text ?? '';
}

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

// Whenever selectedModelId changes, fetch model with that ID
watch(
	() => [props.assetId],
	async () => {
		if (props.assetId !== '') {
			const result = await getModel(props.assetId);
			model.value = result;
			fetchRelatedTerariumArtifacts();
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

onMounted(async () => {
	fetchRelatedTerariumArtifacts();
});
</script>

<template>
	<section class="model">
		<header>
			<h3>{{ model?.name ?? '' }}</h3>
			<Button @click="goToSimulationPlanPage" label="Add to new workflow" />
		</header>

		<Accordion :multiple="true" :active-index="[0, 1, 2, 3]" class="accordion">
			<AccordionTab header="Description">
				<p v-html="highlightSearchTerms(model?.description)" />
			</AccordionTab>
			<AccordionTab header="Structure">
				<div v-if="model !== null" ref="graphElement" class="graph-element"></div>
			</AccordionTab>
			<AccordionTab header="Variables">
				<DataTable :value="model?.content.S">
					<Column field="sname" header="Name"></Column>
					<Column field="mira_ids" header="MIRA IDs"></Column>
					<Column field="mira_context" header="MIRA context"></Column>
				</DataTable>
			</AccordionTab>
			<AccordionTab header="Parameters">
				<DataTable :value="model?.parameters">
					<Column field="name" header="Name"></Column>
					<Column field="type" header="Type"></Column>
					<Column field="default_value" header="Default"></Column>
				</DataTable>
			</AccordionTab>
			<AccordionTab v-if="!isEmpty(relatedTerariumArtifacts)" header="Associated resources">
				<DataTable :value="relatedTerariumModels">
					<Column field="name" header="Models"></Column>
				</DataTable>
				<br />
				<DataTable :value="relatedTerariumDatasets">
					<Column field="name" header="Datasets"></Column>
				</DataTable>
				<br />
				<DataTable :value="relatedTerariumDocuments">
					<Column field="name" header="Papers"></Column>
				</DataTable>
			</AccordionTab>
		</Accordion>
	</section>
</template>

<style scoped>
.model {
	padding: 10px;
	display: flex;
	flex-direction: column;
	overflow-y: scroll;
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
	flex: 1;
	height: 400px;
	width: 400px;
	border: 1px solid var(--surface-border);
	overflow: hidden;
}

.slider .graph-element {
	pointer-events: none;
}

/* Let svg dynamically resize when the sidebar opens/closes or page resizes */
:deep(.graph-element svg) {
	width: 100%;
	height: 100%;
}

.accordion {
	margin-top: 1rem;
	margin-bottom: 1rem;
}
</style>
