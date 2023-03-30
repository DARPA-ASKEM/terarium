<template>
	<section class="asset">
		<header>
			<div class="framework">{{ model?.framework }}</div>
			<div class="header-and-buttons">
				<h4 v-html="title" />
				<span v-if="isEditable">
					<Button
						v-if="isEditing"
						@click="cancelEdit"
						label="Cancel"
						class="p-button-sm p-button-outlined"
					/>
					<Button
						@click="toggleEditMode"
						:label="isEditing ? 'Save model' : 'Edit model'"
						class="p-button-sm p-button-outlined"
					/>
					<Button
						@click="launchForecast"
						label="Open simulation space"
						:disabled="isEditing"
						class="p-button-sm"
					/>
				</span>
			</div>
			<!--contributor-->
			<!--created on: date-->
		</header>
		<Accordion :multiple="true" :active-index="[0, 1, 2, 3, 4]">
			<AccordionTab header="Description">
				<div class="constrain-width">
					<p v-html="description" />
				</div>
			</AccordionTab>
			<AccordionTab header="Model diagram">
				<section class="model_diagram">
					<TeraResizablePanel>
						<div class="content">
							<Splitter class="mb-5 model-panel" :layout="layout">
								<SplitterPanel class="tera-split-panel" :size="60" :minSize="50">
									<section class="graph-element">
										<div v-if="model" ref="graphElement" class="graph-element" />
										<ContextMenu ref="menu" :model="contextMenuItems" />
									</section>
								</SplitterPanel>
								<SplitterPanel class="tera-split-panel" :size="40" :minSize="30">
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
										<!-- eventually remove -->
										<math-editor
											:value="equation"
											:mathmode="mathmode"
											@formula-updated="updateFormula"
											@mathml="updatePetriFromMathML"
										></math-editor>
									</section>
								</SplitterPanel>
							</Splitter>
						</div>
					</TeraResizablePanel>
				</section>
			</AccordionTab>
			<AccordionTab>
				<template #header>
					State variables<span class="artifact-amount">({{ model?.content?.S.length }})</span>
				</template>
				<template v-if="!isEditable">
					<DataTable
						:value="model?.content?.S"
						selectionMode="single"
						v-model:selection="selectedRow"
						@row-select="onStateVariableClick"
						@row-unselect="onStateVariableClick"
					>
						<Column field="sname" header="Name" />
						<Column header="Type">
							<template #body="slotProps">
								{{ model?.parameters.find((p) => p.name === slotProps.data.sname)?.type }}
							</template>
						</Column>
						<Column header="Default">
							<template #body="slotProps">
								{{ model?.parameters.find((p) => p.name === slotProps.data.sname)?.default_value }}
							</template>
						</Column>
						<Column field="miraIds" header="Concepts">
							<template #body="slotProps">
								<ul>
									<li
										v-for="ontology in [...slotProps.data.miraIds, ...slotProps.data.miraContext]"
										:key="ontology.curie"
									>
										<a :href="ontology.link">{{ ontology.title }}</a
										><br />{{ ontology.description }}
									</li>
								</ul>
							</template>
						</Column>
					</DataTable>
				</template>
				<template v-else>
					<model-parameter-list
						:parameters="betterStates"
						attribute="parameters"
						:selected-variable="selectedVariable"
						@update-parameter-row="updateParamaterRow"
						@parameter-click="onVariableSelected"
					/>
				</template>
			</AccordionTab>
			<AccordionTab>
				<template #header>
					Parameters<span class="artifact-amount">({{ betterParams?.length }})</span>
				</template>
				<template v-if="!isEditable">
					<DataTable :value="betterParams">
						<Column field="name" header="Name" />
						<Column field="type" header="Type" />
						<Column field="default_value" header="Default" />
					</DataTable>
				</template>
				<template v-else>
					<model-parameter-list
						:parameters="betterParams"
						attribute="parameters"
						:selected-variable="selectedVariable"
						@update-parameter-row="updateParamaterRow"
						@parameter-click="onVariableSelected"
					/>
				</template>
			</AccordionTab>
			<AccordionTab :header="`Extractions ${extractions?.length}`">
				<DataTable :value="extractions">
					<Column field="name" header="Name" />
					<Column field="id" header="ID" />
					<Column field="text_annotations" header="Text">
						<template #body="slotProps">
							<ul>
								<li v-for="(text, key) in slotProps.data.text_annotations" :key="key">
									{{ text }}
								</li>
							</ul>
						</template>
					</Column>
					<Column field="dkg_annotations" header="Concepts">
						<template #body="slotProps">
							<ul>
								<li v-for="(text, key) in slotProps.data.dkg_annotations" :key="key">
									<a :href="`http://34.230.33.149:8772/${text[0]}`">{{ text[1] }}</a>
								</li>
							</ul>
						</template>
					</Column>
					<Column field="data_annotations" header="Data">
						<template #body="slotProps">
							<ul>
								<li v-for="(text, key) in slotProps.data.data_annotations" :key="key">
									{{ `${text[0]}: ${text[1]}` }}
								</li>
							</ul>
						</template>
					</Column>
					<Column field="file" header="File" />
					<Column field="doi" header="doi">
						<template #body="slotProps">
							<a :href="slotProps.data.doi">{{ slotProps.data.doi }}</a>
						</template>
					</Column>
					<Column field="equation_annotations" header="Equations">
						<template #body="slotProps">
							<div style="word-wrap: break-word">
								{{ slotProps.data.equation_annotations }}
							</div>
						</template>
					</Column>
				</DataTable>
			</AccordionTab>
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

		<Teleport to="body">
			<ForecastLauncher
				v-if="showForecastLauncher && model"
				:model="model"
				@close="showForecastLauncher = false"
				@launch-forecast="goToSimulationRunPage"
			/>
		</Teleport>
	</section>
