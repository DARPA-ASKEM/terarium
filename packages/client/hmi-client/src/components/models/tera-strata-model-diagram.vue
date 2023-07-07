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
							<section v-if="showTypingToolbar">
								<div class="typing-row">
									<div>COLOR</div>
									<div class="input-header">NODE TYPE</div>
									<div class="input-header">NAME OF TYPE</div>
									<div class="input-header">ASSIGN TO</div>
									<div><div class="empty-spacer" :style="{ width: `28px` }"></div></div>
								</div>
								<div class="typing-row" v-for="(modelType, index) in modelTypes" :key="index">
									<div>
										<div
											:class="
												modelType.nodeType === 'Variable'
													? `legend-key-state`
													: `legend-key-transition-lg`
											"
											:style="{ backgroundColor: strataTypeColors[index] }"
										/>
									</div>
									<div>
										<Dropdown
											class="p-inputtext-sm"
											:options="nodeTypes"
											v-model="modelType.nodeType"
										/>
									</div>
									<div>
										<InputText class="p-inputtext-sm" />
									</div>
									<div>
										<Dropdown class="p-inputtext-sm" placeholder="Select nodes" />
									</div>
									<div>
										<Button icon="pi pi-times" text rounded />
									</div>
								</div>
							</section>
							<section class="legend">
								<ul>
									<li v-for="(type, i) in stateTypes" :key="i">
										<div
											class="legend-key-state"
											:style="{ backgroundColor: strataTypeColors[i] }"
										/>
										{{ type }}
									</li>
								</ul>
								<ul>
									<li v-for="(type, i) in transitionTypes" :key="i">
										<div
											class="legend-key-transition"
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
import { IGraph } from '@graph-scaffolder/index';
import { watch, ref, computed, onMounted, onUnmounted, onUpdated } from 'vue';
import { runDagreLayout } from '@/services/graph';
import {
	PetrinetRenderer,
	NodeData,
	EdgeData
} from '@/model-representation/petrinet/petrinet-renderer';
import { petriToLatex } from '@/petrinet/petrinet-service';
import {
	convertAMRToACSet,
	convertToIGraph
} from '@/model-representation/petrinet/petrinet-service';
import Button from 'primevue/button';
import Splitter from 'primevue/splitter';
import SplitterPanel from 'primevue/splitterpanel';
import { Model } from '@/types/Types';
import { strataTypeColors } from '@/utils/color-schemes';
import Dropdown from 'primevue/dropdown';
import InputText from 'primevue/inputtext';
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
	isEditable: boolean;
	nodePreview?: boolean;
	showTypingToolbar?: boolean;
}>();

const menu = ref();

const newModelName = ref('New Model');

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
const modelTypes = ref<
	{
		nodeType: string;
		typeName?: string;
		assignTo?: string;
	}[]
>([]);
// const numberNodes = computed<number>(() => props.model?.model.states.length + props.model?.model.transitions.length);
const nodeTypes = ref<string[]>(['Variable', 'Transition']);

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
			modelTypes.value.push(
				{
					nodeType: 'Variable',
					typeName: props.model.semantics?.typing?.type_system.states[0].name
				},
				{
					nodeType: 'Transition',
					typeName: props.model.semantics?.typing?.type_system.transitions[0].properties?.name
				}
			);
			const data = await petriToLatex(convertAMRToACSet(props.model));
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

		renderer.on('background-click', () => {
			if (menu.value) menu.value.hide();
		});

		// Render graph
		await renderer?.setData(graphData);
		await renderer?.render();
		const latexFormula = await petriToLatex(convertAMRToACSet(props.model));
		if (latexFormula) {
			updateLatexFormula(latexFormula);
		} else {
			updateLatexFormula('');
		}
	},
	{ deep: true }
);

/*
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
*/

onMounted(async () => {
	document.addEventListener('keyup', editorKeyHandler);
});

onUnmounted(() => {
	document.removeEventListener('keyup', editorKeyHandler);
});
</script>

<style scoped>
main {
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
	overflow: auto;
}

.legend {
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
.legend-key-state {
	height: 24px;
	width: 24px;
	border-radius: 12px;
}

.legend-key-transition {
	height: 16px;
	width: 16px;
	border-radius: 4px;
}

.legend-key-transition-lg {
	height: 24px;
	width: 24px;
	border-radius: 6px;
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

.typing-row {
	display: flex;
	justify-content: space-around;
	align-items: center;
	padding: 1rem;
	color: var(--text-color-subdued);
	gap: 1rem;
	font-size: var(--font-caption);
}

.typing-row > div {
	display: flex;
	flex: 1 1 auto;
	justify-content: flex-start;
}

.typing-row > div:first-of-type {
	flex: 0 0 48px;
	min-width: 0;
}

.typing-row > div:last-of-type {
	flex: 0 1 0px;
	min-width: 0;
}

.p-inputtext,
.p-dropdown {
	min-width: 150px;
}

.input-header {
	min-width: 150px;
}
</style>
