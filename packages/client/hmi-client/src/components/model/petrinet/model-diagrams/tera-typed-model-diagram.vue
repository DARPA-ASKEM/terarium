<template>
	<main>
		<TeraResizablePanel>
			<div class="splitter-container">
				<section class="graph-element">
					<section v-if="showTypingToolbar" class="typingSection">
						<div class="typing-row">
							<div>COLOR</div>
							<div class="input-header">NODE TYPE</div>
							<div class="input-header">NAME OF TYPE</div>
							<div class="input-header">ASSIGN TO</div>
							<div>
								<div class="empty-spacer" :style="{ width: `28px` }"></div>
							</div>
						</div>
						<div class="typing-row" v-for="(row, index) in typedRows" :key="index">
							<!-- legend key -->
							<div>
								<div
									:class="getLegendKeyClass(row.nodeType ?? '')"
									:style="
										getLegendKeyStyle(
											row.assignTo && row.nodeType && row.typeName ? row.typeName : ''
										)
									"
								/>
							</div>
							<div>
								<!-- node type -->
								<Dropdown :options="Object.keys(assignToOptions[index])" v-model="row.nodeType" />
							</div>
							<div>
								<!-- name of type -->
								<InputText
									:model-value="row.typeName"
									@update:model-value="(newValue) => setTypeNameBuffer(newValue, index)"
									@change="updateRowTypeName(index)"
								/>
							</div>
							<div>
								<!-- assign to -->
								<MultiSelect
									placeholder="Select nodes"
									:options="assignToOptions[index][row.nodeType ?? '']"
									v-model="row.assignTo"
									:maxSelectedLabels="1"
								/>
							</div>
							<!-- cancel row  -->
							<div>
								<Button icon="pi pi-times" text rounded @click="cancelTypedRow(index)" />
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
					<tera-reflexives-toolbar
						v-if="model && strataModel"
						v-show="showReflexivesToolbar"
						:model-to-update="model"
						:model-to-compare="strataModel"
						@model-updated="
							(value) => {
								typedModel = value;
								emit('model-updated', value);
							}
						"
					/>
					<tera-model-type-legend :model="typedModel" />
					<Toolbar>
						<template #end>
							<Button
								v-if="getStratificationType(model)"
								@click="toggleCollapsedView"
								:label="isCollapsed ? 'Show expanded view' : 'Show collapsed view'"
								class="p-button-sm p-button-outlined toolbar-button"
							/>
						</template>
					</Toolbar>
					<div v-if="typedModel" ref="graphElement" class="graph-element" />
				</section>
			</div>
		</TeraResizablePanel>
	</main>
</template>

<script setup lang="ts">
import { IGraph } from '@graph-scaffolder/index';
import { watch, ref, computed } from 'vue';
import {
	PetrinetRenderer,
	NodeData,
	EdgeData
} from '@/model-representation/petrinet/petrinet-renderer';
import { addTyping, getStratificationType } from '@/model-representation/petrinet/petrinet-service';
import Button from 'primevue/button';
import { Model, State, Transition, TypeSystem, TypingSemantics } from '@/types/Types';
import { useNodeTypeColorPalette } from '@/utils/petrinet-color-palette';
import Dropdown from 'primevue/dropdown';
import MultiSelect from 'primevue/multiselect';
import InputText from 'primevue/inputtext';
import {
	generateTypeTransition,
	generateTypeState
} from '@/services/models/stratification-service';
import Toolbar from 'primevue/toolbar';
import { getGraphData, getPetrinetRenderer } from '@/model-representation/petrinet/petri-util';
import TeraResizablePanel from '@/components/widgets/tera-resizable-panel.vue';
import TeraReflexivesToolbar from './tera-reflexives-toolbar.vue';
import TeraModelTypeLegend from './tera-model-type-legend.vue';

const emit = defineEmits(['model-updated', 'all-nodes-typed', 'not-all-nodes-typed']);

