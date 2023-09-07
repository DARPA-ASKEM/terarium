<template>
	<main>
		<Accordion v-if="!nodePreview" multiple :activeIndex="[0, 1, 2]">
			<!-- Model diagram -->
			<AccordionTab header="Model diagram">
				<TeraResizablePanel class="diagram-container">
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
								<span v-if="isEditing" class="toolbar-subgroup">
									<Button
										@click="prepareStateEdit()"
										label="Add state"
										class="p-button-sm p-button-outlined toolbar-button"
									/>
									<Button
										@click="prepareTransitionEdit()"
										label="Add transition"
										class="p-button-sm p-button-outlined toolbar-button"
									/>
								</span>
							</template>
							<template #end>
								<span v-if="isEditable" class="toolbar-subgroup">
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
								<Button
									v-if="model && getStratificationType(model) && !isEditing"
									@click="toggleCollapsedView"
									:label="isCollapsed ? 'Show expanded view' : 'Show collapsed view'"
									class="p-button-sm p-button-outlined toolbar-button"
								/>
							</template>
						</Toolbar>
						<tera-model-type-legend v-if="model" :model="model" />
						<div v-if="model" class="graph-container">
							<div ref="graphElement" class="graph-element" />
							<div class="legend">
								<div class="legend-item" v-for="(label, index) in graphLegendLabels" :key="index">
									<div
										class="legend-circle"
										:style="`background: ${graphLegendColors[index]}`"
									></div>
									{{ label }}
								</div>
							</div>
						</div>
						<ContextMenu ref="menu" :model="contextMenuItems" />
					</section>
				</TeraResizablePanel>
			</AccordionTab>
			<!-- Model equations -->
			<AccordionTab header="Model equations">
				<section :class="isEditingEQ ? `diagram-container-editing` : `diagram-container`">
					<section v-if="props.isEditable && props.isEquationsEditable" class="controls">
						<Button
							v-if="isEditingEQ"
							@click="cancelEditEquations"
							label="Cancel"
							class="p-button-sm p-button-outlined edit-button"
							style="background-color: white"
						/>
						<Button
							v-if="isEditingEQ"
							@click="onClickUpdateModel"
							label="Update model"
							class="p-button-sm"
						/>
						<Button
							v-else
							@click="isEditingEQ = true"
							label="Edit equation"
							class="p-button-sm p-button-outlined edit-button"
						/>
					</section>
					<section class="math-editor-container" :class="mathEditorSelected">
						<tera-math-editor
							v-for="(eq, index) in latexEquationList"
							:key="index"
							:index="index"
							:is-editable="isEditable"
							:latex-equation="eq"
							:is-editing-eq="isEditingEQ"
							@equation-updated="setNewEquation"
							@delete="deleteEquation"
							ref="equationsRef"
						/>
						<Button
							v-if="isEditingEQ"
							class="p-button-sm add-equation-button"
							icon="pi pi-plus"
							label="Add equation"
							@click="latexEquationList.push('')"
							text
						/>
					</section>
				</section>
			</AccordionTab>
			<!-- Model observables -->
			<AccordionTab header="Model observables">
				<section
					:class="isEditingObservables ? `diagram-container-editing` : `diagram-container`"
					:start-height="300"
				>
					<section v-if="isEditable" class="controls">
						<Button
							v-if="isEditingObservables"
							@click="cancelEditObservables"
							label="Cancel"
							class="p-button-sm p-button-outlined edit-button"
						/>
						<Button
							@click="updateObservables"
							:label="isEditingObservables ? 'Update observable' : 'Edit observables'"
							:disabled="disableSaveObservable"
							:class="
								isEditingObservables ? 'p-button-sm' : 'p-button-sm p-button-outlined edit-button'
							"
						/>
					</section>
					<section class="observable-editor-container">
						<tera-math-editor
							v-for="(ob, index) in observervablesList"
							:key="index"
							:index="index"
							:is-editable="isEditable"
							:latex-equation="ob.expression || ''"
							:id="ob.id"
							:name="ob.name"
							:is-editing-eq="isEditingObservables"
							:show-metadata="true"
							@equation-updated="setNewObservables"
							@delete="deleteObservable"
							ref="observablesRefs"
						/>
						<Button
							v-if="observablesList.length === 0 || isEditingObservables"
							class="p-button-sm add-equation-button"
							icon="pi pi-plus"
							label="Add observable"
							@click="addObservable"
							text
						/>
					</section>
				</section>
			</AccordionTab>
		</Accordion>
		<div v-else-if="model" ref="graphElement" class="graph-element preview" />
	</main>

	<Teleport to="body">
		<tera-modal
			class="edit-modal"
			v-if="openEditNode === true"
			@modal-mask-clicked="openEditNode = false"
			@modal-enter-press="addNode"
		>
			<template #header>
				<h4>Add/Edit {{ editNodeObj.nodeType }}</h4>
			</template>
			<div class="modal-input-container">
				<span class="modal-input-label">ID: </span>
				<InputText class="modal-input" v-model="editNodeObj.id" placeholder="Id" />
			</div>
			<div class="modal-input-container">
				<span class="modal-input-label">Name: </span>
				<InputText class="modal-input" v-model="editNodeObj.name" placeholder="Name" />
			</div>
			<template #math-editor>
				<div class="modal-input-container">
					<span class="modal-input-label">Transition Expression: </span>
					<tera-math-editor
						:keep-open="true"
						:is-editing-eq="true"
						:latex-equation="editNodeObj.expression"
						@equation-updated="updateRateEquation"
						ref="editNodeMathEditor"
					>
					</tera-math-editor>
				</div>
			</template>
			<template #footer>
				<Button label="Submit" :disabled="editNodeObj.id === ''" @click="addNode()" />
				<Button label="Cancel" class="p-button-secondary" @click="openEditNode = false" />
			</template>
		</tera-modal>
	</Teleport>
