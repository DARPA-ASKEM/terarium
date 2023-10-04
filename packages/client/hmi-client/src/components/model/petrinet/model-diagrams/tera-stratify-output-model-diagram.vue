<template>
	<main>
		<TeraResizablePanel>
			<div class="splitter-container">
				<section class="graph-element">
					<Toolbar>
						<template #end>
							<Button
								@click="toggleCollapsedView"
								:label="isCollapsed ? 'Show expanded view' : 'Show collapsed view'"
								class="p-button-sm p-button-outlined toolbar-button"
							/>
						</template>
					</Toolbar>

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
					<div v-if="model" ref="graphElement" class="graph-element" />
				</section>
			</div>
		</TeraResizablePanel>
	</main>
</template>

<script setup lang="ts">
import { IGraph } from '@graph-scaffolder/index';
import { ref, computed, onMounted } from 'vue';
import {
	NodeData,
	EdgeData,
	PetrinetRenderer
} from '@/model-representation/petrinet/petrinet-renderer';
import { Model } from '@/types/Types';
import { useNodeTypeColorPalette } from '@/utils/petrinet-color-palette';
import Button from 'primevue/button';
import Toolbar from 'primevue/toolbar';
import { getGraphData, getPetrinetRenderer } from '@/model-representation/petrinet/petri-util';
import TeraResizablePanel from '@/components/widgets/tera-resizable-panel.vue';

const props = defineProps<{
	model: Model;
}>();

const graphElement = ref<HTMLDivElement | null>(null);
let renderer: PetrinetRenderer | null = null;

const stateTypes = computed(() => {
	if (props.model.semantics?.typing?.system.states) {
		return props.model.semantics.typing.system.states.map((s) => s.name);
	}
	return props.model.semantics?.typing?.system?.model.states.map((s) => s.name);
});
const transitionTypes = computed(() => {
	if (props.model.semantics?.typing?.system.transitions) {
		return props.model.semantics.typing.system.transitions.map((t) => t.properties?.name);
	}
	return props.model.semantics?.typing?.system?.model.transitions.map((t) => t.properties?.name);
});

const isCollapsed = ref(true);

const { getNodeTypeColor } = useNodeTypeColorPalette();

function getLegendKeyStyle(id: string) {
	return {
		backgroundColor: getNodeTypeColor(id)
	};
}
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

// Render graph whenever a new model is fetched or whenever the HTML element
//	that we render the graph to changes.
onMounted(async () => {
	if (props.model === null || graphElement.value === null) return;
	const graphData: IGraph<NodeData, EdgeData> = getGraphData(props.model, isCollapsed.value);

	// Create renderer
	if (!renderer) {
		renderer = getPetrinetRenderer(props.model, graphElement.value as HTMLDivElement);
	} else {
		renderer.isGraphDirty = true;
	}

	// Render graph
	await renderer?.setData(graphData);
	await renderer?.render();
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

/* Let svg dynamically resize when the sidebar opens/closes or page resizes */
:deep(.graph-element svg) {
	width: 100%;
	height: 100%;
}
.p-toolbar {
	position: absolute;
	width: 100%;
	z-index: 1;
	isolation: isolate;
	background: transparent;
	padding: 0.5rem;
}
</style>
