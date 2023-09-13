<template>
	<main>
		<TeraResizablePanel>
			<div ref="splitterContainer" class="splitter-container">
				<section class="graph-element">
					<tera-reflexives-toolbar
						v-if="strataModel && baseModel"
						v-show="showReflexivesToolbar"
						:model-to-update="strataModel"
						:model-to-compare="baseModel"
						@model-updated="
							(value) => {
								typedModel = value;
								emit('model-updated', typedModel);
							}
						"
					/>
					<tera-model-type-legend :model="typedModel" />
					<div v-if="typedModel" ref="graphElement" class="graph-element" />
				</section>
			</div>
		</TeraResizablePanel>
	</main>
</template>

<script setup lang="ts">
import { IGraph } from '@graph-scaffolder/index';
import { watch, ref } from 'vue';
import { runDagreLayout } from '@/services/graph';
import {
	PetrinetRenderer,
	NodeData,
	EdgeData
} from '@/model-representation/petrinet/petrinet-renderer';
import { convertToIGraph } from '@/model-representation/petrinet/petrinet-service';
import { Model, TypeSystem } from '@/types/Types';
import { useNodeTypeColorPalette } from '@/utils/petrinet-color-palette';
import TeraResizablePanel from '@/components/widgets/tera-resizable-panel.vue';
import TeraReflexivesToolbar from './tera-reflexives-toolbar.vue';
import TeraModelTypeLegend from './tera-model-type-legend.vue';

const props = defineProps<{
	strataModel: Model;
	baseModel?: Model | null;
	baseModelTypeSystem?: TypeSystem;
	showReflexivesToolbar: boolean;
}>();

const emit = defineEmits(['model-updated']);

const typedModel = ref<Model>(props.strataModel); // this is the object being edited

const splitterContainer = ref<HTMLElement | null>(null);

const graphElement = ref<HTMLDivElement | null>(null);
let renderer: PetrinetRenderer | null = null;

const { setNodeTypeColor } = useNodeTypeColorPalette();

// Whenever selectedModelId changes, fetch model with that ID
watch(
	() => [props.strataModel],
	async () => {
		typedModel.value = props.strataModel;
	},
	{ immediate: true }
);

watch(
	() => props.baseModelTypeSystem,
	() => {
		const nodeIds: string[] = [];
		props.baseModelTypeSystem?.states.forEach((s) => {
			nodeIds.push(s.id);
		});
		props.baseModelTypeSystem?.transitions.forEach((t) => {
			nodeIds.push(t.id);
		});
		setNodeTypeColor(nodeIds);
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
		if (!renderer) {
			renderer = new PetrinetRenderer({
				el: graphElement.value as HTMLDivElement,
				useAStarRouting: false,
				useStableZoomPan: true,
				runLayout: runDagreLayout,
				dragSelector: 'no-drag'
			});
		} else {
			renderer.isGraphDirty = true;
		}

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