</template>

<script setup lang="ts">
import { IGraph } from '@graph-scaffolder/index';
import { watch, ref, computed, onMounted, onUnmounted, onUpdated } from 'vue';
import {
	PetrinetRenderer,
	NodeData,
	EdgeData,
	NodeType
} from '@/model-representation/petrinet/petrinet-renderer';
import { NestedPetrinetRenderer } from '@/model-representation/petrinet/nested-petrinet-renderer';

import { petriToLatex } from '@/petrinet/petrinet-service';
import {
	getStratificationType,
	convertAMRToACSet,
	updateExistingModelContent
} from '@/model-representation/petrinet/petrinet-service';
import { latexToAMR } from '@/services/knowledge';
import { updateModel } from '@/services/model';
import Button from 'primevue/button';
import ContextMenu from 'primevue/contextmenu';
import TeraMathEditor from '@/components/mathml/tera-math-editor.vue';
import { cleanLatexEquations, extractVariablesFromMathML, EquationSide } from '@/utils/math';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Toolbar from 'primevue/toolbar';
import { Model, Observable } from '@/types/Types';
import TeraModal from '@/components/widgets/tera-modal.vue';
import InputText from 'primevue/inputtext';
import { getGraphData, getPetrinetRenderer } from '@/model-representation/petrinet/petri-util';
import TeraResizablePanel from '../widgets/tera-resizable-panel.vue';
import TeraModelTypeLegend from './tera-model-type-legend.vue';

// Get rid of these emits
const emit = defineEmits([
	'update-tab-name',
	'asset-loaded',
	'update-model-content',
	'update-model-observables'
]);

const props = defineProps<{
	model: Model | null;
	isEditable: boolean;
	isEquationsEditable?: boolean;
	nodePreview?: boolean;
}>();

const menu = ref();

const isEditing = ref<boolean>(false);
const isEditingEQ = ref<boolean>(false);
const isEditingObservables = ref<boolean>(false);

// Model Equations
const equationsRef = ref<any[]>([]);
const latexEquationList = ref<string[]>([]);
const latexEquationsOriginalList = ref<string[]>([]);

// Observable Equations
const observablesRefs = ref<any[]>([]);
const observervablesList = ref<Observable[]>([]);

const mathContainerStyle = computed(() => (props.isEquationsEditable ? '-1rem' : '0rem'));

// For model editing
interface AddStateObj {
	id: string;
	name: string;
	nodeType: string;
	expression: string;
	expression_mathml?: string;
}

