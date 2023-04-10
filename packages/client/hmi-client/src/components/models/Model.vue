<template>
	<section class="asset">
		<header>
			<div class="framework">{{ model?.framework }}</div>
			<div class="header-and-buttons">
				<h4 v-html="title" />
				<span v-if="isEditable">
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
				<p v-html="description" class="constrain-width" />
			</AccordionTab>
			<AccordionTab header="Model diagram">
				<section class="model_diagram">
					<TeraResizablePanel>
						<div ref="splitterContainer" class="splitter-container">
							<Splitter :gutterSize="5" :layout="layout">
								<SplitterPanel
									class="tera-split-panel"
									:size="equationPanelSize"
									:minSize="equationPanelMinSize"
									:maxSize="equationPanelMaxSize"
								>
									<section class="graph-element">
										<div v-if="isEditing" class="toolbar">
											<Button
												@click="resetZoom"
												label="Reset Zoom"
												class="p-button-sm p-button-outlined"
											/>
											<Button
												@click="addState"
												label="Add State"
												class="p-button-sm p-button-outlined"
											/>
											<Button
												@click="addTransition"
												label="Add Transition"
												class="p-button-sm p-button-outlined"
											/>
											<Button
												@click="cancelEdit"
												label="Cancel"
												class="p-button-sm p-button-outlined"
											/>
											<Button
												@click="toggleEditMode"
												:label="isEditing ? 'Save model' : 'Edit model'"
												class="p-button-sm p-button-outlined"
											/>
										</div>
										<div v-else>
											<Button
												@click="toggleEditMode"
												:label="isEditing ? 'Save model' : 'Edit model'"
												class="p-button-sm p-button-outlined floating-edit-button"
											/>
										</div>
										<div v-if="model" ref="graphElement" class="graph-element" />
										<ContextMenu ref="menu" :model="contextMenuItems" />
									</section>
								</SplitterPanel>
								<SplitterPanel
									class="tera-split-panel"
									:size="mathPanelSize"
									:minSize="mathPanelMinSize"
									:maxSize="mathPanelMaxSize"
								>
									<section class="math-editor-container" :class="mathEditorSelected">
										<tera-math-editor
											:is-editable="isEditable"
											:latex-equation="equationLatex"
											:is-editing-eq="isEditingEQ"
											:is-math-ml-valid="isMathMLValid"
											:math-mode="MathEditorModes.LIVE"
											@cancel-editing="cancelEditng"
											@equation-updated="setNewLatexFormula"
											@validate-mathml="validateMathML"
										></tera-math-editor>
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
								<vue-mathjax
									:formula="mathJaxEq(slotProps.data.equation_annotations)"
								></vue-mathjax>
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
import { remove, isEmpty, pickBy, isArray } from 'lodash';
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
	petriToLatex,
	NodeType
} from '@/petrinet/petrinet-service';
import { separateEquations, MathEditorModes } from '@/utils/math';
import { getModel, updateModel } from '@/services/model';
import { getRelatedArtifacts } from '@/services/provenance';
import { useRouter } from 'vue-router';
import { RouteName } from '@/router/routes';
import { logger } from '@/utils/logger';
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
import TeraMathEditor from '@/components/mathml/tera-math-editor.vue';
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
const isEditingEQ = ref<boolean>(false);
const selectedRow = ref<any>(null);
const selectedVariable = ref('');

const equationLatex = ref<string>('');
const equationLatexOriginal = ref<string>('');
const equationLatexNew = ref<string>('');
const isMathMLValid = ref<boolean>(true);

const splitterContainer = ref<HTMLElement | null>(null);
const layout = ref<'horizontal' | 'vertical' | undefined>('horizontal');
const showForecastLauncher = ref(false);

const switchWidthPercent = ref<number>(50); // switch model layout when the size of the model window is < 50%

const equationPanelSize = ref<number>(50);
const equationPanelMinSize = ref<number>(0);
const equationPanelMaxSize = ref<number>(100);

const mathPanelSize = ref<number>(50);
const mathPanelMinSize = ref<number>(0);
const mathPanelMaxSize = ref<number>(100);

const updateLayout = () => {
	if (splitterContainer.value) {
		layout.value =
			(splitterContainer.value.offsetWidth / window.innerWidth) * 100 < switchWidthPercent.value ||
			window.innerWidth < 800
				? 'vertical'
				: 'horizontal';
	}
};

const handleResize = () => {
	updateLayout();
};

onMounted(() => {
	window.addEventListener('resize', handleResize);
	handleResize();
});

onUnmounted(() => {
	window.removeEventListener('resize', handleResize);
});

const mathEditorSelected = computed(() => {
	if (!isMathMLValid.value) {
		return 'math-editor-error';
	}
	if (isEditingEQ.value) {
		return 'math-editor-selected';
	}
	return '';
});

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
			equationLatex.value = equationLatexOriginal.value;
		} else {
			selectedVariable.value = variable;
			equationLatex.value = equationLatexOriginal.value.replaceAll(
				selectedVariable.value,
				String.raw`{\color{red}${variable}}`
			);
		}
	} else {
		equationLatex.value = equationLatexOriginal.value;
	}
};

const onStateVariableClick = () => {
	if (selectedRow.value) {
		equationLatex.value = equationLatexOriginal.value.replaceAll(
			selectedRow.value.sname,
			String.raw`{\color{red}${selectedRow.value.sname}}`
		);
	} else {
		equationLatex.value = equationLatexOriginal.value;
	}
};