</template>

<script setup lang="ts">
import { remove, isEmpty } from 'lodash';
import { IGraph } from '@graph-scaffolder/index';
import { watch, ref, computed, onMounted, onUnmounted } from 'vue';
import { runDagreLayout } from '@/services/graph';
import { PetrinetRenderer } from '@/petrinet/petrinet-renderer';
import {
	parsePetriNet2IGraph,
	PetriNet,
	NodeData,
	EdgeData,
	parseIGraph2PetriNet,
	mathmlToPetri,
	petriToLatex
} from '@/petrinet/petrinet-service';
import { getModel, updateModel } from '@/services/model';
import { getRelatedArtifacts } from '@/services/provenance';
import { useRouter } from 'vue-router';
import { RouteName } from '@/router/routes';
import Button from 'primevue/button';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import ContextMenu from 'primevue/contextmenu';
import * as textUtil from '@/utils/text';
import ModelParameterList from '@/components/models/model-parameter-list.vue';
import ForecastLauncher from '@/components/models/forecast-launcher.vue';
import { isModel, isDataset, isDocument } from '@/utils/data-util';
import { ITypedModel, Model } from '@/types/Model';
import { ResultType } from '@/types/common';
import { DocumentType } from '@/types/Document';
import { ProvenanceType } from '@/types/Types';
import { Dataset } from '@/types/Dataset';
import MathEditor from '@/components/mathml/math-editor.vue';
import Splitter from 'primevue/splitter';
import SplitterPanel from 'primevue/splitterpanel';
import TeraResizablePanel from '../widgets/tera-resizable-panel.vue';
import { example } from './example-model-extraction'; // TODO - to be removed after March demo

export interface ModelProps {
	assetId: string;
	isEditable: boolean;
	highlight?: string;
}

const extractions = ref(Object.values(example));

const props = defineProps<ModelProps>();

const relatedTerariumArtifacts = ref<ResultType[]>([]);
const menu = ref();

const model = ref<ITypedModel<PetriNet> | null>(null);
const isEditing = ref<boolean>(false);
const selectedRow = ref<any>(null);
const selectedVariable = ref('');

