<template>
	<main>
		<Accordion v-if="!nodePreview" multiple :activeIndex="[0, 1, 2]">
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
								<span class="toolbar-subgroup">
									<Button
										v-if="isEditing"
										@click="prepareStateEdit()"
										label="Add state"
										class="p-button-sm p-button-outlined toolbar-button"
									/>
									<Button
										v-if="isEditing"
										@click="prepareTransitionEdit()"
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
				</TeraResizablePanel>
			</AccordionTab>
			<AccordionTab header="Model equations">
				<TeraResizablePanel
					:class="isEditingEQ ? `diagram-container-editing` : `diagram-container`"
				>
					<section class="controls">
						<span v-if="props.isEditable" class="equation-edit-button">
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
						</span>
					</section>
					<section class="math-editor-container" :class="mathEditorSelected">
						<tera-math-editor
							v-for="(eq, index) in latexEquationList"
							:key="index"
							:index="index"
							:is-editable="isEditable"
							:latex-equation="eq"
							:is-editing-eq="isEditingEQ"
							:is-math-ml-valid="isMathMLValid"
							@equation-updated="setNewEquation"
							@delete="deleteEquation"
							ref="equationsRef"
						>
						</tera-math-editor>
						<Button
							v-if="isEditingEQ"
							class="p-button-sm add-equation-button"
							icon="pi pi-plus"
							label="Add Equation"
							@click="latexEquationList.push('')"
							text
						/>
					</section>
				</TeraResizablePanel>
			</AccordionTab>
			<AccordionTab header="Model Observables">
				<TeraResizablePanel
					:class="isEditingObservables ? `diagram-container-editing` : `diagram-container`"
					:start-height="200"
				>
					<section class="controls">
						<span v-if="props.isEditable" class="equation-edit-button">
							<Button
								v-if="isEditingObservables"
								@click="cancelEditObservables"
								label="Cancel"
								class="p-button-sm p-button-outlined edit-button"
							/>
							<Button
								v-if="observervablesList.length !== 0"
								@click="updateObservables"
								:label="isEditingObservables ? 'Update observable' : 'Edit observables'"
								:class="
									isEditingObservables ? 'p-button-sm' : 'p-button-sm p-button-outlined edit-button'
								"
							/>
						</span>
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
							@equation-updated="setNewObservables"
							@delete="deleteObservable"
							ref="observablesRefs"
						>
						</tera-math-editor>
						<Button
							v-if="observablesList.length === 0 || isEditingObservables"
							class="p-button-sm add-equation-button"
							icon="pi pi-plus"
							label="Add Observable"
							@click="addObservable"
							text
						/>
					</section>
				</TeraResizablePanel>
			</AccordionTab>
		</Accordion>
		<div v-else-if="model" ref="graphElement" class="graph-element preview" />
	</main>

	<Teleport to="body">
		<tera-modal
			class="edit-modal"
			v-if="openEditNode === true"
			@modal-mask-clicked="openEditNode = false"
		>
			<template #header>
				<h4>Add/Edit {{ editNodeObj.nodeType }}</h4>
			</template>
			<div>
				<InputText v-model="editNodeObj.id" placeholder="Id" />
			</div>
			<div>
				<InputText v-model="editNodeObj.name" placeholder="Name" />
			</div>
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
import { runDagreLayout } from '@/services/graph';
import {
	PetrinetRenderer,
	NodeData,
	EdgeData,
	NodeType
} from '@/model-representation/petrinet/petrinet-renderer';
import { petriToLatex } from '@/petrinet/petrinet-service';
import {
	convertAMRToACSet,
	convertToIGraph
} from '@/model-representation/petrinet/petrinet-service';
import { mathmlToAMR } from '@/services/models/extractions';
import { separateEquations } from '@/utils/math';
import { updateModel } from '@/services/model';
import Button from 'primevue/button';
import ContextMenu from 'primevue/contextmenu';
import TeraMathEditor from '@/components/mathml/tera-math-editor.vue';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Toolbar from 'primevue/toolbar';
import { Model, Observable } from '@/types/Types';
import TeraModal from '@/components/widgets/tera-modal.vue';
import InputText from 'primevue/inputtext';
import TeraResizablePanel from '../widgets/tera-resizable-panel.vue';

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
// const newLatexEquationsList = ref<string[]>([]);
const isMathMLValid = ref<boolean>(true);

