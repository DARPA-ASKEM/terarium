<template>
	<main>
		<TeraResizablePanel v-if="!isPreview" class="diagram-container">
			<section class="graph-element">
				<Toolbar>
					<template #start>
						<span>
							<Button @click="resetZoom" label="Reset zoom" class="p-button-sm p-button-outlined" />
						</span>
					</template>
					<template #center>
						<span v-if="isEditing">
							<Button
								@click="prepareStateEdit()"
								label="Add state"
								class="p-button-sm p-button-outlined"
							/>
							<Button
								@click="prepareTransitionEdit()"
								label="Add transition"
								class="p-button-sm p-button-outlined"
							/>
						</span>
					</template>
					<template #end>
						<span>
							<template v-if="isEditable">
								<Button
									v-if="isEditing"
									@click="cancelEdit"
									label="Cancel"
									class="p-button-sm p-button-outlined"
								/>
								<Button
									@click="toggleEditMode"
									:label="isEditing ? 'Save model' : 'Edit model'"
									:class="isEditing ? 'p-button-sm save-model' : 'p-button-sm p-button-outlined'"
								/>
							</template>
							<SelectButton
								v-if="model && getStratificationType(model)"
								:model-value="stratifiedView"
								@change="
									if ($event.value) {
										stratifiedView = $event.value;
										toggleCollapsedView();
									}
								"
								:options="stratifiedViewOptions"
								option-value="value"
							>
								<template #option="slotProps">
									<i :class="`${slotProps.option.icon} p-button-icon-left`" />
									<span class="p-button-label">{{ slotProps.option.value }}</span>
								</template>
							</SelectButton>
						</span>
					</template>
				</Toolbar>
				<tera-model-type-legend v-if="model" class="legend-anchor" :model="model" />
				<div v-if="model" class="graph-container">
					<div ref="graphElement" class="graph-element" />
					<div class="legend">
						<div class="legend-item" v-for="(label, index) in graphLegendLabels" :key="index">
							<div class="legend-circle" :style="`background: ${graphLegendColors[index]}`"></div>
							{{ label }}
						</div>
					</div>
				</div>
				<ContextMenu ref="menu" :model="contextMenuItems" />
			</section>
		</TeraResizablePanel>
		<div
			v-else-if="model"
			ref="graphElement"
			class="graph-element preview"
			:style="!isEditable && { pointerEvents: 'none' }"
		/>
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
							ref="editNodeMathEditor"
							:keep-open="true"
							:is-editing-eq="true"
							:latex-equation="editNodeObj.expression"
							@equation-updated="updateRateEquation"
						/>
					</div>
				</template>
				<template #footer>
					<Button label="Submit" :disabled="editNodeObj.id === ''" @click="addNode" />
					<Button label="Cancel" class="p-button-secondary" @click="openEditNode = false" />
				</template>
			</tera-modal>
			<tera-stratified-matrix-modal
				v-if="openValueConfig && modelConfiguration"
				:id="selectedTransitionId"
				:model-configuration="modelConfiguration"
				:stratified-model-type="StratifiedModel.Mira"
				:stratified-matrix-type="StratifiedMatrix.Rates"
				:open-value-config="openValueConfig"
				@close-modal="openValueConfig = false"
				@update-configuration="
					(configToUpdate: ModelConfiguration) => emit('update-configuration', configToUpdate)
				"
			/>
		</Teleport>
	</main>
</template>

<script setup lang="ts">
import { watch, ref, onMounted, onUnmounted } from 'vue';
import TeraMathEditor from '@/components/mathml/tera-math-editor.vue';
import TeraModal from '@/components/widgets/tera-modal.vue';
import InputText from 'primevue/inputtext';
import Toolbar from 'primevue/toolbar';
import Button from 'primevue/button';
import ContextMenu from 'primevue/contextmenu';
import {
	getStratificationType,
	StratifiedModel
} from '@/model-representation/petrinet/petrinet-service';
import { IGraph } from '@graph-scaffolder/index';
import {
	PetrinetRenderer,
	NodeData,
	EdgeData,
	NodeType
} from '@/model-representation/petrinet/petrinet-renderer';
import { getGraphData, getPetrinetRenderer } from '@/model-representation/petrinet/petri-util';
import { Model, ModelConfiguration } from '@/types/Types';
import TeraResizablePanel from '@/components/widgets/tera-resizable-panel.vue';
import { NestedPetrinetRenderer } from '@/model-representation/petrinet/nested-petrinet-renderer';
import { StratifiedMatrix } from '@/types/Model';
import SelectButton from 'primevue/selectbutton';
import TeraModelTypeLegend from './tera-model-type-legend.vue';
import TeraStratifiedMatrixModal from '../model-configurations/tera-stratified-matrix-modal.vue';

interface AddStateObj {
	id: string;
	name: string;
	nodeType: string;
	expression: string;
	expression_mathml?: string;
}

const props = defineProps<{
	model: Model;
	isEditable: boolean;
	modelConfiguration?: ModelConfiguration;
	isPreview?: boolean;
}>();

const emit = defineEmits(['update-model', 'update-configuration']);

// Model editor context menu
const menu = ref();
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
const isEditing = ref(false);
const graphElement = ref<HTMLDivElement | null>(null);
const openEditNode = ref(false);
const editNodeMathEditor = ref<typeof TeraMathEditor | null>(null);
const editNodeObj = ref<AddStateObj>({
	id: '',
	name: '',
	nodeType: '',
	expression: '',
	expression_mathml: ''
});
const splitterContainer = ref<HTMLElement | null>(null);
const layout = ref<'horizontal' | 'vertical' | undefined>('horizontal');
const switchWidthPercent = ref<number>(50); // switch model layout when the size of the model window is < 50%
const graphLegendLabels = ref<string[]>([]);
const graphLegendColors = ref<string[]>([]);
const openValueConfig = ref(false);
const selectedTransitionId = ref('');

