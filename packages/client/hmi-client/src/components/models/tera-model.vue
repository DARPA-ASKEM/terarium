<template>
	<section class="asset" style="padding-top: 0">
		<header class="fixed-header">
			<div class="framework">{{ model?.framework }}</div>
			<div class="header-and-buttons">
				<!-- search bar -->
				<div class="flex justify-content-end">
					<span class="p-input-icon-left">
						<i class="pi pi-search" />
						<InputText v-model="globalFilter['global'].value" placeholder="Keyword Search" />
					</span>
				</div>
				<InputText
					v-if="assetId === ''"
					v-model="newModelName"
					class="model-title-text-area"
					placeholder="Title of new model"
				/>
				<h4 v-else v-html="title" />
				<span v-if="assetId === ''">
					<Button @click="createNewModel" label="Create New Model" class="p-button-sm" />
				</span>
				<span v-else-if="isEditable">
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
				<p v-if="assetId !== ''" v-html="description" class="constrain-width" />
				<section v-else>
					<label for="placeholder"></label
					><Textarea
						v-model="newDescription"
						class="model-description-text-area"
						rows="5"
						placeholder="Description of new model"
					/>
				</section>
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
										<Toolbar>
											<template #start>
												<Button
													@click="resetZoom"
													label="Reset zoom"
													class="p-button-sm p-button-outlined toolbar-button"
												/>
											</template>
											<template #center>
												<span class="toolbar-subgroup">
													<Button
														v-if="isEditing"
														@click="addState"
														label="Add state"
														class="p-button-sm p-button-outlined toolbar-button"
													/>
													<Button
														v-if="isEditing"
														@click="addTransition"
														label="Add transition"
														class="p-button-sm p-button-outlined toolbar-button"
													/>
												</span>
											</template>
											<template #end>
												<span class="toolbar-subgroup">
													<Button
														v-if="isEditing"
														@click="cancelEdit"
														label="Cancel"
														class="p-button-sm p-button-outlined toolbar-button"
													/>
													<Button
														@click="toggleEditMode"
														:label="isEditing ? 'Save model' : 'Edit model'"
														:class="
															isEditing
																? 'p-button-sm toolbar-button-saveModel'
																: 'p-button-sm p-button-outlined toolbar-button'
														"
													/>
												</span>
											</template>
										</Toolbar>
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
											:math-mode="MathEditorModes.KATEX"
											@cancel-editing="cancelEditng"
											@equation-updated="setNewLatexFormula"
											@validate-mathml="validateMathML"
											@set-editing="isEditingEQ = true"
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
						v-model:filters="globalFilter"
						filterDisplay="row"
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
						:parameters="filteredStates"
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
					<DataTable :value="betterParams" v-model:filters="globalFilter" filterDisplay="row">
						<Column field="name" header="Name" />
						<Column field="type" header="Type" />
						<Column field="default_value" header="Default" />
					</DataTable>
				</template>
				<template v-else>
					<model-parameter-list
						:parameters="filteredParams"
						attribute="parameters"
						:selected-variable="selectedVariable"
						@update-parameter-row="updateParamaterRow"
						@parameter-click="onVariableSelected"
					/>
				</template>
			</AccordionTab>
			<AccordionTab
				:header="`Extractions ${
					extractions?.length ? extractions?.length : ': No Extractions Found'
				}`"
			>
				<DataTable :value="extractions" v-model:filters="globalFilter" filterDisplay="row">
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
								<katex-element :expression="Object.keys(slotProps.data.equation_annotations)[0]" />
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
import Textarea from 'primevue/textarea';
import InputText from 'primevue/inputtext';
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
import ForecastLauncher from '@/components/models/tera-forecast-launcher.vue';
import { isModel, isDataset, isDocument } from '@/utils/data-util';
import { ITypedModel, Model } from '@/types/Model';
import { ResultType } from '@/types/common';
import { DocumentType } from '@/types/Document';
import { ProvenanceType } from '@/types/Types';
import { Dataset } from '@/types/Dataset';
import TeraMathEditor from '@/components/mathml/tera-math-editor.vue';
import Splitter from 'primevue/splitter';
import SplitterPanel from 'primevue/splitterpanel';
import Toolbar from 'primevue/toolbar';
import { FilterMatchMode } from 'primevue/api';
import ModelParameterList from '@/components/models/model-parameter-list.vue';
import TeraResizablePanel from '../widgets/tera-resizable-panel.vue';

const emit = defineEmits(['create-new-model', 'update-tab-name']);

const extractions = ref([]);

const props = defineProps({
	assetId: {
		type: String,
		required: true
	},
	isEditable: {
		type: Boolean,
		required: true
	},
	highlight: {
		type: String,
		default: '',
		required: false
	}
});

const relatedTerariumArtifacts = ref<ResultType[]>([]);
const menu = ref();

const model = ref<ITypedModel<PetriNet> | null>(null);

const isEditing = ref<boolean>(false);
const isEditingEQ = ref<boolean>(false);

const newModelName = ref('New Model');
const newDescription = ref<string | undefined>('');
const newPetri = ref();

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
	// new model
	if (props.assetId === '') {
		isEditingEQ.value = true;
		isMathMLValid.value = false;
	}
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

const globalFilter = ref({
	// @ts-ignore
	// eslint-disable-line
	global: { value: '', matchMode: FilterMatchMode.CONTAINS }
});

const filteredStates = computed(() =>
	betterStates.value?.filter(
		(p) =>
			p.name.toLowerCase().includes(globalFilter.value.global.value.toLowerCase()) ||
			p.label.toLowerCase().includes(globalFilter.value.global.value.toLowerCase())
	)
);

const filteredParams = computed(() =>
	betterParams.value?.filter(
		(p) =>
			p.name.toLowerCase().includes(globalFilter.value.global.value.toLowerCase()) ||
			p.label.toLowerCase().includes(globalFilter.value.global.value.toLowerCase())
	)
);

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
	updateLatexFormula(equationLatexOriginal.value);
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

watch(
	() => newModelName.value,
	(newValue, oldValue) => {
		if (newValue !== oldValue) {
			emit('update-tab-name', newValue);
		}
	}
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
			renderer.nodeSelection = null;
			renderer.render();
		}

		if (renderer && renderer.edgeSelection) {
			const edgeData = renderer.edgeSelection.datum();
			remove(
				renderer.graph.edges,
				(e) => e.source === edgeData.source && e.target === edgeData.target
			);
			renderer.edgeSelection = null;
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

const createNewModel = async () => {
	const newModel = {
		name: newModelName.value,
		framework: 'Petri Net',
		description: newDescription.value,
		content: JSON.stringify(newPetri.value ?? { S: [], T: [], I: [], O: [] })
	};
	emit('create-new-model', newModel);
	isEditingEQ.value = false;
	isMathMLValid.value = true;
};

const validateMathML = async (mathMlString: string, editMode: boolean) => {
	isEditingEQ.value = true;
	isMathMLValid.value = false;
	const cleanedMathML = separateEquations(mathMlString);
	if (mathMlString === '') {
		isMathMLValid.value = true;
		isEditingEQ.value = false;
	} else if (!editMode) {
		try {
			newPetri.value = await mathmlToPetri(cleanedMathML);
			if (
				(isArray(newPetri.value) && newPetri.value.length > 0) ||
				(!isArray(newPetri.value) &&
					Object.keys(newPetri.value).length > 0 &&
					hasNoEmptyKeys(newPetri.value))
			) {
				isMathMLValid.value = true;
				isEditingEQ.value = false;
				updatePetri(newPetri.value);
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
</script>

<style scoped>
.p-toolbar {
	position: absolute;
	width: 100%;
	z-index: 1;
	isolation: isolate;
	background: transparent;
	padding: 0.5rem;
}

.p-button.p-component.p-button-sm.p-button-outlined.toolbar-button {
	background-color: var(--surface-0);
	margin: 0.25rem;
}

.toolbar-button-saveModel {
	margin: 0.25rem;
}

.toolbar-subgroup {
	display: flex;
}

.fixed-header {
	position: sticky;
	top: -1px;
	z-index: 1;
	background-color: white;
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
	border: 4px solid transparent;
	border-radius: 0px var(--border-radius) var(--border-radius) 0px;
	overflow: auto;
}

.math-editor-selected {
	border: 4px solid var(--primary-color);
}

.math-editor-error {
	border: 4px solid var(--surface-border-warning);
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

.model-description-text-area {
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
	padding: 5px;
	resize: none;
	overflow-y: hidden;
	width: 100%;
}

.model-title-text-area {
	border: 1px solid var(--surface-border-light);
	border-radius: 4px;
	padding: 5px;
	resize: none;
	overflow-y: hidden;
	width: 100%;
	font-size: var(--font-body-medium);
}
</style>