const openEditNode = ref<boolean>(false);
const editNodeMathEditor = ref<typeof TeraMathEditor | null>(null);
const editNodeObj = ref<AddStateObj>({
	id: '',
	name: '',
	nodeType: '',
	expression: '',
	expression_mathml: ''
});
let previousId: any = null;

const addObservable = () => {
	isEditingObservables.value = true;
	const obs: Observable = {
		id: '',
		name: '',
		states: [],
		expression: '',
		expression_mathml: ''
	};
	observervablesList.value.push(obs);
};

const splitterContainer = ref<HTMLElement | null>(null);
const layout = ref<'horizontal' | 'vertical' | undefined>('horizontal');

const switchWidthPercent = ref<number>(50); // switch model layout when the size of the model window is < 50%

const graphElement = ref<HTMLDivElement | null>(null);
let renderer: PetrinetRenderer | NestedPetrinetRenderer | null = null;
let eventX = 0;
let eventY = 0;

const graphLegendLabels = ref<string[]>([]);
const graphLegendColors = ref<string[]>([]);

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

const setNewEquation = (index: number, latexEq: string) => {
	latexEquationList.value[index] = latexEq;
};

const deleteEquation = (index) => {
	latexEquationList.value.splice(index, 1);
};

const deleteObservable = (index) => {
	observervablesList.value.splice(index, 1);
};

const setNewObservables = (index: number, latexEq: string, mathmlEq: string) => {
	const id = extractVariablesFromMathML(mathmlEq, EquationSide.Left).join('_');
	const obs: Observable = {
		id,
		name: id,
		states: [],
		expression: latexEq,
		expression_mathml: mathmlEq
	};
	observervablesList.value[index] = obs;
	emit('update-model-observables', observervablesList.value);
};

const disableSaveObservable = computed(() => {
	const numEmptyObjservables = observervablesList.value.filter((ob) => ob.id === '').length;
	if (observervablesList.value.length > 0 && numEmptyObjservables === 0) {
		return false;
	}
	if (numEmptyObjservables > 0) {
		return true;
	}

	return false;
});

// Updates the transition equations
const updateRateEquation = (_index: number, latexEquation: string, mathml: string) => {
	editNodeObj.value.expression = latexEquation;
	editNodeObj.value.expression_mathml = mathml;
};

const cancelEditEquations = () => {
	isEditingEQ.value = false;
	latexEquationList.value = latexEquationsOriginalList.value.map((eq) => eq);
	equationsRef.value.forEach((eq) => {
		eq.isEditingEquation = false;
	});
};

const cancelEditObservables = () => {
	isEditingObservables.value = false;
	observervablesList.value = observervablesList.value.filter((eq) => eq.expression !== '');
	observablesRefs.value.forEach((eq) => {
		eq.isEditingEquation = false;
	});
};

const updateObservables = () => {
	if (isEditingObservables.value) {
		isEditingObservables.value = false;
		// update
		emit(
			'update-model-observables',
			observablesRefs.value.map((eq) => ({
				id: eq.target.id,
				name: eq.target.name,
				expression: eq.target.mathLiveField.value,
				expression_mathml: eq.target.mathLiveField.getValue('math-ml'),
				states: extractVariablesFromMathML(eq.target.mathLiveField.getValue('math-ml'))
			}))
		);
		observablesRefs.value.forEach((eq) => {
			eq.isEditingEquation = false;
		});
	} else {
		isEditingObservables.value = true;
	}
};

const mathEditorSelected = computed(() => {
	if (isEditingEQ.value) {
		return 'math-editor-selected';
	}
	return '';
});

const updateLatexFormula = (equationsList: string[]) => {
	latexEquationList.value = equationsList;
	if (latexEquationsOriginalList.value.length === 0)
		latexEquationsOriginalList.value = equationsList.map((eq) => eq);
};

const editorKeyHandler = (event: KeyboardEvent) => {
	// Ignore backspace if the current focus is a text/input box
	if ((event.target as HTMLElement).tagName === 'INPUT') {
		return;
	}

	if (event.key === 'Backspace' && renderer) {
		if (renderer && renderer.nodeSelection) {
			const nodeData = renderer.nodeSelection.datum();
			renderer.removeNode(nodeData.id);
		}

		if (renderer && renderer.edgeSelection) {
			const edgeData = renderer.edgeSelection.datum();
			renderer.removeEdge(edgeData.source, edgeData.target);
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
			prepareStateEdit();
		}
	},
	{
		label: 'Add transition',
		icon: 'pi pi-fw pi-stop',
		command: () => {
			prepareTransitionEdit();
		}
	}
]);

