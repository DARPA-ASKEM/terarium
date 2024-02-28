<template>
	<main v-if="modelType === AMRSchemaNames.PETRINET">
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

	<!--REGNET and STOCKFLOW models use beaker to generate an image-->
	<main v-else-if="modelType === AMRSchemaNames.REGNET || modelType === AMRSchemaNames.STOCKFLOW">
		<div v-if="!isGeneratingModelPreview">
			<img :src="templatePreview" alt="" :style="{ 'max-height': isPreview ? '180px' : '400px' }" />
		</div>
		<tera-progress-spinner v-else is-centered> Generating model preview... </tera-progress-spinner>
	</main>
</template>

<script setup lang="ts">
import { watch, ref, onMounted, onUnmounted, computed } from 'vue';
import Toolbar from 'primevue/toolbar';
import Button from 'primevue/button';
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
import type { Model, ModelConfiguration } from '@/types/Types';
import TeraResizablePanel from '@/components/widgets/tera-resizable-panel.vue';
import { NestedPetrinetRenderer } from '@/model-representation/petrinet/nested-petrinet-renderer';
import { StratifiedMatrix } from '@/types/Model';
import SelectButton from 'primevue/selectbutton';
import { AMRSchemaNames } from '@/types/common';
import { KernelSessionManager } from '@/services/jupyter';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import TeraStratifiedMatrixModal from '../model-configurations/tera-stratified-matrix-modal.vue';
import TeraModelTypeLegend from './tera-model-type-legend.vue';

const props = defineProps<{
	model: Model;
	isEditable: boolean;
	modelConfiguration?: ModelConfiguration;
	isPreview?: boolean;
}>();

const emit = defineEmits(['update-model', 'update-configuration']);

const isCollapsed = ref(true);
const graphElement = ref<HTMLDivElement | null>(null);
const splitterContainer = ref<HTMLElement | null>(null);
const layout = ref<'horizontal' | 'vertical' | undefined>('horizontal');
const switchWidthPercent = ref<number>(50); // switch model layout when the size of the model window is < 50%
const graphLegendLabels = ref<string[]>([]);
const graphLegendColors = ref<string[]>([]);
const openValueConfig = ref(false);
const selectedTransitionId = ref('');
const modelType = computed(
	() => props.model?.header?.schema_name?.toLowerCase() ?? AMRSchemaNames.PETRINET
);
const templatePreview = ref('');
const isGeneratingModelPreview = ref(false);

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

let renderer: PetrinetRenderer | NestedPetrinetRenderer | null = null;

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

// Render graph whenever a new model is fetched or whenever the HTML element
// that we render the graph to changes.
// Consider just watching the model
watch(
	[() => props.model, graphElement],
	async () => {
		if (modelType.value === AMRSchemaNames.PETRINET) {
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

			// Render graph
			await renderer?.setData(graphData);
			await renderer?.render();
		}
	},
	{ deep: true }
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
const handleResize = () => updateLayout();

onMounted(() => {
	window.addEventListener('resize', handleResize);
	handleResize();
	if (modelType.value === AMRSchemaNames.REGNET || modelType.value === AMRSchemaNames.STOCKFLOW) {
		generateTemplatePreview();
	}
});

onUnmounted(() => {
	window.removeEventListener('resize', handleResize);
});

// Create a preview image based on MMT
const generateTemplatePreview = async () => {
	if (!props.model) return;
	try {
		const kernelManager = new KernelSessionManager();
		isGeneratingModelPreview.value = true;
		await kernelManager.init('beaker_kernel', 'Beaker Kernel', {
			context: 'mira_model',
			language: 'python3',
			context_info: {
				id: props.model.id
			}
		});

		kernelManager
			.sendMessage('reset_request', {})
			.register('reset_response', () => null) // noop
			.register('model_preview', (data) => {
				templatePreview.value = `data:image/png;base64, ${data?.content?.['image/png']}`;
				kernelManager.shutdown();
				isGeneratingModelPreview.value = false;
			});
	} catch (err) {
		console.error(err);
		isGeneratingModelPreview.value = false;
	}
};
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
