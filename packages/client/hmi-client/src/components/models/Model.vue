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
									<section class="mathml">
										<MathMlEditor
											ref="formula"
											:value="equation"
											:highlight-formula="highlightFormula"
											@formula-updated="updateFormula"
											@mathml="mathml"
										></MathMlEditor>
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
						v-model:selected="selectedRow"
						@click="onClick($event)"
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
					v-model:selected="selectedRow"
					@row-click="onClick"
					@mouseenter="onHover"
					@focus="onHover"
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
import DataTable, { DataTableRowEditCancelEvent } from 'primevue/datatable';
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
import MathMlEditor from '@/components/mathml/mathml-editor.vue';
import Splitter from 'primevue/splitter';
import SplitterPanel from 'primevue/splitterpanel';
import { MathfieldElement } from 'mathlive';

export interface ModelProps {
	assetId: string;
	isEditable: boolean;
	highlight?: string;
}

// function highlightVar(inputString: string, variable: string, replacementCharacter: string): string {
// 	// Use a regular expression to replace all occurrences of the input character with the replacement character
// 	const regex = new RegExp(variable, 'g');
// 	const resultString = inputString.replace(regex, replacementCharacter);

// 	return resultString;
// }

// const mathml = `<math xmlns = "http://www.w3.org/1998/Math/MathML">
//    <mrow>
//       <mrow>
//          <msup> <mi>x</mi> <mn>2</mn> </msup> <mo>+</mo>
//          <mrow>
//             <mn>4</mn>
//             <mo>⁢</mo>
//             <mi>x</mi>
//          </mrow>
//          <mo>+</mo>
//          <mn>4</mn>
//       </mrow>

//       <mo>=</mo>
//       <mn>0</mn>
//    </mrow>
// </math>`;

const props = defineProps<ModelProps>();

const relatedTerariumArtifacts = ref<ResultType[]>([]);
const model = ref<ITypedModel<PetriNet> | null>(null);
const isEditing = ref<boolean>(false);

const equation = ref<string>('');
const formula = ref<MathfieldElement | null>(null);
const selectedRow = ref(null);
const highlightFormula = ref<boolean>(true);
const height = ref(400);
let startY = 0;

const latexList = [
	String.raw`\frac{\color{red}{dS}}{dt} = -\beta IS \frac{dI}{dt} = \
	\beta IS - \gamma I \frac{dR}{dt} = \gamma I`
];

// const highlightMap = {
// 	S: String.raw`\frac{\color{red}{dS}}{dt} = {\color{red}{-\beta IS} \frac{dI}{dt} = \
// 	{\color{red}{\beta IS} - \gamma I \frac{dR}{dt} = \gamma I`
// };

const onClick = (event: DataTableRowEditCancelEvent): void => {
	console.log(event);
	console.log(selectedRow.value);
	highlightFormula.value = !highlightFormula.value;
};

const onHover = (event: Event) => {
	console.log(event);
	console.log(selectedRow.value);
};

const mathml = (mathml_string: string) => {
	console.log(mathml_string);
};

const updateFormula = (formula_string: string) => {
	equation.value = formula_string;
};

const resize = (event: MouseEvent) => {
	height.value += event.clientY - startY;
	startY = event.clientY;
};

const stopResize = () => {
	document.removeEventListener('mousemove', resize);
	document.removeEventListener('mouseup', stopResize);
};

const startResize = (event: MouseEvent) => {
	startY = event.clientY;
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
			equation.value = latexList[Math.floor(Math.random() * latexList.length)];
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

.mathml {
	display: flex;
	max-height: 100%;
	flex-grow: 1;
}

.model_diagram {
	display: flex;
	height: v-bind('height');
	min-height: v-bind('height');
}

.p-splitter .p-splitter-gutter {
	color: red;
}

/* Let svg dynamically resize when the sidebar opens/closes or page resizes */
:deep(.graph-element svg) {
	width: 100%;
	height: 100%;
}
</style>