const props = defineProps<{
	model: Model;
	strataModel: Model | null;
	showTypingToolbar: boolean;
	typeSystem?: TypeSystem;
	showReflexivesToolbar: boolean;
}>();

const graphElement = ref<HTMLDivElement | null>(null);
let renderer: PetrinetRenderer | null = null;

const typedModel = ref<Model>(props.model);
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
const numberTypedNodes = computed(() => typedModel.value.semantics?.typing?.map.length ?? 0);
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
const allNodesTyped = computed(() => numberTypedNodes.value === numberNodes.value);

function addTypedRow() {
	typedRows.value.push({});
}
function cancelTypedRow(rowIndex: number) {
	typedRows.value.splice(rowIndex, 1);
}
// 'typedRows.typeName' is assigned the value of 'typeNameBuffer' when the user focuses away from the associated InputText
let typeNameBuffer: string[] = [];
function setTypeNameBuffer(newValue, row) {
	typeNameBuffer[row] = newValue;
}
function updateRowTypeName(rowIndex: number) {
	typedRows.value[rowIndex].typeName = typeNameBuffer[rowIndex];
}

const { getNodeTypeColor, setNodeTypeColor } = useNodeTypeColorPalette();
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
	if (!id) {
		return {
			backgroundColor: 'var(--petri-nodeFill)'
		};
	}
	return {
		backgroundColor: getNodeTypeColor(id)
	};
}
function setNodeColors() {
	const nodeIds: string[] = [];
	props.typeSystem?.states.forEach((s) => {
		nodeIds.push(s.id);
	});
	props.typeSystem?.transitions.forEach((t) => {
		nodeIds.push(t.id);
	});
	props.model.semantics?.typing?.system.model.states.forEach((s) => {
		nodeIds.push(s.id);
	});
	props.model.semantics?.typing?.system.model.transitions.forEach((t) => {
		nodeIds.push(t.id);
	});
	setNodeTypeColor(nodeIds);
}

const isCollapsed = ref(true);
async function toggleCollapsedView() {
	isCollapsed.value = !isCollapsed.value;
	const graphData: IGraph<NodeData, EdgeData> = getGraphData(props.model, isCollapsed.value);
	// Render graph
	if (renderer) {
		renderer.isGraphDirty = true;
		await renderer.setData(graphData);
		await renderer.render();
	}
}

watch(
	() => props.model,
	async () => {
		typedModel.value = props.model;
		typedRows.value = [];
		populateTypingRows();
	},
	{ immediate: true }
);

watch(
	() => props.showTypingToolbar,
	async () => {
		populateTypingRows();
	},
	{ immediate: true }
);

