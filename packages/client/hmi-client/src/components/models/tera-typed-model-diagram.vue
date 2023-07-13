<template>
	<main>
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
							<section v-if="showTypingToolbar">
								<div class="typing-row">
									<div>COLOR</div>
									<div class="input-header">NODE TYPE</div>
									<div class="input-header">NAME OF TYPE</div>
									<div class="input-header">ASSIGN TO</div>
									<div><div class="empty-spacer" :style="{ width: `28px` }"></div></div>
								</div>
								<div class="typing-row" v-for="(row, index) in typedRows" :key="index">
									<!-- legend key -->
									<div>
										<div
											:class="getLegendKeyClass(row.nodeType ?? '')"
											:style="getLegendKeyStyle(row.typeName ?? '')"
										/>
									</div>
									<div>
										<!-- node type -->
										<Dropdown
											class="p-inputtext-sm"
											:options="Object.keys(assignToOptions[index])"
											v-model="row.nodeType"
										/>
									</div>
									<div>
										<!-- name of type -->
										<InputText class="p-inputtext-sm" v-model="row.typeName" />
									</div>
									<div>
										<!-- assign to -->
										<MultiSelect
											class="p-inputtext-sm"
											placeholder="Select nodes"
											:options="assignToOptions[index][row.nodeType ?? '']"
											v-model="row.assignTo"
										/>
									</div>
									<!-- cancel row  -->
									<div>
										<Button icon="pi pi-times" text rounded />
									</div>
								</div>
								<Button
									label="Add type"
									icon="pi pi-plus"
									class="p-button-sm"
									text
									@click="addTypedRow"
								/>
							</section>
							<section class="legend">
								<ul>
									<li v-for="(type, i) in stateTypes" :key="i">
										<div class="legend-key-circle" :style="getLegendKeyStyle(type ?? '')" />
										{{ type }}
									</li>
								</ul>
								<ul>
									<li v-for="(type, i) in transitionTypes" :key="i">
										<div class="legend-key-square" :style="getLegendKeyStyle(type ?? '')" />
										{{ type }}
									</li>
								</ul>
							</section>
							<div v-if="typedModel" ref="graphElement" class="graph-element" />
						</section>
					</SplitterPanel>
				</Splitter>
			</div>
		</TeraResizablePanel>
	</main>
</template>

<script setup lang="ts">
import { IGraph } from '@graph-scaffolder/index';
import { watch, ref, computed, onMounted, onUnmounted } from 'vue';
import { runDagreLayout } from '@/services/graph';
import {
	PetrinetRenderer,
	NodeData,
	EdgeData
} from '@/model-representation/petrinet/petrinet-renderer';
import { petriToLatex } from '@/petrinet/petrinet-service';
import {
	convertAMRToACSet,
	convertToIGraph,
	addTyping
} from '@/model-representation/petrinet/petrinet-service';
import Button from 'primevue/button';
import Splitter from 'primevue/splitter';
import SplitterPanel from 'primevue/splitterpanel';
import { Model, State, Transition, TypeSystem, TypingSemantics } from '@/types/Types';
import { useNodeTypeColorMap } from '@/utils/color-schemes';
import Dropdown from 'primevue/dropdown';
import MultiSelect from 'primevue/multiselect';
import InputText from 'primevue/inputtext';
import {
	generateTypeTransition,
	generateTypeState
} from '@/services/models/stratification-service';
import TeraResizablePanel from '../widgets/tera-resizable-panel.vue';

// Get rid of these emits
const emit = defineEmits([
	'update-tab-name',
	'close-preview',
	'asset-loaded',
	'close-current-tab',
	'update-model-content',
	'all-nodes-typed'
]);

const props = defineProps<{
	model: Model;
	showTypingToolbar: boolean;
	typeSystem?: TypeSystem;
}>();

const typedModel = ref<Model>(props.model);

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

const stateTypes = computed(() =>
	props.model.semantics?.typing?.type_system?.states.map((s) => s.name)
);
const transitionTypes = computed(() =>
	props.model.semantics?.typing?.type_system?.transitions.map((t) => t.properties?.name)
);
// these are values that user will edit/select that correspond to each row in the model typing editor
const typedRows = ref<
	{
		nodeType?: string;
		typeName?: string;
		assignTo?: string[];
	}[]
>([]);

const numberNodes = computed(
	() => typedModel.value.model.states.length + typedModel.value.model.transitions.length
);
const numberTypedRows = computed(() => typedModel.value.semantics?.typing?.type_map.length ?? 0);

// TODO: don't allow user to assign a variable or transition twice
const assignToOptions = computed<{ [s: string]: string[] }[]>(() => {
	const options: { [s: string]: string[] }[] = [];
	typedRows.value.forEach(() => {
		options.push({
			Variable: typedModel.value.model.states.map((s) => s.id),
			Transition: typedModel.value.model.transitions.map((t) => t.id)
		});
	});
	return options;
});

const { getNodeTypeColor, setNodeTypeColor } = useNodeTypeColorMap();

function addTypedRow() {
	typedRows.value.push({});
}