enum StratifiedView {
	Expanded = 'Expanded',
	Collapsed = 'Collapsed'
}

const stratifiedView = ref(StratifiedView.Collapsed);
const stratifiedViewOptions = ref([
	{ value: StratifiedView.Expanded },
	{ value: StratifiedView.Collapsed }
]);

// Is this going to consistently have an option to switch from diagram to equation if not the toggle should be somewherlse
// enum

let previousId: any = null;
let renderer: PetrinetRenderer | NestedPetrinetRenderer | null = null;
let eventX = 0;
let eventY = 0;

const resetZoom = async () => {
	renderer?.setToDefaultZoom();
};

async function renderGraph(updatedModel: Model | null = null) {
	const modelToRender = updatedModel ?? props.model;

	// Convert petri net into a graph with raw input data
	const graphData: IGraph<NodeData, EdgeData> = getGraphData(modelToRender, isCollapsed.value);
	// Render graph
	if (renderer) {
		renderer.isGraphDirty = true;
		await renderer.setData(graphData);
		await renderer.render();

		if (updatedModel) {
			emit('update-model', renderer.graph.amr);
		}
	}
}
defineExpose({ renderGraph });

async function toggleCollapsedView() {
	isCollapsed.value = !isCollapsed.value;
	renderGraph();
}

// Cancel existing edits, currently this will:
// - Resets changes to the model structure
const cancelEdit = async () => {
	isEditing.value = false;
	renderer?.setEditMode(false);
	renderGraph();
};

const toggleEditMode = () => {
	isEditing.value = !isEditing.value;
	renderer?.setEditMode(isEditing.value);
	if (!isEditing.value && renderer) {
		emit('update-model', renderer.graph.amr);
	}
};

// Updates the transition equations
const updateRateEquation = (_index: number, latexEquation: string, mathml: string) => {
	editNodeObj.value.expression = latexEquation;
	editNodeObj.value.expression_mathml = mathml;
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

// Render graph whenever a new model is fetched or whenever the HTML element
// that we render the graph to changes.
// Consider just watching the model
watch(
	[() => props.model, graphElement],
	async () => {
		if (graphElement.value === null) return;
		const graphData: IGraph<NodeData, EdgeData> = getGraphData(props.model, isCollapsed.value);

		// Create renderer
		renderer = getPetrinetRenderer(props.model, graphElement.value as HTMLDivElement);
		if (renderer.constructor === NestedPetrinetRenderer && renderer.dims?.length) {
			graphLegendLabels.value = renderer.dims;
			graphLegendColors.value = renderer.depthColorList;
		}

		renderer.on('node-click', (_eventName, _event, selection) => {
			const { id, type } = selection.datum();
			if (type === NodeType.Transition) {
				selectedTransitionId.value = id;
				openValueConfig.value = true;
			}
		});

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
	},
	{ deep: true }
);

const editorKeyHandler = (event: KeyboardEvent) => {
	// Ignore backspace if the current focus is a text/input box
	if ((event.target as HTMLElement).tagName === 'INPUT') {
		return;
	}

	if (event.key === 'Backspace' && renderer) {
		if (renderer.nodeSelection) {
			const nodeData = renderer.nodeSelection.datum();
			renderer.removeNode(nodeData.id);
		}

		if (renderer.edgeSelection) {
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

const updateLayout = () => {
	if (splitterContainer.value) {
		layout.value =
			(splitterContainer.value.offsetWidth / window.innerWidth) * 100 < switchWidthPercent.value ||
			window.innerWidth < 800
				? 'vertical'
				: 'horizontal';
	}
};
const handleResize = () => updateLayout();

onMounted(() => {
	document.addEventListener('keyup', editorKeyHandler);
	window.addEventListener('resize', handleResize);
	handleResize();
});

onUnmounted(() => {
	document.removeEventListener('keyup', editorKeyHandler);
	window.removeEventListener('resize', handleResize);
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
	box-shadow:
		inset 0 0 0 1px #1b8073,
		inset 0 0 0 1px #1b8073,
		inset 0 0 0 1px #1b8073,
		inset 0 0 0 1px var(--primary-color);
	border: 2px solid var(--primary-color);
	border-radius: var(--border-radius);
}

.preview {
	/* Having both min and max heights prevents height from resizing itself while being dragged on templating canvas
	This resizes on template canvas but not when its in a workflow node?? (tera-model-node)
	FIXME: Will take a look at this again later
	*/
	min-height: 8rem;
	max-height: 8rem;
	background-color: var(--surface-secondary);
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

.p-toolbar:deep(> div > span) {
	gap: 0.25rem;
	display: flex;
}

section math-editor {
	justify-content: center;
}

/* Let svg dynamically resize when the sidebar opens/closes or page resizes */
:deep(.graph-element svg) {
	width: 100%;
	height: 100%;
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

:deep(.graph-element .p-button) {
	&,
	&:hover {
		background-color: var(--surface-secondary);
	}
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

.legend-anchor {
	position: absolute;
	bottom: 0;
	z-index: 1;
	margin-bottom: 1rem;
	margin-left: 1rem;
	display: flex;
	gap: 1rem;
	background-color: var(--surface-section);
	border-radius: 0.5rem;
	padding: 0.5rem;
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
