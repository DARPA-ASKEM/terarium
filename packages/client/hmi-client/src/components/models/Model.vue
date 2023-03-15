<template>
	<section class="asset">
		<header>
			<div class="framework">{{ model?.framework }}</div>
			<div class="header-and-buttons">
				<h4 v-html="title" @click="printModel" />
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
			</AccordionTab>
			<template v-if="!isEditable">
				<AccordionTab header="Parameters">
					<DataTable :value="model?.parameters">
						<Column field="name" header="Name"></Column>
						<Column field="type" header="Type"></Column>
						<Column field="default_value" header="Default"></Column>
					</DataTable>
				</AccordionTab>
				<AccordionTab header="State variables">
					<DataTable :value="model?.content.S">
						<Column field="sname" header="Label"></Column>
						<Column field="mira_ids" header="Name"></Column>
						<Column field="units" header="Units"></Column>
						<Column field="mira_context" header="Concepts"></Column>
						<Column field="definition" header="Definition"></Column>
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
			<template v-if="isEditable">
				<AccordionTab>
					<template #header>
						Parameters<span class="artifact-amount">({{ model?.parameters.length }})</span>
					</template>
					<table>
						<tr class="parameters_header">
							<th>Label</th>
							<th>Name</th>
							<th>Units</th>
							<th>Concept</th>
							<th>Definition</th>
						</tr>
					</table>
					<ul>
						<li v-for="(param_row, index) in model?.parameters" :key="index">
							<section class="parameters_row">
								<DataTable
									v-model:editing-rows="editingRows"
									:value="[param_row]"
									editMode="row"
									@row-edit-save="onRowEditSave"
									tableClass="editable-cells-table"
									:data-key="id"
								>
									<Column field="label">
										<template #editor="{ data, field }">
											<InputText v-model="data[field]" />
										</template>
									</Column>
									<Column field="name">
										<template #editor="{ data, field }">
											<InputText v-model="data[field]" />
										</template>
									</Column>
									<Column field="units">
										<template #editor="{ data, field }">
											<InputText v-model="data[field]" />
										</template>
									</Column>
									<Column field="concept">
										<template #editor="{ data, field }">
											<InputText v-model="data[field]" />
										</template>
									</Column>
									<Column field="definition">
										<template #editor="{ data, field }">
											<InputText v-model="data[field]" />
										</template>
									</Column>
								</DataTable>
							</section>
							<div>stuff</div>
						</li>
						<footer>
							<Button label="Add parameter" icon="pi pi-plus" />
							<Button label="Extract from a dataset" icon="pi pi-file-export" />
						</footer>
					</ul>

					<!-- <Column field="default_value" header="Default"></Column> -->
				</AccordionTab>
			</template>
		</Accordion>

		<!-- <span>Variables</span>
			 <Badge :value="model?.content.S.length" /> 
			<DataTable :value="model?.content.S">
				<Column field="slabel" header="Label"></Column>
				<Column field="sname" header="Name"></Column>
				<Column field="units" header="Units"></Column>
				<Column field="mira_context" header="Concept"></Column>
				<Column field="definition" header="Definition"></Column>
				<Column field="default_value" header="Default"></Column>
			</DataTable> -->

		<!-- <TabView>
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
		</TabView> -->
	</section>
</template>

<script setup lang="ts">
import { IGraph } from '@graph-scaffolder/index';
import { watch, ref, computed, onMounted } from 'vue';
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
// import TabView from 'primevue/tabview';
// import TabPanel from 'primevue/tabpanel';
// import Badge from 'primevue/badge';
import * as textUtil from '@/utils/text';
import { isModel, isDataset, isDocument } from '@/utils/data-util';
import { isEmpty } from 'lodash';
import { ITypedModel, Model } from '@/types/Model';
import { ResultType } from '@/types/common';
import { DocumentType } from '@/types/Document';
import { ProjectAssetTypes } from '@/types/Project';
import { ProvenanceType } from '@/types/Provenance';
import { Dataset } from '@/types/Dataset';

export interface ModelProps {
	assetId: string;
	isEditable: boolean;
	highlight?: string;
}

const props = defineProps<ModelProps>();

const relatedTerariumArtifacts = ref<ResultType[]>([]);
const model = ref<ITypedModel<PetriNet> | null>(null);
const isEditing = ref(false);

// row editing
const editingRows = ref([]);

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

function onRowEditSave() {
	console.log(0);
}

function printModel() {
	console.log(model.value);
}

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
// Render graph whenever a new model is fetched or whenever the HTML element
//	that we render the graph to changes.
watch([model, graphElement], async () => {
	if (model.value === null || graphElement.value === null) return;
	// Convert petri net into a graph
	const g: IGraph<NodeData, EdgeData> = parsePetriNet2IGraph(model.value.content);
	// Create renderer
	const renderer = new PetrinetRenderer({
		el: graphElement.value as HTMLDivElement,
		useAStarRouting: true,
		runLayout: runDagreLayout,
		dragSelector: 'no-drag'
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
});

function toggleEditMode() {
	isEditing.value = !isEditing.value;
}

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

table {
	width: 100%;
}

table th {
	text-align: left;
}

.p-datatable:deep(.p-datatable-thead > tr > th),
.p-datatable:deep(.p-datatable-tbody > tr > td) {
	border: none;
}

.p-datatable.parameters_header:deep(tbody),
.parameters_row .p-datatable:deep(thead) {
	display: none;
}

ul {
	background-color: var(--surface-ground);
	border-radius: 0.75rem;
	padding: 0.75rem;
}

ul .p-button {
	color: var(--primary-color);
	background-color: transparent;
}

ul .p-button:hover,
ul .p-button:focus {
	color: var(--primary-color);
	background-color: var(--surface-hover);
}

li {
	background-color: var(--surface-section);
	border-radius: 0.5rem;
	padding: 0.5rem;
}

footer {
	display: flex;
	gap: 1rem;
	margin-left: 1rem;
}

/* Let svg dynamically resize when the sidebar opens/closes or page resizes */
:deep(.graph-element svg) {
	width: 100%;
	height: 100%;
}
</style>