const setNewLatexFormula = (formulaString: string) => {
	equationLatexNew.value = formulaString;
};

const updateLatexFormula = (formulaString: string) => {
	equationLatex.value = formulaString;
	equationLatexOriginal.value = formulaString;
};

const cancelEditng = () => {
	isEditingEQ.value = false;
	isMathMLValid.value = true;
	// updateLatexFormula(equationLatexOriginal.value);
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
		updateLatexFormula('');
		if (props.assetId !== '') {
			const result = await getModel(props.assetId);
			model.value = result;
			fetchRelatedTerariumArtifacts();
			if (model.value) {
				const data = await petriToLatex(model.value.content);
				if (data) {
					updateLatexFormula(data);
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
	if (event.key === 'Enter' && renderer) {
		if (renderer.nodeSelection) {
			renderer.deselectNode(renderer.nodeSelection);
			renderer.nodeSelection
				.selectAll('.no-drag')
				.style('opacity', 0)
				.style('visibility', 'hidden');
			renderer.nodeSelection = null;
		}
		if (renderer.edgeSelection) {
			renderer.deselectEdge(renderer.edgeSelection);
			renderer.edgeSelection = null;
		}
	}
};

// Model editor context menu
const contextMenuItems = ref([
	{
		label: 'Add state',
		icon: 'pi pi-fw pi-circle',
		command: () => {
			if (renderer) {
				renderer.addNode('S', '?', { x: eventX, y: eventY });
			}
		}
	},
	{
		label: 'Add transition',
		icon: 'pi pi-fw pi-stop',
		command: () => {
			if (renderer) {
				renderer.addNode('T', '?', { x: eventX, y: eventY });
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
			menu.value.show(evt);
		});

		renderer.on('background-click', () => {
			if (menu.value) menu.value.hide();
		});

		// Render graph
		await renderer?.setData(graphData);
		await renderer?.render();
		const latexFormula = await petriToLatex(model.value.content);
		if (latexFormula) {
			updateLatexFormula(latexFormula);
		} else {
			updateLatexFormula('');
		}
	},
	{ deep: true }
);

const updatePetri = async (m: PetriNet) => {
	// equationML.value = mathmlString;
	// Convert petri net into a graph
	const graphData: IGraph<NodeData, EdgeData> = parsePetriNet2IGraph(m, {
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
	updateLatexFormula(equationLatexNew.value);
};

const launchForecast = () => {
	showForecastLauncher.value = true;
};

const hasNoEmptyKeys = (obj: Record<string, unknown>): boolean => {
	const nonEmptyKeysObj = pickBy(obj, (value) => !isEmpty(value));
	return Object.keys(nonEmptyKeysObj).length === Object.keys(obj).length;
};

const validateMathML = async (mathMlString: string, editMode: boolean) => {
	isEditingEQ.value = true;
	isMathMLValid.value = false;
	const cleanedMathML = separateEquations(mathMlString);
	if (mathMlString === '') {
		logger.error(
			'Empty MathML cannot be converted to a Petrinet.  Please try again or click cancel.'
		);
	} else if (!editMode) {
		try {
			const newPetri = await mathmlToPetri(cleanedMathML);
			if (
				(isArray(newPetri) && newPetri.length > 0) ||
				(!isArray(newPetri) && Object.keys(newPetri).length > 0 && hasNoEmptyKeys(newPetri))
			) {
				isMathMLValid.value = true;
				isEditingEQ.value = false;
				updatePetri(newPetri);
			} else {
				logger.error(
					'MathML cannot be converted to a Petrinet.  Please try again or click cancel.'
				);
			}
		} catch (e) {
			isMathMLValid.value = false;
		}
	} else if (editMode) {
		isMathMLValid.value = true;
	}
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

const resetZoom = async () => {
	renderer?.setToDefaultZoom();
};

const addState = async () => {
	renderer?.addNodeCenter(NodeType.State, '?');
};

const addTransition = async () => {
	renderer?.addNodeCenter(NodeType.Transition, '?');
};

const title = computed(() => highlightSearchTerms(model.value?.name ?? ''));
const description = computed(() => highlightSearchTerms(model.value?.description ?? ''));

const mathJaxEq = (eq) => {
	if (eq) {
		return String.raw`$$${Object.keys(eq)[0]}$$`;
	}
	return '';
};
</script>

<style scoped>
.toolbar {
	right: 0;
	display: flex;
	justify-content: space-between;
	position: absolute;
	z-index: 1;
	isolation: isolate;
}

section math-editor {
	justify-content: center;
}

.floating-edit-button {
	background-color: var(--surface-0);
	margin-top: 10px;
	position: absolute;
	right: 10px;
	z-index: 10;
}

.splitter-container {
	height: 100%;
}

.graph-element {
	background-color: var(--surface-secondary);
	height: 100%;
	max-height: 100%;
	flex-grow: 1;
	overflow: hidden;
	border: none;
	position: relative;
}

.math-editor-container {
	display: flex;
	position: absolute;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	flex-direction: column;
	border-width: 2px;
	overflow: auto;
}

.math-editor-selected {
	outline: 2px solid var(--primary-color);
}

.math-editor-error {
	outline: 2px solid red;
	transition: outline 0.3s ease-in-out, color 0.3s ease-in-out, opacity 0.3s ease-in-out;
}

.model_diagram {
	display: flex;
	height: 100%;
}

.p-splitter {
	height: 100%;
}

.tera-split-panel {
	position: relative;
	height: 100%;
	display: flex;
	align-items: center;
	width: 100%;
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
