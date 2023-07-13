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
							<section v-if="showReflexivesToolbar">
								<div
									class="reflexives-row"
									v-for="(option, stateType) in reflexiveOptions"
									:key="stateType"
								>
									<div v-for="(transition, i) in option" :key="i">
										<div>
											{{ `Which groups of type '${stateType}' are allowed to '${transition.id}'` }}
										</div>
										<MultiSelect
											class="p-inputtext-sm"
											placeholder="Select"
											:options="reflexiveNodeOptions[stateType]"
											:model-value="statesToAddReflexives[stateType]"
											@update:model-value="newValue => updateStatesToAddReflexives(newValue, stateType as string, transition.id)"
										/>
									</div>
								</div>
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
	addReflexives,
	addTyping
} from '@/model-representation/petrinet/petrinet-service';
import Splitter from 'primevue/splitter';
import SplitterPanel from 'primevue/splitterpanel';
import { Model, Transition, TypeSystem, TypingSemantics } from '@/types/Types';
import { useNodeTypeColorMap } from '@/utils/color-schemes';
import MultiSelect from 'primevue/multiselect';
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
	strataModel: Model;
	baseModel: Model | null;
	baseModelTypeSystem?: TypeSystem;
	showReflexivesToolbar: boolean;
}>();

const typedModel = ref<Model>(props.strataModel);

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
	props.strataModel.semantics?.typing?.type_system?.states.map((s) => s.name)
);
const transitionTypes = computed(() =>
	props.strataModel.semantics?.typing?.type_system?.transitions.map((t) => t.properties?.name)
);

const { getNodeTypeColor, setNodeTypeColor } = useNodeTypeColorMap();

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

const excludedTransitionTypes: Transition[] = [];
const reflexiveOptions = ref<{ [stateType: string]: Transition[] }>({});
const reflexiveNodeOptions = computed<{ [id: string]: string[] }>(() => {
	const options: { [id: string]: string[] } = {};
	Object.keys(reflexiveOptions.value).forEach((key) => {
		options[key] =
			props.strataModel.semantics?.typing?.type_map.filter((m) => m[1] === key).map((m) => m[0]) ??
			[];
	});
	return options;
});
const statesToAddReflexives = ref<{ [id: string]: string[] }>({});
const typeIdToTransitionIdMap = computed<{ [id: string]: string }>(() => {
	const map: { [id: string]: string } = {};
	props.baseModel?.semantics?.typing?.type_system.transitions.forEach((type) => {
		const transitionId =
			props.baseModel?.semantics?.typing?.type_map.find((m) => m[1] === type.id)?.[0] ?? '';
		map[type.id] = transitionId;
	});
	return map;
});

function updateStatesToAddReflexives(
	newValue: string[],
	typeOfState: string,
	typeOfTransition: string
) {
	statesToAddReflexives.value[typeOfState] = newValue;
	newValue.forEach((stateId) => {
		const newTransitionId = `${typeIdToTransitionIdMap.value[typeOfTransition]}${stateId}${stateId}`;
		addReflexives(typedModel.value, stateId, newTransitionId);
		const updatedTypeMap = typedModel.value.semantics?.typing?.type_map ?? [];
		const transition = props.baseModel?.semantics?.typing?.type_system.transitions.find(
			(t) => t.id === typeOfTransition
		);
		const updatedTypeSystem = typedModel.value.semantics?.typing?.type_system;
		if (updatedTypeMap && transition && updatedTypeSystem) {
			updatedTypeMap.push([newTransitionId, typeOfTransition]);
			updatedTypeSystem.transitions.push(transition);
			const updatedTyping: TypingSemantics = {
				type_map: updatedTypeMap,
				type_system: updatedTypeSystem
			};
			addTyping(typedModel.value, updatedTyping);
		}
	});
}

watch(
	[() => props.showReflexivesToolbar, () => props.baseModelTypeSystem],
	() => {
		if (props.showReflexivesToolbar && props.baseModelTypeSystem) {
			const strataTypeTransitionIds =
				props.strataModel.semantics?.typing?.type_system.transitions.map((t) => t.id);
			const baseModelTypeTransitionIds = props.baseModelTypeSystem?.transitions.map((t) => t.id);
			if (strataTypeTransitionIds && baseModelTypeTransitionIds) {
				const excludedIds = baseModelTypeTransitionIds.filter(
					(id) => !strataTypeTransitionIds.includes(id)
				);
				const excludedTransitions: Transition[] = props.baseModelTypeSystem?.transitions.filter(
					(t) => excludedIds.includes(t.id)
				);
				excludedTransitionTypes.push(excludedTransitions[0]);
			}

			props.strataModel.model.states.forEach((state) => {
				// get type of state for each state in strata model
				const type: string =
					props.strataModel.semantics?.typing?.type_map.find((m) => m[0] === state.id)?.[1] ?? '';
				// for each excluded transition type, check if inputs or ouputs have the type of this state
				const allowedTransitionsForState: Transition[] = excludedTransitionTypes.filter(
					(excluded) => excluded.input.includes(type) || excluded.output.includes(type)
				);
				if (!reflexiveOptions.value[type]) {
					reflexiveOptions.value[type] = allowedTransitionsForState;
				}
			});
		}
	},
	{ immediate: true }
);

// Whenever selectedModelId changes, fetch model with that ID
watch(
	() => [props.strataModel],
	async () => {
		updateLatexFormula('');
		typedModel.value = props.strataModel;
		const data = await petriToLatex(convertAMRToACSet(props.strataModel));
		if (data) {
			updateLatexFormula(data);
		}
	},
	{ immediate: true }
);

watch(
	() => props.baseModelTypeSystem,
	() => {
		props.baseModelTypeSystem?.states.forEach((s) => {
			setNodeTypeColor(s.id);
		});
		props.baseModelTypeSystem?.transitions.forEach((t) => {
			setNodeTypeColor(t.id);
		});
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

.reflexives-row {
	display: flex;
	flex-direction: column;
}

.reflexives-row > div {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.reflexives-row > div > div {
	margin: 1rem;
}

.p-inputtext,
.p-multiselect {
	min-width: 150px;
}

.p-multiselect .p-multiselect-label {
	padding: 0.875rem 0.875rem;
}
</style>