const equation = ref<string>('');
const equationOriginal = ref<string>('');
const mathmode = ref('mathLIVE');

const showForecastLauncher = ref(false);

// Test equation.  Was thinking this would probably eventually live in model.mathLatex or model.mathML?
// const modelMath = ref(String.raw`\begin{align}
// \frac{\mathrm{d} S\left( t \right)}{\mathrm{d}t} =&  - inf I\left( t \right) S\left( t \right) \\
// \frac{\mathrm{d} I\left( t \right)}{\mathrm{d}t} =&  - death I\left( t \right) - recover I\left( t \right) + inf I\left( t \right) S\left( t \right) \\
// \frac{\mathrm{d} R\left( t \right)}{\mathrm{d}t} =& recover I\left( t \right) \\
// \frac{\mathrm{d} D\left( t \right)}{\mathrm{d}t} =& death I\left( t \right)
// \end{align}`);

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

const betterStates = computed(() => {
	const statesFromParams = model.value?.parameters.filter((p) => p.state_variable);
	const statesFromContent: any[] = model.value?.content?.S ?? [];

	statesFromContent.forEach((stateFromContent) => {
		statesFromParams.forEach((stateFromParams) => {
			if (stateFromParams.name === stateFromContent.sname) {
				stateFromParams.label = stateFromContent?.sname;
				stateFromParams.concepts = [...stateFromContent.miraIds, ...stateFromContent.miraContext];
			}
		});
	});
	return statesFromParams;
});

const betterParams = computed(() => {
	const params = model.value?.parameters.filter((p) => !p.state_variable);
	const transitions: any[] = model.value?.content?.T ?? [];

	transitions.forEach((transition) => {
		params.forEach((param) => {
			if (param.name === transition.parameter_name) {
				param.label = transition?.tname;
				param.template_type = transition?.template_type;
			}
		});
	});
	return params;
});

const onVariableSelected = (variable: string) => {
	if (variable) {
		if (variable === selectedVariable.value) {
			selectedVariable.value = '';
			equation.value = equationOriginal.value;
		} else {
			selectedVariable.value = variable;
			equation.value = equationOriginal.value.replaceAll(
				selectedVariable.value,
				String.raw`{\color{red}${variable}}`
			);
		}
	} else {
		equation.value = equationOriginal.value;
	}
};

const onStateVariableClick = () => {
	if (selectedRow.value) {
		equation.value = equationOriginal.value.replaceAll(
			selectedRow.value.sname,
			String.raw`{\color{red}${selectedRow.value.sname}}`
		);
	} else {
		equation.value = equationOriginal.value;
	}
};

const updateFormula = (formulaString: string) => {
	equation.value = formulaString;
	equationOriginal.value = formulaString;
};

const layout = computed(() => (!props.isEditable ? 'vertical' : 'horizontal'));

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

