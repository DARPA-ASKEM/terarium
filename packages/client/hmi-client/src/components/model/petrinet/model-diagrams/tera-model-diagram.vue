<template>
	<tera-tooltip :custom-position="hoveredTransitionPosition" :show-tooltip="!isEmpty(hoveredTransitionId)">
		<main>
			<TeraResizablePanel v-if="!featureConfig?.isPreview" class="diagram-container">
				<section class="graph-element">
					<Toolbar>
						<template #start>
							<span>
								<Button @click="resetZoom" label="Reset zoom" size="small" severity="secondary" outlined />
								<span class="how-to-zoom">
									<kbd>Ctrl</kbd>
									+
									<kbd>scroll</kbd>&nbsp;to zoom</span
								>
							</span>
						</template>
						<template #center> </template>
						<template #end>
							<span>
								<SelectButton
									v-if="model && isStratified"
									class="p-button-sm"
									:model-value="stratifiedView"
									@change="$event.value && toggleCollapsedView($event.value)"
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
					<template v-if="model">
						<div class="graph-container">
							<div ref="graphElement" class="graph-element" />
							<ul class="legend" v-if="!isEmpty(graphLegendLabels)">
								<li v-for="(label, index) in graphLegendLabels" :key="index">
									<div class="legend-circle" :style="`background: ${graphLegendColors[index]}`" />
									{{ label }}
								</li>
							</ul>
						</div>
					</template>
				</section>
			</TeraResizablePanel>
			<div v-else-if="model" ref="graphElement" class="graph-element preview" />
			<tera-stratified-matrix-modal
				v-if="selectedTransitionId"
				:id="selectedTransitionId"
				:mmt="mmt"
				:mmt-params="mmtParams"
				:stratified-matrix-type="StratifiedMatrix.Rates"
				@close-modal="selectedTransitionId = ''"
			/>
		</main>
		<template #tooltip-content v-if="isStratified && !isEmpty(hoveredTransitionId)">
			<div ref="tooltipContentRef">
				<tera-stratified-matrix-preview
					:mmt="mmt"
					:mmt-params="mmtParams"
					:id="hoveredTransitionId"
					:stratified-matrix-type="StratifiedMatrix.Rates"
				/>
			</div>
		</template>
	</tera-tooltip>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';
import { ref, watch, computed, nextTick } from 'vue';
import Toolbar from 'primevue/toolbar';
import Button from 'primevue/button';
import SelectButton from 'primevue/selectbutton';
import { PetrinetRenderer } from '@/model-representation/petrinet/petrinet-renderer';
import { getModelType, getMMT } from '@/services/model';
import type { Model } from '@/types/Types';
import TeraResizablePanel from '@/components/widgets/tera-resizable-panel.vue';
import TeraTooltip from '@/components/widgets/tera-tooltip.vue';
import { NestedPetrinetRenderer } from '@/model-representation/petrinet/nested-petrinet-renderer';
import { StratifiedMatrix } from '@/types/Model';
import { AMRSchemaNames } from '@/types/common';
import { MiraModel, MiraTemplateParams, ObservableSummary } from '@/model-representation/mira/mira-common';
import { isStratifiedModel, emptyMiraModel, convertToIGraph } from '@/model-representation/mira/mira';
import { getModelRenderer } from '@/model-representation/service';
import { NodeType } from '@/services/graph';
import type { FeatureConfig } from '@/types/common';
import TeraStratifiedMatrixModal from '../model-configurations/tera-stratified-matrix-modal.vue';
import TeraStratifiedMatrixPreview from '../model-configurations/tera-stratified-matrix-preview.vue';

const props = defineProps<{
	model: Model;
	featureConfig?: FeatureConfig;
}>();

const graphElement = ref<HTMLDivElement | null>(null);
const graphLegendLabels = ref<string[]>([]);
const graphLegendColors = ref<string[]>([]);
const selectedTransitionId = ref('');
const modelType = computed(() => getModelType(props.model));
const mmt = ref<MiraModel>(emptyMiraModel());
const mmtParams = ref<MiraTemplateParams>({});

let observableSummary: ObservableSummary = {};
const hoveredTransitionId = ref('');
const hoveredTransitionPosition = ref({ x: 0, y: 0 });
const tooltipContentRef = ref();

enum StratifiedView {
	Expanded = 'Expanded',
	Collapsed = 'Collapsed'
}

const stratifiedView = ref(StratifiedView.Collapsed);
const stratifiedViewOptions = ref([{ value: StratifiedView.Expanded }, { value: StratifiedView.Collapsed }]);

const isStratified = computed(() => isStratifiedModel(mmt.value));

let renderer: PetrinetRenderer | NestedPetrinetRenderer | null = null;

const resetZoom = async () => {
	renderer?.setToDefaultZoom();
};

