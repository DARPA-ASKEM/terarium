<template>
	<main>
		<TeraResizablePanel v-if="!nodePreview">
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
							</Toolbar>
							<section class="legend">
								<ul>
									<li v-for="(type, i) in stateTypes" :key="i">
										<div class="legend-state" :style="{ backgroundColor: strataTypeColors[i] }" />
										{{ type }}
									</li>
								</ul>
								<ul>
									<li v-for="(type, i) in transitionTypes" :key="i">
										<div
											class="legend-transition"
											:style="{ backgroundColor: strataTypeColors[stateTypes?.length ?? 0 + i] }"
										/>
										{{ type }}
									</li>
								</ul>
							</section>
							<div v-if="model" ref="graphElement" class="graph-element" />
						</section>
					</SplitterPanel>
				</Splitter>
			</div>
		</TeraResizablePanel>
		<div v-else-if="model" ref="graphElement" class="graph-element preview" />
	</main>
</template>

<script setup lang="ts">
import { remove } from 'lodash';
import { IGraph } from '@graph-scaffolder/index';
import { watch, ref, computed, onMounted, onUnmounted, onUpdated } from 'vue';
import { runDagreLayout } from '@/services/graph';
import { PetrinetRenderer } from '@/petrinet/petrinet-renderer';
import { NodeData, EdgeData, petriToLatex } from '@/petrinet/petrinet-service';
import { parseAMR2IGraph, AMRToPetri } from '@/model-representation/petrinet/petrinet-service';
import Button from 'primevue/button';
import Splitter from 'primevue/splitter';
import SplitterPanel from 'primevue/splitterpanel';
import Toolbar from 'primevue/toolbar';
import { Model } from '@/types/Types';
import { strataTypeColors } from '@/utils/color-schemes';
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
	model: Model | null;
	nodePreview?: boolean;
}>();

const newModelName = ref('New Model');
const selectedVariable = ref('');
const equationLatex = ref<string>('');
const equationLatexOriginal = ref<string>('');
const splitterContainer = ref<HTMLElement | null>(null);
const layout = ref<'horizontal' | 'vertical' | undefined>('horizontal');
const switchWidthPercent = ref<number>(50); // switch model layout when the size of the model window is < 50%
const equationPanelSize = ref<number>(50);
const equationPanelMinSize = ref<number>(0);
const equationPanelMaxSize = ref<number>(100);
const graphElement = ref<HTMLDivElement | null>(null);
let renderer: PetrinetRenderer | null = null;
const modelTypeSystem = computed(() => props.model?.semantics?.typing?.type_system);
const stateTypes = computed(() => modelTypeSystem.value?.states.map((s) => s.name));
const transitionTypes = computed(() =>
	modelTypeSystem.value?.transitions.map((t) => t.properties?.name)
);

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

const updateLatexFormula = (formulaString: string) => {
	equationLatex.value = formulaString;
	equationLatexOriginal.value = formulaString;
};

// Whenever selectedModelId changes, fetch model with that ID
watch(
	() => [props.model],
	async () => {
		updateLatexFormula('');
		if (props.model) {
			const data = await petriToLatex(AMRToPetri(props.model));
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

// Render graph whenever a new model is fetched or whenever the HTML element
//	that we render the graph to changes.
watch(
	[() => props.model, graphElement],
	async () => {
		if (props.model === null || graphElement.value === null) return;
		const graphData: IGraph<NodeData, EdgeData> = parseAMR2IGraph(props.model, {
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

		renderer.on('node-click', async (_evtName, _evt, _e, _renderer, d) => {
			// Note: do not change the renderer's visuals, this is done internally
			onVariableSelected(d.label);
		});

		// Render graph
		await renderer?.setData(graphData);
		await renderer?.render();
		const latexFormula = await petriToLatex(AMRToPetri(props.model));
		if (latexFormula) {
			updateLatexFormula(latexFormula);
		} else {
			updateLatexFormula('');
		}
	},
	{ deep: true }
);

onMounted(async () => {
	// fetchRelatedTerariumArtifacts();
	document.addEventListener('keyup', editorKeyHandler);
});

onUnmounted(() => {
	document.removeEventListener('keyup', editorKeyHandler);
});

const resetZoom = async () => {
	renderer?.setToDefaultZoom();
};
</script>

<style scoped>
main {
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
	overflow: auto;
}

.preview {
	min-height: 8rem;
	background-color: var(--surface-secondary);
	flex-grow: 1;
	overflow: hidden;
	border: none;
	position: relative;
}

.legend {
	position: absolute;
	bottom: 0;
	z-index: 1;
	margin-bottom: 1rem;
	margin-left: 1rem;
	display: flex;
	gap: 1rem;
}
.legend-state {
	height: 24px;
	width: 24px;
	border-radius: 12px;
}

.legend-transition {
	height: 16px;
	width: 16px;
	border-radius: 4px;
}

ul {
	display: flex;
	gap: 0.5rem;
	list-style-type: none;
}

li {
	display: flex;
	align-items: center;
	gap: 0.5rem;
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

.p-splitter {
	border: none;
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