// Observable Equations
const observablesRefs = ref<any[]>([]);
const observervablesList = ref<Observable[]>([]);

// For model editing
interface AddStateObj {
	id: string;
	name: string;
	nodeType: string;
}

const openEditNode = ref<boolean>(false);
const editNodeObj = ref<AddStateObj>({ id: '', name: '', nodeType: '' });
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

const setNewEquation = (index: number, latexEq: string) => {
	latexEquationList.value[index] = latexEq;
};

const deleteEquation = (index) => {
	latexEquationList.value.splice(index, 1);
};

const deleteObservable = (index) => {
	observervablesList.value.splice(index, 1);
};

const setNewObservables = (
	index: number,
	latexEq: string,
	mathmlEq: string,
	name: string,
	id: string
) => {
	const obs: Observable = {
		id,
		name,
		states: [],
		expression: latexEq,
		expression_mathml: mathmlEq
	};
	observervablesList.value[index] = obs;
	emit('update-model-observables', observervablesList.value);
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

function extractVariablesFromMathML(mathML: string): string[] {
	const parser = new DOMParser();
	const xmlDoc = parser.parseFromString(mathML, 'text/xml');

	const variables: string[] = [];

	const miElements = xmlDoc.getElementsByTagName('mi');
	for (let i = 0; i < miElements.length; i++) {
		const miElement = miElements[i];
		const variable = miElement.textContent?.trim();
		if (variable) {
			variables.push(variable);
		}
	}

	return variables;
}

const updateObservables = () => {
	if (isEditingObservables.value) {
		isEditingObservables.value = false;
		// update
		emit(
			'update-model-observables',
			observablesRefs.value.map((eq, index) => {
				console.log(eq);
				return {
					id: eq.id || observablesRefs.value[index].id,
					name: eq.name || observablesRefs.value[index].name,
					expression: eq.mathLiveField.value,
					expression_mathml: eq.mathLiveField.getValue('math-ml'),
					states: extractVariablesFromMathML(eq.mathLiveField.getValue('math-ml'))
				};
			})
		);
		observablesRefs.value.forEach((eq) => {
			eq.isEditingEquation = false;
		});
	} else {
		isEditingObservables.value = true;
	}
};

const mathEditorSelected = computed(() => {
	if (!isMathMLValid.value) {
		return 'math-editor-error';
	}
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

// Get the MathML list of equations
const mathmlequations = computed(
	() =>
		equationsRef.value
			.map((eq) => separateEquations(eq.mathLiveField.getValue('math-ml')))
			.flat() as Array<string>
);

watch(
	() => [latexEquationList.value],
	() => {
		const mathMLEquations = equationsRef.value.map((eq) =>
			separateEquations(eq.mathLiveField.getValue('math-ml'))
		);
		validateMathML(mathMLEquations.flat(), false);
	},
	{ deep: true }
);

// Whenever selectedModelId changes, fetch model with that ID
watch(
	() => [props.model],
	async () => {
		updateLatexFormula([]);
		if (props.model) {
			const data = await petriToLatex(convertAMRToACSet(props.model));
			const eqList = data
				?.split(' \\\\')
				.map(
					(elem) =>
						`\\begin{align} ${elem
							.replace('\\\\', '\\')
							.replace('\\begin{align}', '')
							.replace('\\end{align}', '')
							.trim()} \\end{align}`
				);

			if (data) {
				updateLatexFormula(eqList || []);
			}
		}
	},
	{ immediate: true }
);

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

// Render graph whenever a new model is fetched or whenever the HTML element
//	that we render the graph to changes.
watch(
	[() => props.model, graphElement],
	async () => {
		if (props.model === null || graphElement.value === null) return;
		const graphData: IGraph<NodeData, EdgeData> = convertToIGraph(props.model);

		// Create renderer
		renderer = new PetrinetRenderer({
			el: graphElement.value as HTMLDivElement,
			useAStarRouting: false,
			useStableZoomPan: true,
			runLayout: runDagreLayout,
			dragSelector: 'no-drag'
		});

		renderer.on('node-dbl-click', (_eventName, _event, selection) => {
			const data = selection.datum();
			editNodeObj.value = {
				id: data.id,
				name: data.label,
				nodeType: data.data.type
			};
			previousId = data.id;
			openEditNode.value = true;
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
		const latexFormula = await petriToLatex(convertAMRToACSet(props.model));
		const eqList = latexFormula
			?.split(' \\\\')
			.map(
				(elem) =>
					`\\begin{align} ${elem
						.replace('\\\\', '\\')
						.replace('\\begin{align}', '')
						.replace('\\end{align}', '')
						.trim()} \\end{align}`
			);
		if (latexFormula) {
			updateLatexFormula(eqList || []);
		} else {
			updateLatexFormula([]);
		}
	},
	{ deep: true }
);

const updatePetriNet = async (model: Model) => {
	// Convert PetriNet into a graph
	const graphData: IGraph<NodeData, EdgeData> = convertToIGraph(model);

	if (renderer) {
		await renderer.setData(graphData);
		renderer.isGraphDirty = true;
		await renderer.render();
	}
	updateLatexFormula(latexEquationList.value);
};

const validateMathML = async (mathMLStringList: string[], editMode: boolean) => {
	isMathMLValid.value = false;
	if (mathMLStringList.length === 0) {
		isMathMLValid.value = true;
	} else if (editMode) {
		isMathMLValid.value = true;
	}
};

const onClickUpdateModel = async () => {
	const model = (await mathmlToAMR(mathmlequations.value)) as Model;
	if (model) {
		await updatePetriNet(model);
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
// - Resets changs to the model structure
const cancelEdit = async () => {
	isEditing.value = false;
	if (!props.model) return;

	// Convert petri net into a graph with raw input data
	const graphData: IGraph<NodeData, EdgeData> = convertToIGraph(props.model);

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
	editNodeObj.value = { id: '', name: '', nodeType: NodeType.State };
	openEditNode.value = true;
};

const prepareTransitionEdit = () => {
	editNodeObj.value = { id: '', name: '', nodeType: NodeType.Transition };
	openEditNode.value = true;
};

const addNode = async () => {
	if (!renderer) return;

	const node = editNodeObj.value;

	if (!previousId) {
		if (eventX && eventY) {
			renderer.addNode(node.nodeType, node.id, node.name, { x: eventX, y: eventY });
		} else {
			renderer.addNodeCenter(node.nodeType, node.id, node.name);
		}
	} else {
		renderer.updateNode(previousId, node.id, node.name);
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
	border: 1px solid var(--surface-border);
	border-radius: var(--border-radius);
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

.floating-edit-button {
	background-color: var(--surface-0);
	margin-top: 10px;
	position: absolute;
	right: 10px;
	z-index: 10;
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
	padding-top: 50px;
	padding-bottom: 20px;
}

.observable-editor-container {
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
	padding-top: 50px;
	padding-bottom: 20px;
}

.controls {
	display: flex;
	flex-direction: row;
	margin: 0.5rem 2.5rem 0px 10px;
	justify-content: flex-end;
	position: relative;
	z-index: 20;
}

.edit-button {
	margin-left: 5px;
	margin-right: 5px;
}

.add-equation-button {
	width: 150px;
	min-height: 30px;
	margin-left: 5px;
	margin-top: 5px;
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
</style>
