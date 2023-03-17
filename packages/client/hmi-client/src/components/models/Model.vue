<template>
	<section class="asset">
		<header>
			<div class="framework">{{ model?.framework }}</div>
			<div class="header-and-buttons">
				<h4 v-html="title" />
				<span v-if="isEditable">
					<Button
						@click="toggleEditMode"
						:label="isEditing ? 'Save model' : 'Edit model'"
						class="p-button-sm p-button-outlined"
					/>
					<Button
						@click="goToSimulationPlanPage"
						label="Open parameter space"
						:disabled="isEditing"
						class="p-button-sm"
					/>
				</span>
			</div>
			<!--contributor-->
			<!--created on: date-->
		</header>
		<Accordion :multiple="true" :active-index="[0, 1, 2, 3]">
			<AccordionTab header="Description">
				<p v-html="description" />
			</AccordionTab>
			<AccordionTab header="Model diagram">
				<div v-if="model" ref="graphElement" class="graph-element" />
				<ContextMenu ref="menu" :model="items" @item-click="testItem" />
			</AccordionTab>
			<template v-if="!isEditable">
				<AccordionTab header="State variables">
					<DataTable :value="model?.content.S">
						<Column field="sname" header="Label"></Column>
						<Column field="mira_ids" header="Name"></Column>
						<Column field="units" header="Units"></Column>
						<Column field="mira_context" header="Concepts"></Column>
						<Column field="definition" header="Definition"></Column>
					</DataTable>
				</AccordionTab>
				<AccordionTab header="Parameters">
					<DataTable :value="model?.parameters">
						<Column field="name" header="Name"></Column>
						<Column field="type" header="Type"></Column>
						<Column field="default_value" header="Default"></Column>
					</DataTable>
				</AccordionTab>
			</template>
			<AccordionTab v-if="!isEmpty(relatedTerariumArtifacts)" header="Associated resources">
				<DataTable :value="relatedTerariumModels">
					<Column field="name" header="Models"></Column>
				</DataTable>
				<DataTable :value="relatedTerariumDatasets">
					<Column field="name" header="Datasets"></Column>
				</DataTable>
				<DataTable :value="relatedTerariumDocuments">
					<Column field="name" header="Documents"></Column>
				</DataTable>
			</AccordionTab>
		</Accordion>
		<TabView v-if="isEditable">
			<TabPanel>
				<template #header>
					<span>State variables</span>
					<Badge :value="model?.content.S.length" />
				</template>
				<DataTable :value="model?.content.S">
					<Column field="sname" header="Label"></Column>
					<Column field="mira_ids" header="Name"></Column>
					<Column field="units" header="Units"></Column>
					<Column field="mira_context" header="Concepts"></Column>
					<Column field="definition" header="Definition"></Column>
				</DataTable>
			</TabPanel>
			<TabPanel>
				<template #header>
					<span>Parameters</span>
					<Badge :value="model?.parameters.length" />
				</template>
				<DataTable :value="model?.parameters">
					<Column field="name" header="Name"></Column>
					<Column field="type" header="Type"></Column>
					<Column field="default_value" header="Default"></Column>
				</DataTable>
			</TabPanel>
		</TabView>
	</section>
</template>

<script setup lang="ts">
import _, { isEmpty } from 'lodash';
import { IGraph } from '@graph-scaffolder/index';
import { watch, ref, computed, onMounted, onUnmounted } from 'vue';
import { runDagreLayout } from '@/services/graph';
import { PetrinetRenderer } from '@/petrinet/petrinet-renderer';
import { parsePetriNet2IGraph, PetriNet, NodeData, EdgeData } from '@/petrinet/petrinet-service';
import { getModel } from '@/services/model';
import { getRelatedArtifacts } from '@/services/provenance';
import { useRouter } from 'vue-router';
import { RouteName } from '@/router/routes';
import Button from 'primevue/button';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import TabView from 'primevue/tabview';
import TabPanel from 'primevue/tabpanel';
import Badge from 'primevue/badge';
import ContextMenu from 'primevue/contextmenu';
import * as textUtil from '@/utils/text';
import { isModel, isDataset, isDocument } from '@/utils/data-util';
import { ITypedModel, Model } from '@/types/Model';
import { ResultType } from '@/types/common';
import { DocumentType } from '@/types/Document';
import { ProjectAssetTypes } from '@/types/Project';
import { ProvenanceType } from '@/types/Types';
import { Dataset } from '@/types/Dataset';

