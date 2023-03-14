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
				<section class="model_diagram">
					<div class="resizable" :style="{ height: height + 'px' }">
						<div class="content">
							<Splitter class="mb-5 model-panel">
								<SplitterPanel
									class="flex align-items-center justify-content-center"
									:size="80"
									:minSize="50"
								>
									<section class="graph-element">
										<div v-if="model" ref="graphElement" class="graph-element" />
									</section>
								</SplitterPanel>
								<SplitterPanel
									class="flex align-items-center justify-content-center"
									:size="20"
									:minSize="20"
								>
									<section class="math-editor">
										<!-- eventually remove -->
										<section class="dev-options">
											<div style="align-self: center">[Math Renderer]</div>
											<div class="math-options">
												<label>
													<input type="radio" v-model="mathmode" value="mathJAX" />
													MathJAX
												</label>
												<label>
													<input type="radio" v-model="mathmode" value="mathLIVE" />
													MathLIVE
												</label>
											</div>
										</section>
										<math-editor
											:value="equation"
											:mathmode="mathmode"
											@formula-updated="updateFormula"
										></math-editor>
									</section>
								</SplitterPanel>
							</Splitter>
						</div>
						<div class="resizer" @mousedown="startResize"></div>
					</div>
				</section>
			</AccordionTab>
			<template v-if="!isEditable">
				<AccordionTab header="State variables">
					<DataTable
						:value="model?.content.S"
						selectionMode="single"
						@row-select="onRowClick"
						@row-unselect="onRowClick"
					>
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
				<DataTable
					:value="model?.content.S"
					selectionMode="single"
					v-model:selection="selectedRow"
					@row-select="onRowClick"
					@row-unselect="onRowClick"
					:metaKeySelection="false"
				>
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
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import TabView from 'primevue/tabview';
import TabPanel from 'primevue/tabpanel';
import Badge from 'primevue/badge';
import * as textUtil from '@/utils/text';
import { isModel, isDataset, isDocument } from '@/utils/data-util';
import { isEmpty } from 'lodash';
import { ITypedModel, Model } from '@/types/Model';
import { ResultType } from '@/types/common';
import { DocumentType } from '@/types/Document';
import { ProjectAssetTypes } from '@/types/Project';
import { ProvenanceType } from '@/types/Provenance';
import { Dataset } from '@/types/Dataset';
import MathEditor from '@/components/mathml/math-editor.vue';
import Splitter from 'primevue/splitter';
import SplitterPanel from 'primevue/splitterpanel';

export interface ModelProps {
	assetId: string;
	isEditable: boolean;
	highlight?: string;
}

const props = defineProps<ModelProps>();

const relatedTerariumArtifacts = ref<ResultType[]>([]);
const model = ref<ITypedModel<PetriNet> | null>(null);
const isEditing = ref<boolean>(false);

const equation = ref<string>('');
const selectedRow = ref();
const height = ref(400);

let draggableY = 0;

const mathmode = ref('mathJAX');

// Test equation.  Was thinking this would probably eventually live in model.mathLatex or model.mathML?
const modelMath = String.raw`\frac{dS}{dt} = -\beta IS \frac{dI}{dt} = \
 	\beta IS - \gamma I \frac{dR}{dt} = \gamma I`;

// Another experiment using a map to automatically select highlighted version of the latex formula
// this would require the backend service to provide a map of the eq.  Might be a little challenging.
// const equationMap = {
// 	default: String.raw`\frac{dS}{dt} = -\beta IS \frac{dI}{dt} = \
// 	\beta IS - \gamma I \frac{dR}{dt} = \gamma I`,
// 	S: String.raw`\frac{d\colorbox{red}{S}}{dt} = -\beta I{\color{red}{S}} \frac{dI}{dt} = \
// 	\beta I{\color{red}{S}} - \gamma I \frac{dR}{dt} = \gamma I`,
// 	I: String.raw`\frac{dS}{dt} = -\beta {\color{red}I}S \frac{dI}{dt} = \
// 	\beta {\color{red}I}S - \gamma {\color{red}I} \frac{dR}{dt} = \gamma {\color{red}I}`,
// 	R: String.raw`\frac{dS}{dt} = -\beta IS \frac{dI}{dt} = \
// 	\beta IS - \gamma I \frac{d\color{red}{R}}{dt} = \gamma I`
// };

// DataTable click handler for State Variables.  Currently used to do the highlighting.
const onRowClick = () => {
	if (selectedRow.value) {
		equation.value = modelMath.replaceAll(
			selectedRow.value.sname,
			String.raw`{\color{red}${selectedRow.value.sname}}`
		);
	} else {
		equation.value = modelMath;
	}
};

const updateFormula = (formula_string: string) => {
	equation.value = formula_string;
};

const resize = (event: MouseEvent) => {
	height.value += event.clientY - draggableY;
	draggableY = event.clientY;
};

const stopResize = () => {
	document.removeEventListener('mousemove', resize);
	document.removeEventListener('mouseup', stopResize);
};

const startResize = (event: MouseEvent) => {
	draggableY = event.clientY;
	document.addEventListener('mousemove', resize);
	document.addEventListener('mouseup', stopResize);
};

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
			equation.value = modelMath;
		} else {
			equation.value = '';
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
.model-panel {
	height: 100%;
}
.resizable {
	position: relative;
	height: 100%;
	width: 100%;
	min-height: 200px;
	border: 1px solid #ccc;
	overflow: hidden;
}

.resizer {
	width: 100%;
	height: 5px;
	cursor: ns-resize;
	background-color: #1c857764;
	border-radius: 0.25rem;
}

.content {
	height: 99%;
	width: 100%;
}
.graph-element {
	background-color: #f9fafb;
	height: 100%;
	max-height: 100%;
	flex-grow: 1;
	overflow: hidden;
	border-radius: 0.25rem;
}

.slider .graph-element {
	pointer-events: none;
}

.math-editor {
	display: flex;
	max-height: 100%;
	flex-grow: 1;
	flex-direction: column;
}

.model_diagram {
	display: flex;
	height: v-bind('height');
	min-height: v-bind('height');
}

.p-splitter .p-splitter-gutter {
	color: red;
}

.dev-options {
	display: flex;
	flex-direction: column;
	align-self: center;
	width: 100%;
	font-size: 0.75em;
	font-family: monospace;
}

.math-options {
	display: flex;
	flex-direction: row;
	align-self: center;
}

/* Let svg dynamically resize when the sidebar opens/closes or page resizes */
:deep(.graph-element svg) {
	width: 100%;
	height: 100%;
}
</style>