const isCollapsed = ref(true);
async function toggleCollapsedView() {
	isCollapsed.value = !isCollapsed.value;
	if (props.model) {
		const graphData: IGraph<NodeData, EdgeData> = getGraphData(props.model, isCollapsed.value);
		// Render graph
		if (renderer) {
			renderer.isGraphDirty = true;
			await renderer.setData(graphData);
			await renderer.render();
		}
	}
}

// Render graph whenever a new model is fetched or whenever the HTML element
//	that we render the graph to changes.
watch(
	[() => props.model, graphElement],
	async () => {
		if (props.model === null || graphElement.value === null) return;
		const graphData: IGraph<NodeData, EdgeData> = getGraphData(props.model, isCollapsed.value);

		// Create renderer
		renderer = getPetrinetRenderer(props.model, graphElement.value as HTMLDivElement);
		if (renderer.constructor === NestedPetrinetRenderer && renderer.dims?.length) {
			graphLegendLabels.value = renderer.dims;
			graphLegendColors.value = renderer.depthColorList;
		}

		renderer.on('node-dbl-click', (_eventName, _event, selection, thisRenderer) => {
			if (isEditing.value === true) {
				const data = selection.datum();
				const rate = thisRenderer.graph.amr.semantics?.ode?.rates.find((d) => d.target === data.id);
				editNodeObj.value = {
					id: data.id,
					name: data.label,
					nodeType: data.data.type,
					expression: rate?.expression ? rate.expression : ''
				};
				previousId = data.id;
				openEditNode.value = true;
			}
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

		// Update the latex equations
		if (latexEquationList.value.length > 0) {
			/* TODO
					We need to remedy the fact that the equations are not being updated;
				A proper merging of the equations is needed with a diff UI for the user.
				For now, we do nothing.
			 */
		} else {
			const latexFormula = await petriToLatex(convertAMRToACSet(props.model));
			if (latexFormula) {
				updateLatexFormula(cleanLatexEquations(latexFormula.split(' \\\\')));
			}
		}
	},
	{ deep: true }
);

const updatePetriNet = async (model: Model) => {
	// Convert PetriNet into a graph
	const graphData = getGraphData(model, isCollapsed.value);

	if (renderer) {
		await renderer.setData(graphData);
		renderer.isGraphDirty = true;
		await renderer.render();
	}
	updateLatexFormula(latexEquationList.value);
};

// Update the model from the new mathml equations
const onClickUpdateModel = async () => {
	const model = await latexToAMR(latexEquationList.value, 'petrinet', props.model?.id);
	if (model) {
		if (props.model) {
			const newModel = updateExistingModelContent(model, props.model);
			await updatePetriNet(newModel);
			await updateModel(newModel);
		} else {
			await updatePetriNet(model);
			// FIXME - I don't understand why props.model could be null; but for the hackthon this is a quick fix.
			// createModel(model);
		}

		if (renderer) {
			emit('update-model-content', renderer.graph);
		}
	}
};

const toggleEditMode = () => {
	isEditing.value = !isEditing.value;
	renderer?.setEditMode(isEditing.value);
	if (!isEditing.value && props.model && renderer) {
		emit('update-model-content', renderer.graph);
		updateModel(renderer.graph.amr);
	}
};

// Cancel existing edits, currently this will:
// - Resets changes to the model structure
const cancelEdit = async () => {
	isEditing.value = false;
	if (!props.model) return;

	// Convert petri net into a graph with raw input data
	const graphData: IGraph<NodeData, EdgeData> = getGraphData(props.model, isCollapsed.value);

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

const prepareStateEdit = () => {
	editNodeObj.value = {
		id: '',
		name: '',
		nodeType: NodeType.State,
		expression: '',
		expression_mathml: ''
	};
	openEditNode.value = true;
};

const prepareTransitionEdit = () => {
	editNodeObj.value = {
		id: '',
		name: '',
		nodeType: NodeType.Transition,
		expression: '',
		expression_mathml: ''
	};
	openEditNode.value = true;
};

const addNode = async () => {
	if (!renderer) return;
	const node = editNodeObj.value;
	if (!node?.id) {
		return;
	}
	if (props.model?.model.states.find((s) => s.id === node.id)) {
		return;
	}
	if (props.model?.model.transitions.find((t) => t.id === node.id)) {
		return;
	}
	node.expression_mathml = editNodeMathEditor.value?.mathLiveField.getValue('math-ml');

	if (!previousId) {
		if (eventX && eventY) {
			renderer.addNode(node.nodeType, node.id, node.name, { x: eventX, y: eventY });
		} else {
			renderer.addNodeCenter(node.nodeType, node.id, node.name);
		}
	} else {
		renderer.updateNode(previousId, node.id, node.name, node.expression);
		previousId = null;
	}

	eventX = -1;
	eventY = -1;
	openEditNode.value = false;
};

const observablesList = computed(() => props.model?.semantics?.ode?.observables ?? []);

onMounted(() => {
	document.addEventListener('keyup', editorKeyHandler);
	window.addEventListener('resize', handleResize);
	handleResize();
	if (observablesList.value.length > 0) {
		observervablesList.value = observablesList.value.filter((ob) => ob.expression);
	}
});

onUnmounted(() => {
	document.removeEventListener('keyup', editorKeyHandler);
	window.removeEventListener('resize', handleResize);
});

onUpdated(() => {
	if (props.model) {
		emit('asset-loaded');
	}
});
</script>

<style scoped>
main {
	overflow: auto;
}

.p-accordion {
	display: flex;
	flex-direction: column;
	gap: 1rem;
}

.diagram-container {
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
	display: flex;
	flex-direction: column;
}

.diagram-container-editing {
	box-shadow: inset 0 0 0 1px #1b8073, inset 0 0 0 1px #1b8073, inset 0 0 0 1px #1b8073,
		inset 0 0 0 1px var(--primary-color);
	border: 2px solid var(--primary-color);
	border-radius: var(--border-radius);
}

.preview {
	min-height: 8rem;
	background-color: var(--surface-secondary);
	flex-grow: 1;
	overflow: hidden;
	border: none;
	position: relative;
}

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

.graph-container {
	background-color: var(--surface-secondary);
	height: 100%;
	max-height: 100%;
	flex-grow: 1;
	overflow: hidden;
	border: none;
	position: relative;
}
.graph-element {
	background-color: var(--surface-secondary);
	height: 100%;
}
.legend {
	position: absolute;
	bottom: 0;
	left: 0;
	display: flex;
	margin: 1rem;
}
.legend-item {
	display: flex;
	align-items: center;
	margin: 0 1rem;
}
.legend-circle {
	display: inline-block;
	height: 1rem;
	width: 1rem;
	border-radius: 50%;
	margin-right: 0.5rem;
}

.math-editor-container {
	display: flex;
	flex-direction: column;
	border: 4px solid transparent;
	border-radius: var(--border-radius);
	position: relative;
	top: v-bind('mathContainerStyle');
}

.observable-editor-container {
	display: flex;
	flex-direction: column;
	border: 4px solid transparent;
	border-radius: var(--border-radius);
	position: relative;
	top: -1rem;
}

.controls {
	display: flex;
	flex-direction: row;
	margin: 0rem 1rem;
	justify-content: flex-end;
	position: relative;
	z-index: 20;
}

.edit-button {
	margin-left: 5px;
	margin-right: 5px;
}

.add-equation-button {
	width: 10rem;
	margin-left: 1rem;
	margin-top: 0.5rem;
	margin-bottom: 1rem;
	border: none;
	outline: none;
}

/* Let svg dynamically resize when the sidebar opens/closes or page resizes */
:deep(.graph-element svg) {
	width: 100%;
	height: 100%;
}

.edit-modal:deep(main) {
	max-width: 50rem;
}

.modal-input-container {
	display: flex;
	flex-direction: column;
	flex-grow: 1;
}

.modal-input {
	height: 25px;
	padding-left: 5px;
	margin: 5px;
	align-items: baseline;
}

.modal-input-label {
	margin-left: 5px;
	padding-top: 5px;
	padding-bottom: 5px;
	align-items: baseline;
}
</style>
