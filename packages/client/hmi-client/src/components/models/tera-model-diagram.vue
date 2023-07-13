<template>
	<main>
		<section v-if="!nodePreview" class="graph-element">
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
		<div v-else-if="model" ref="graphElement" class="graph-element preview" />
	</main>
</template>

<script setup lang="ts">
import { IGraph } from '@graph-scaffolder/index';
import { watch, ref, onMounted, onUnmounted, onUpdated } from 'vue';
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
import { separateEquations } from '@/utils/math';
import { updateModel } from '@/services/model';
import Button from 'primevue/button';
import ContextMenu from 'primevue/contextmenu';
import Toolbar from 'primevue/toolbar';
import { Model } from '@/types/Types';

// Get rid of these emits
const emit = defineEmits([
	'close-preview',
	'asset-loaded',
	'close-current-tab',
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

// Model Equations
const equationsRef = ref<any[]>([]);
const latexEquationList = ref<string[]>([]);
const latexEquationsOriginalList = ref<string[]>([]);

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

const updateLatexFormula = (equationsList: string[]) => {
	latexEquationList.value = equationsList;
	if (latexEquationsOriginalList.value.length === 0)
		latexEquationsOriginalList.value = equationsList.map((eq) => eq);
};

function joinStringLists(lists: string[][]): string[] {
	return ([] as string[]).concat(...lists);
}

watch(
	() => [latexEquationList.value],
	() => {
		const mathMLEquations = equationsRef.value.map((eq) =>
			separateEquations(eq.mathLiveField.getValue('math-ml'))
		);
		validateMathML(joinStringLists(mathMLEquations), false);
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
			if (renderer) {
				renderer.addNode(NodeType.State, 'state', { x: eventX, y: eventY });
			}
		}
	},
	{
		label: 'Add transition',
		icon: 'pi pi-fw pi-stop',
		command: () => {
			if (renderer) {
				renderer.addNode(NodeType.Transition, 'transition', { x: eventX, y: eventY });
			}
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

const addState = async () => {
	renderer?.addNodeCenter(NodeType.State, 'state');
};

const addTransition = async () => {
	renderer?.addNodeCenter(NodeType.Transition, 'transition');
};

onMounted(() => {
	document.addEventListener('keyup', editorKeyHandler);
	window.addEventListener('resize', handleResize);
	handleResize();
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

.graph-element {
	background-color: var(--surface-secondary);
	height: 100%;
	max-height: 100%;
	flex-grow: 1;
	overflow: hidden;
	border: none;
	position: relative;
}

/* Let svg dynamically resize when the sidebar opens/closes or page resizes */
:deep(.graph-element svg) {
	width: 100%;
	height: 100%;
}
</style>