function populateTypingRows() {
	if (props.showTypingToolbar) {
		setNodeColors();
		if (
			typedModel.value.semantics?.typing?.map &&
			typedModel.value.semantics.typing.map.length > 0
		) {
			// pre-populate 'typedRows' if 'typedModel' already has typing
			const typedRowsToPopulate: {
				nodeType: string;

				typeName: string;
				assignTo: string[];
			}[] = [];
			const typedRowsTypeNames: Set<string> = new Set();
			typedModel.value.semantics.typing.map.forEach((m) => {
				const nodeId = m[0];
				const typeId = m[1];
				const state = typedModel.value.model.states.find((s) => s.id === nodeId);
				const transition = typedModel.value.model.transitions.find((t) => t.id === nodeId);
				const typeState = typedModel.value.semantics!.typing!.system.model.states.find(
					(s) => s.id === typeId
				);
				const typeTransition = typedModel.value.semantics!.typing!.system.model.transitions.find(
					(t) => t.id === typeId
				);
				const node = state || transition;
				const nodeType = state ? 'Variable' : 'Transition';
				const typeName = typeState ? typeState.id : typeTransition.properties?.name;
				if (!typedRowsTypeNames.has(typeName)) {
					typedRowsTypeNames.add(typeName);
					typedRowsToPopulate.push({
						nodeType,
						typeName,
						assignTo: [node.id]
					});
				} else {
					const assignTo = typedRowsToPopulate.find((row) => row.typeName === typeName)?.assignTo;
					assignTo?.push(node.id);
				}
			});
			typedRows.value = typedRowsToPopulate;
		} else if (typedRows.value.length === 0) {
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
		typeNameBuffer = typedRows.value.map((r) => r.typeName ?? '');
	}
}

watch(
	() => [props.typeSystem, props.showTypingToolbar],
	() => {
		populateTypingRows();
	},
	{ immediate: true }
);

// construct TypingSemantics data structure when user updates variable/transition assignments
watch(
	typedRows,
	() => {
		const stateTypedMap: string[][] = [];
		const transitionTypedMap: string[][] = [];
		const updatedTypeSystem: TypeSystem = { states: [], transitions: [] };
		let typingSemantics: TypingSemantics;
		typedRows.value.forEach(
			(row) =>
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
				typedModel.value.semantics?.typing?.system.model.states.find((s) => typeId === s.id);
			if (state && !updatedTypeSystem.states.find((s) => s.id === state!.id)) {
				updatedTypeSystem.states.push(state);
			} else if (!updatedTypeSystem.states.find((s) => s.id === typeId)) {
				state = generateTypeState(typedModel.value, stateId, typeId);
				if (state) {
					updatedTypeSystem.states.push(state);
				}
			}
		});

		// eslint-disable-next-line @typescript-eslint/naming-convention
		const { name, description, schema, model_version } = typedModel.value.header;
		typingSemantics = {
			map: stateTypedMap,
			system: {
				name,
				description,
				schema,
				model_version,
				model: updatedTypeSystem
			}
		};
		addTyping(typedModel.value, typingSemantics);

		transitionTypedMap.forEach((map) => {
			// See if there is a corresponding type defined in the strata model's type system
			// If not, generate a new one
			const transitionId = map[0];
			const typeId = map[1];
			let transition: Transition | undefined | null;
			transition =
				props.typeSystem?.transitions.find((t) => map[1] === t.id) ||
				typedModel.value.semantics?.typing?.system.model.transitions.find((t) => typeId === t.id);
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
			// eslint-disable-next-line @typescript-eslint/naming-convention
			const typeMap: string[][] = [...stateTypedMap, ...transitionTypedMap];
			typingSemantics = {
				map: typeMap,
				system: {
					name,
					description,
					schema,
					model_version,
					model: updatedTypeSystem
				}
			};
			addTyping(typedModel.value, typingSemantics);
		}
	},
	{ deep: true }
);

watch(
	numberTypedNodes,
	() => {
		if (allNodesTyped.value) {
			emit('all-nodes-typed', typedModel.value);
		} else {
			emit('not-all-nodes-typed', numberNodes.value - numberTypedNodes.value);
		}
	},
	{ immediate: true }
);

// Render graph whenever a model is updated or whenever the HTML element
//	that we render the graph to changes.
watch(
	[() => typedModel, graphElement],
	async () => {
		if (typedModel.value === null || graphElement.value === null) return;
		const graphData: IGraph<NodeData, EdgeData> = getGraphData(props.model, isCollapsed.value);

		// Create renderer
		renderer = getPetrinetRenderer(props.model, graphElement.value as HTMLDivElement);

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

/* Let svg dynamically resize when the sidebar opens/closes or page resizes */
:deep(.graph-element svg) {
	width: 100%;
	height: 100%;
}

.typingSection {
	padding-bottom: 1rem;
	border-bottom: 1px solid var(--surface-border-light);
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
	width: 100%;
}

.p-multiselect-label-container {
	min-width: 150px;
	width: 100%;
}

.input-header {
	min-width: 150px;
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
</style>
