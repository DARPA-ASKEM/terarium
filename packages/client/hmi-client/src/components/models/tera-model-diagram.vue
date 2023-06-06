<template>
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
								:math-mode="MathEditorModes.LIVE"
								@cancel-editing="cancelEditng"
								@equation-updated="setNewLatexFormula"
								@validate-mathml="validateMathML"
								@set-editing="isEditingEQ = true"
							>
							</tera-math-editor>
						</section>
					</SplitterPanel>
				</Splitter>
			</div>
		</TeraResizablePanel>
	</section>
</template>

<script setup lang="ts">
import { remove, isEmpty, pickBy, isArray } from 'lodash';
import { IGraph } from '@graph-scaffolder/index';
import { watch, ref, computed, onMounted, onUnmounted, onUpdated } from 'vue';
import { runDagreLayout } from '@/services/graph';
import { PetrinetRenderer } from '@/petrinet/petrinet-renderer';
import {
	parsePetriNet2IGraph,
	PetriNet,
	NodeData,
	EdgeData,
	mathmlToPetri,
	petriToLatex,
	NodeType,
	parseAMR2IGraph
} from '@/petrinet/petrinet-service';
import { separateEquations, MathEditorModes } from '@/utils/math';
import { updateModel } from '@/services/model';
import { logger } from '@/utils/logger';
import Button from 'primevue/button';
import ContextMenu from 'primevue/contextmenu';
import { ITypedModel } from '@/types/Model';
import TeraMathEditor from '@/components/mathml/tera-math-editor.vue';
import Splitter from 'primevue/splitter';
import SplitterPanel from 'primevue/splitterpanel';
import Toolbar from 'primevue/toolbar';
import { Model } from '@/types/Types';
import TeraResizablePanel from '../widgets/tera-resizable-panel.vue';

// Get rid of these emits
const emit = defineEmits([
	'update-tab-name',
	'close-preview',
	'asset-loaded',
	'close-current-tab',
	'update-model-content'
]);

const props = defineProps<{
	model: ITypedModel<PetriNet> | null;
	isEditable: boolean;
	amr?: Model | null;
}>();

const menu = ref();

const isEditing = ref<boolean>(false);
const isEditingEQ = ref<boolean>(false);

const newModelName = ref('New Model');
const newPetri = ref();

const selectedVariable = ref('');

const equationLatex = ref<string>('');
const equationLatexOriginal = ref<string>('');
const equationLatexNew = ref<string>('');
const isMathMLValid = ref<boolean>(true);

const splitterContainer = ref<HTMLElement | null>(null);
const layout = ref<'horizontal' | 'vertical' | undefined>('horizontal');

const switchWidthPercent = ref<number>(50); // switch model layout when the size of the model window is < 50%

const equationPanelSize = ref<number>(50);
const equationPanelMinSize = ref<number>(0);
const equationPanelMaxSize = ref<number>(100);

const mathPanelSize = ref<number>(50);
const mathPanelMinSize = ref<number>(0);
const mathPanelMaxSize = ref<number>(100);

const graphElement = ref<HTMLDivElement | null>(null);
let renderer: PetrinetRenderer | null = null;
let eventX = 0;
let eventY = 0;

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
		renderer?.toggoleNodeSelectionByLabel(variable);
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

// Whenever selectedModelId changes, fetch model with that ID
watch(
	() => [props.model],
	async () => {
		updateLatexFormula('');
		if (props.model) {
			const data = await petriToLatex(props.model.content);
			if (data) {
				updateLatexFormula(data);
			}
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

onUpdated(() => {
	if (props.model) {
		emit('asset-loaded');
	}
});

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
	[props.model, graphElement],
	async () => {
		if (props.model === null || graphElement.value === null) return;
		let graphData: IGraph<NodeData, EdgeData>;
		if (props.amr) {
			graphData = parseAMR2IGraph(props.amr);
		} else {
			// TODO: Remove this when we only use AMR
			// Convert petri net into a graph
			graphData = parsePetriNet2IGraph(props.model.content, {
				S: { width: 60, height: 60 },
				T: { width: 40, height: 40 }
			});
		}

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

			// de-select node if selection exists
			if (selectedVariable.value) {
				onVariableSelected(selectedVariable.value);
			}
		});

		renderer.on('node-click', async (_evtName, _evt, _e, _renderer, d) => {
			// Note: do not change the renderer's visuals, this is done internally
			onVariableSelected(d.label);
		});

		// Render graph
		await renderer?.setData(graphData);
		await renderer?.render();
		const latexFormula = await petriToLatex(props.model.content);
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

const hasNoEmptyKeys = (obj: Record<string, unknown>): boolean => {
	const nonEmptyKeysObj = pickBy(obj, (value) => !isEmpty(value));
	return Object.keys(nonEmptyKeysObj).length === Object.keys(obj).length;
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

onMounted(async () => {
	// fetchRelatedTerariumArtifacts();
	document.addEventListener('keyup', editorKeyHandler);
});

onUnmounted(() => {
	document.removeEventListener('keyup', editorKeyHandler);
});

const toggleEditMode = () => {
	isEditing.value = !isEditing.value;
	renderer?.setEditMode(isEditing.value);
	if (!isEditing.value && props.model && renderer) {
		emit('update-model-content', renderer.graph);
		updateModel(props.model);
	} else if (isEditing.value && selectedVariable.value) {
		// de-select node if selection exists
		onVariableSelected(selectedVariable.value);
	}
};

// Cancel existing edits, currently this will:
// - Resets changs to the model structure
const cancelEdit = async () => {
	isEditing.value = false;
	if (!props.model) return;

	// Convert petri net into a graph with raw input data
	const graphData: IGraph<NodeData, EdgeData> = parsePetriNet2IGraph(props.model.content, {
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
</style>
