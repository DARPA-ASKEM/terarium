<template>
	<main>
		<TeraResizablePanel v-if="!isPreview" class="diagram-container">
			<section class="graph-element">
				<Toolbar>
					<template #start>
						<span>
							<Button
								@click="resetZoom"
								label="Reset zoom"
								class="p-button-sm p-button-outlined"
								severity="secondary"
							/>
						</span>
					</template>
					<template #center> </template>
					<template #end>
						<span>
							<SelectButton
								v-if="model && isStratifiedModel(mmt)"
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
			</section>
		</TeraResizablePanel>
		<div
			v-else-if="model"
			ref="graphElement"
			class="graph-element preview"
			:style="!isEditable && { pointerEvents: 'none' }"
		/>
		<Teleport to="body">
			<tera-stratified-matrix-modal
				v-if="openValueConfig && modelConfiguration"
				:id="selectedTransitionId"
				:mmt="mmt"
				:mmt-params="mmtParams"
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
import { watch, ref, onMounted, onUnmounted, computed } from 'vue';
import Toolbar from 'primevue/toolbar';
import Button from 'primevue/button';
import SelectButton from 'primevue/selectbutton';
import { PetrinetRenderer, NodeType } from '@/model-representation/petrinet/petrinet-renderer';
import { getModelType, getMMT } from '@/services/model';
import type { Model, ModelConfiguration } from '@/types/Types';
import TeraResizablePanel from '@/components/widgets/tera-resizable-panel.vue';

import { NestedPetrinetRenderer } from '@/model-representation/petrinet/nested-petrinet-renderer';
import { StratifiedMatrix } from '@/types/Model';
import { AMRSchemaNames } from '@/types/common';
import { MiraModel, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import {
	isStratifiedModel,
	emptyMiraModel,
	convertToIGraph,
	collapseTemplates,
	rawTemplatesSummary
} from '@/model-representation/mira/mira';
import { getModelRenderer } from '@/model-representation/service';
import TeraModelTypeLegend from './tera-model-type-legend.vue';
import TeraStratifiedMatrixModal from '../model-configurations/tera-stratified-matrix-modal.vue';

const props = defineProps<{
	model: Model;
	isEditable: boolean;
	modelConfiguration?: ModelConfiguration;
	isPreview?: boolean;
}>();

const emit = defineEmits(['update-configuration']);

const isCollapsed = ref(true);
const graphElement = ref<HTMLDivElement | null>(null);
const graphLegendLabels = ref<string[]>([]);
const graphLegendColors = ref<string[]>([]);
const openValueConfig = ref(false);
const selectedTransitionId = ref('');
const modelType = computed(() => getModelType(props.model));
const mmt = ref<MiraModel>(emptyMiraModel());
const mmtParams = ref<MiraTemplateParams>({});

enum StratifiedView {
	Expanded = 'Expanded',
	Collapsed = 'Collapsed'
}

const stratifiedView = ref(StratifiedView.Collapsed);
const stratifiedViewOptions = ref([
	{ value: StratifiedView.Expanded },
	{ value: StratifiedView.Collapsed }
]);

let renderer: PetrinetRenderer | NestedPetrinetRenderer | null = null;

const resetZoom = async () => {
	renderer?.setToDefaultZoom();
};

async function renderGraph() {
	const { templatesSummary } = collapseTemplates(mmt.value);
	const rawTemplates = rawTemplatesSummary(mmt.value);

	renderer = getModelRenderer(mmt.value, graphElement.value as HTMLDivElement, isCollapsed.value);
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

	// Render graph
	const graphData =
		isCollapsed.value === true && isStratifiedModel(mmt.value)
			? convertToIGraph(templatesSummary)
			: convertToIGraph(rawTemplates);

	if (renderer) {
		renderer.isGraphDirty = true;
		await renderer.setData(graphData);
		await renderer.render();
	}
}

async function toggleCollapsedView() {
	isCollapsed.value = !isCollapsed.value;
	renderGraph();
}

// Render graph whenever a new model is fetched or whenever the HTML element
// that we render the graph to changes.
// Consider just watching the model
watch(
	[() => props.model, graphElement],
	async () => {
		if (modelType.value === AMRSchemaNames.DECAPODES) return;
		if (graphElement.value === null) return;

		// FIXME: inefficient, do not constant call API in watch
		const response: any = await getMMT(props.model);
		mmt.value = response.mmt;
		mmtParams.value = response.template_params;
		await renderGraph();
	},
	{ deep: true }
);

onMounted(async () => {
	const response: any = await getMMT(props.model);
	mmt.value = response.mmt;
	mmtParams.value = response.template_params;
});

onUnmounted(() => {});
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

/* Let svg dynamically resize when the sidebar opens/closes or page resizes */
:deep(.graph-element svg) {
	width: 100%;
	height: 100%;
	background: var(--gray-50) !important;
	border: none !important;
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
	background-color: var(--surface-glass);
	backdrop-filter: blur(5px);
	border-radius: 0.5rem;
	padding: 0.5rem;
	max-width: 95%;
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