export interface ModelProps {
	assetId: string;
	isEditable: boolean;
	highlight?: string;
}

const props = defineProps<ModelProps>();

const relatedTerariumArtifacts = ref<ResultType[]>([]);
const menu = ref();

const model = ref<ITypedModel<PetriNet> | null>(null);
const isEditing = ref(false);

const relatedTerariumModels = computed(
	() => relatedTerariumArtifacts.value.filter((d) => isModel(d)) as Model[]
);
const relatedTerariumDatasets = computed(
	() => relatedTerariumArtifacts.value.filter((d) => isDataset(d)) as Dataset[]
);
const relatedTerariumDocuments = computed(
	() => relatedTerariumArtifacts.value.filter((d) => isDocument(d)) as DocumentType[]
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

let renderer: PetrinetRenderer | null = null;
let eventX = 0;
let eventY = 0;

const keyHandler = (event: KeyboardEvent) => {
	if (event.key === 'Backspace' && renderer) {
		if (renderer && renderer.nodeSelection) {
			const nodeData = renderer.nodeSelection.datum();
			_.remove(renderer.graph.edges, (e) => e.source === nodeData.id || e.target === nodeData.id);
			_.remove(renderer.graph.nodes, (n) => n.id === nodeData.id);
			renderer.render();
		}

		if (renderer && renderer.edgeSelection) {
			const edgeData = renderer.edgeSelection.datum();
			_.remove(
				renderer.graph.edges,
				(e) => e.source === edgeData.source || e.target === edgeData.target
			);
			renderer.render();
		}
	}
};

const items = ref([
	{
		label: 'Add State',
		icon: 'pi pi-fw pi-circle',
		command: () => {
			if (renderer) {
				renderer.addNode('S', 'test', { x: eventX, y: eventY });
			}
		}
	},
	{
		label: 'Add Transition',
		icon: 'pi pi-fw pi-stop',
		command: () => {
			if (renderer) {
				renderer.addNode('T', 'test', { x: eventX, y: eventY });
			}
		}
	}
]);

// Render graph whenever a new model is fetched or whenever the HTML element
//	that we render the graph to changes.
watch([model, graphElement], async () => {
	if (model.value === null || graphElement.value === null) return;
	// Convert petri net into a graph
	const g: IGraph<NodeData, EdgeData> = parsePetriNet2IGraph(model.value.content, {
		S: { width: 60, height: 60 },
		T: { width: 40, height: 40 }
	});

	// Create renderer
	renderer = new PetrinetRenderer({
		el: graphElement.value as HTMLDivElement,
		useAStarRouting: false,
		useStableZoomPan: true,
		runLayout: runDagreLayout,
		dragSelector: 'no-drag'
	});

	renderer.on('add-edge', (_evtName, _evt, _selection, d) => {
		renderer?.addEdge(d.source, d.target);
	});

	renderer.on('background-contextmenu', (evtName, evt, _selection, _renderer, pos: any) => {
		if (renderer?.editMode) return;
		eventX = pos.x;
		eventY = pos.y;
		menu.value.toggle(evt);
	});

	// Render graph
	await renderer?.setData(g);
	await renderer?.render();
});

// FIXME: update after Dec 8 demo
const router = useRouter();
const goToSimulationPlanPage = () => {
	router.push({
		name: RouteName.ProjectRoute,
		params: {
			assetType: ProjectAssetTypes.PLANS
		}
	});
};

onMounted(async () => {
	fetchRelatedTerariumArtifacts();
	document.addEventListener('keyup', keyHandler);
});

onUnmounted(() => {
	document.removeEventListener('keyup', keyHandler);
});

const toggleEditMode = () => {
	isEditing.value = !isEditing.value;
	renderer?.setEditMode(isEditing.value);
};

const title = computed(() => highlightSearchTerms(model.value?.name ?? ''));
const description = computed(() => highlightSearchTerms(model.value?.description ?? ''));
</script>

<style scoped>
.graph-element {
	flex: 1;
	height: 400px;
	width: 100%;
	border: 1px solid var(--surface-border);
	overflow: hidden;
	border-radius: 0.25rem;
}

.slider .graph-element {
	pointer-events: none;
}

/* Let svg dynamically resize when the sidebar opens/closes or page resizes */
:deep(.graph-element svg) {
	width: 100%;
	height: 100%;
}
</style>