function getLegendKeyClass(type: string) {
	if (type === 'Variable') {
		return 'legend-key-circle';
	}
	if (type === 'Transition') {
		return 'legend-key-square';
	}
	return '';
}

function getLegendKeyStyle(id: string) {
	return {
		backgroundColor: getNodeTypeColor(id)
	};
}

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
		typedModel.value = props.model;
		const data = await petriToLatex(convertAMRToACSet(props.model));
		if (data) {
			updateLatexFormula(data);
		}
	},
	{ immediate: true }
);

watch(
	() => props.typeSystem,
	() => {
		props.typeSystem?.states.forEach((s) => {
			setNodeTypeColor(s.id);
		});
		props.typeSystem?.transitions.forEach((t) => {
			setNodeTypeColor(t.id);
		});
		typedRows.value.push(
			{
				nodeType: 'Variable',
				typeName: props.typeSystem?.states[0].name
			},
			{
				nodeType: 'Transition',
				typeName: props.typeSystem?.transitions[0].properties?.name
			}
		);
	}
);

watch(
	() => newModelName.value,
	(newValue, oldValue) => {
		if (newValue !== oldValue) {
			emit('update-tab-name', newValue);
		}
	}
);

// construct TypingSemantics data structure when user updates variable/transition assignments
watch(
	typedRows,
	() => {
		const stateTypedMap: string[][] = [];
		const transitionTypedMap: string[][] = [];
		const updatedTypeSystem: TypeSystem = { states: [], transitions: [] };
		let typingSemantics: TypingSemantics;
		typedRows.value.forEach((row) =>
			row.assignTo?.forEach((parameter) => {
				if (row.typeName && row.typeName && row.nodeType) {
					if (row.nodeType === 'Variable') {
						stateTypedMap.push([parameter, row.typeName]);
					}
					if (row.nodeType === 'Transition') {
						transitionTypedMap.push([parameter, row.typeName]);
					}
				}
			})
		);

		stateTypedMap.forEach((map) => {
			// See if there is a corresponding type defined in the strata model's type system
			// If not, generate a new one
			const stateId = map[0];
			const typeId = map[1];
			let state: State | undefined | null;
			state =
				props.typeSystem?.states.find((s) => typeId === s.id) ||
				typedModel.value.semantics?.typing?.type_system.states.find((s) => typeId === s.id);
			if (state && !updatedTypeSystem.states.find((s) => s.id === state!.id)) {
				updatedTypeSystem.states.push(state);
			} else if (!updatedTypeSystem.states.find((s) => s.id === typeId)) {
				state = generateTypeState(typedModel.value, stateId, typeId);
				if (state) {
					updatedTypeSystem.states.push(state);
				}
			}
		});

		if (stateTypedMap.length > 0) {
			typingSemantics = { type_map: stateTypedMap, type_system: updatedTypeSystem };
			addTyping(typedModel.value, typingSemantics);
		}

		transitionTypedMap.forEach((map) => {
			// See if there is a corresponding type defined in the strata model's type system
			// If not, generate a new one
			const transitionId = map[0];
			const typeId = map[1];
			let transition: Transition | undefined | null;
			transition =
				props.typeSystem?.transitions.find((t) => map[1] === t.id) ||
				typedModel.value.semantics?.typing?.type_system.transitions.find((t) => typeId === t.id);
			if (transition && !updatedTypeSystem.transitions.find((t) => t.id === typeId)) {
				updatedTypeSystem.transitions.push(transition);
			} else if (!updatedTypeSystem.transitions.find((t) => t.id === typeId)) {
				transition = generateTypeTransition(typedModel.value, transitionId, typeId);
				if (transition) {
					updatedTypeSystem.transitions.push(transition);
				}
			}
		});
		if (transitionTypedMap.length > 0) {
			const typeMap: string[][] = [...stateTypedMap, ...transitionTypedMap];
			typingSemantics = { type_map: typeMap, type_system: updatedTypeSystem };
			addTyping(typedModel.value, typingSemantics);
		}
	},
	{ deep: true }
);

watch(numberTypedRows, () => {
	if (numberTypedRows.value === numberNodes.value) {
		emit('all-nodes-typed', typedModel.value);
	}
});

// Render graph whenever a new model is fetched or whenever the HTML element
//	that we render the graph to changes.
watch(
	[() => typedModel, graphElement],
	async () => {
		if (typedModel.value === null || graphElement.value === null) return;
		const graphData: IGraph<NodeData, EdgeData> = convertToIGraph(typedModel.value);

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

		// Render graph
		await renderer?.setData(graphData);
		await renderer?.render();
	},
	{ deep: true }
);
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
.legend-key-circle {
	height: 24px;
	width: 24px;
	border-radius: 12px;
}

.legend-key-square {
	height: 24px;
	width: 24px;
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
	margin: 1rem;
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
	flex: 0 1 28px;
	min-width: 0;
}

.p-inputtext,
.p-dropdown,
.p-multiselect {
	min-width: 150px;
}

.input-header {
	min-width: 150px;
}

.p-multiselect .p-multiselect-label {
	padding: 0.875rem 0.875rem;
}
</style>