async function renderGraph() {
	renderer = getModelRenderer(
		mmt.value,
		graphElement.value as HTMLDivElement,
		stratifiedView.value === StratifiedView.Collapsed
	);
	if (renderer.constructor === NestedPetrinetRenderer && renderer.dims?.length) {
		graphLegendLabels.value = renderer.dims;
		graphLegendColors.value = renderer.depthColorList;
	} else {
		graphLegendLabels.value = [];
		graphLegendColors.value = [];
	}

	renderer.on('node-click', (_eventName, _event, selection) => {
		const { id, data } = selection.datum();
		if (data.type === NodeType.Transition && data.isStratified) {
			selectedTransitionId.value = id;
		}
	});

	renderer.on('node-mouse-enter', async (_eventName, _event, selection) => {
		const { id, data } = selection.datum();

		if (data.type === NodeType.Transition && data.isStratified) {
			hoveredTransitionId.value = id;

			const diagramBounds = renderer?.svgEl?.getBoundingClientRect();
			const transitionMatrixBounds = selection.node().getBoundingClientRect();

			await nextTick(); // Wait for tooltip to render to get its dimensions
			const tooltipHeight = tooltipContentRef.value.parentElement.clientHeight;
			const tooltipWidth = tooltipContentRef.value.parentElement.clientWidth;

			if (diagramBounds && transitionMatrixBounds && tooltipHeight && tooltipWidth) {
				const transitionMatrixX = transitionMatrixBounds.left - diagramBounds.left;
				const transitionMatrixY = transitionMatrixBounds.top - diagramBounds.top;
				const transitionMatrixHeight = selection.datum().height;
				const transitionMatrixWidth = selection.datum().width;

				// Shift tooltip to the top center of the transition matrix
				const x = transitionMatrixX - (tooltipWidth + transitionMatrixWidth / 2) / 2 + transitionMatrixBounds.width / 2;
				const y = transitionMatrixY - tooltipHeight - transitionMatrixHeight / 2;

				hoveredTransitionPosition.value = { x, y };
			}
		}
	});

	renderer.on('node-mouse-leave', (_eventName, _event, selection) => {
		const { data } = selection.datum();
		if (data.type === NodeType.Transition && data.isStratified) hoveredTransitionId.value = '';
	});

	// Render graph
	const graphData = convertToIGraph(
		mmt.value,
		observableSummary,
		isStratified.value && stratifiedView.value === StratifiedView.Collapsed
	);

	if (renderer) {
		renderer.isGraphDirty = true;
		await renderer.setData(graphData);
		await renderer.render();
	}
}

async function toggleCollapsedView(view: StratifiedView) {
	stratifiedView.value = view;
	await renderGraph();
}

watch(
	() => [props.model.model, props.model?.semantics, graphElement.value],
	async () => {
		if (modelType.value === AMRSchemaNames.DECAPODES || graphElement.value === null) return;
		// FIXME: inefficient, do not constant call API in watch
		const response: any = await getMMT(props.model);
		mmt.value = response.mmt;
		mmtParams.value = response.template_params;
		observableSummary = response.observable_summary;
		await renderGraph();
	},
	{ immediate: true, deep: true }
);
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
	pointer-events: none;
}

.p-toolbar {
	position: absolute;
	width: 100%;
	z-index: 1;
	isolation: isolate;
	background: transparent;
	padding: 0.5rem;
	pointer-events: none;
}

.p-toolbar:deep(> div > span) {
	gap: 0.25rem;
	display: flex;
	pointer-events: all;
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
	cursor: grab;

	&:active {
		cursor: grabbing;
	}
}

:deep(.graph-element .p-button) {
	&,
	&:hover {
		background-color: var(--surface-secondary);
	}
}

.how-to-zoom {
	display: flex;
	align-items: center;
	font-size: var(--font-caption);
	background-color: var(--surface-transparent);
	backdrop-filter: blur(4px);
	padding: 0 var(--gap-small);
	border-radius: var(--border-radius);
	pointer-events: none;
	user-select: none;
}

kbd {
	background-color: var(--surface-section);
	border: 1px solid var(--surface-border);
	border-radius: var(--border-radius);
	padding: 2px var(--gap-xsmall);
	font-size: var(--font-tiny);
	font-weight: var(--font-weight-semibold);
	color: var(--text-color-subdued);
}

ul.legend {
	background-color: var(--surface-transparent);
	backdrop-filter: blur(4px);
	padding: var(--gap-xsmall) var(--gap-small);
	border-radius: var(--border-radius);
	position: absolute;
	bottom: 0;
	left: 0;
	display: flex;
	margin: var(--gap-small);
	margin-bottom: var(--gap);
	gap: var(--gap);
	pointer-events: none;

	& > li {
		display: flex;
		align-items: center;
		gap: var(--gap-xsmall);
	}
}

.legend-circle {
	display: inline-block;
	height: 1rem;
	width: 1rem;
	border-radius: 50%;
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