function updateParamaterRow(attribute: string, newParameterRow) {
	if (model?.value?.[attribute]) {
		const rowIndex = model.value[attribute].findIndex(({ id }) => id === newParameterRow.id);
		model.value[attribute][rowIndex] = { ...newParameterRow };
	}
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
		updateFormula('');
		if (props.assetId !== '') {
			const result = await getModel(props.assetId);
			model.value = result;
			fetchRelatedTerariumArtifacts();
			if (model.value) {
				const data = await petriToLatex(model.value.content);
				if (data) {
					updateFormula(data);
				}
			}
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

const editorKeyHandler = (event: KeyboardEvent) => {
	// Ignore backspace if the current focus is a text/input box
	if ((event.target as HTMLElement).tagName === 'INPUT') {
		return;
	}

	if (event.key === 'Backspace' && renderer) {
		if (renderer && renderer.nodeSelection) {
			const nodeData = renderer.nodeSelection.datum();
			remove(renderer.graph.edges, (e) => e.source === nodeData.id || e.target === nodeData.id);
			remove(renderer.graph.nodes, (n) => n.id === nodeData.id);
			renderer.render();
		}

		if (renderer && renderer.edgeSelection) {
			const edgeData = renderer.edgeSelection.datum();
			remove(
				renderer.graph.edges,
				(e) => e.source === edgeData.source && e.target === edgeData.target
			);
			renderer.render();
		}
	}
};

// Model editor context menu
const contextMenuItems = ref([
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
watch(
	[model, graphElement],
	async () => {
		if (model.value === null || graphElement.value === null) return;
		// Convert petri net into a graph
		const graphData: IGraph<NodeData, EdgeData> = parsePetriNet2IGraph(model.value.content, {
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

		renderer.on('background-contextmenu', (_evtName, evt, _selection, _renderer, pos: any) => {
			if (!renderer?.editMode) return;
			eventX = pos.x;
			eventY = pos.y;
			menu.value.toggle(evt);
		});

		// Render graph
		await renderer?.setData(graphData);
		await renderer?.render();
		const latexFormula = await petriToLatex(model.value.content);
		if (latexFormula) {
			updateFormula(latexFormula);
		} else {
			updateFormula('');
		}
	},
	{ deep: true }
);

const updatePetriFromMathML = async (mathmlString: string) => {
	// No bueno - doesn't work right now.
	const newPetri = await mathmlToPetri([mathmlString]);
	if (model.value && newPetri) {
		model.value.content = newPetri;
		updateModel(model.value);
	}
};

const launchForecast = () => {
	showForecastLauncher.value = true;
};

const router = useRouter();
const goToSimulationRunPage = () => {
	showForecastLauncher.value = false;
	router.push({
		name: RouteName.ProjectRoute,
		params: {
			assetId: model.value?.id ?? 0 + 1000,
			assetName: highlightSearchTerms(model.value?.name ?? ''),
			assetType: 'simulation_runs'
		}
	});
};

onMounted(async () => {
	fetchRelatedTerariumArtifacts();
	document.addEventListener('keyup', editorKeyHandler);
});

onUnmounted(() => {
	document.removeEventListener('keyup', editorKeyHandler);
});

const toggleEditMode = () => {
	isEditing.value = !isEditing.value;
	renderer?.setEditMode(isEditing.value);
	if (!isEditing.value && model.value && renderer) {
		model.value.content = parseIGraph2PetriNet(renderer.graph);
		updateModel(model.value);
	}
};

// Cancel existing edits, currently this will:
// - Resets changs to the model structure
const cancelEdit = async () => {
	isEditing.value = false;
	if (!model.value) return;

	// Convert petri net into a graph with raw input data
	const graphData: IGraph<NodeData, EdgeData> = parsePetriNet2IGraph(model.value.content, {
		S: { width: 60, height: 60 },
		T: { width: 40, height: 40 }
	});

	if (renderer) {
		renderer.setEditMode(false);
		await renderer.setData(graphData);
		renderer.isGraphDirty = true;
		await renderer.render();
	}
};

const title = computed(() => highlightSearchTerms(model.value?.name ?? ''));
const description = computed(() => highlightSearchTerms(model.value?.description ?? ''));
</script>

<style scoped>
.model-panel {
	height: 100%;
	border-radius: var(--border-radius-big);
	border: solid 1px var(--surface-border);
	overflow: hidden;
}

.content {
	height: 100%;
	width: 100%;
}

.graph-element {
	background-color: var(--surface-secondary);
	height: 100%;
	max-height: 100%;
	flex-grow: 1;
	overflow: hidden;
	border: none;
}

.math-editor {
	display: flex;
	max-height: 100%;
	flex-grow: 1;
	flex-direction: column;
}

.model_diagram {
	display: flex;
	height: 100%;
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

.tera-split-panel {
	display: flex;
	align-items: center;
	justify-content: center;
}

/* Let svg dynamically resize when the sidebar opens/closes or page resizes */
:deep(.graph-element svg) {
	width: 100%;
	height: 100%;
}

.constrain-width {
	max-width: 60rem;
}
</style>
