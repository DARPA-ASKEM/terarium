<template>
	<section class="template-editor-wrapper">
		<aside>
			<section v-if="model?.header?.schema_name">
				<header>Model framework</header>
				<h5>{{ model.header.schema_name }}<i class="pi pi-info-circle"></i></h5>
			</section>
			<section class="template-options">
				<header>Model templates</header>
				<ul>
					<li v-for="(card, index) in modelTemplatingService.modelTemplateCardOptions" :key="index">
						<tera-model-template
							:model="card.model"
							:is-editable="false"
							is-decomposed
							:style="isDecomposedLoading && { cursor: 'wait' }"
							:draggable="!isDecomposedLoading"
							@dragstart="sidebarTemplateToAdd = cloneDeep(card)"
						/>
					</li>
				</ul>
			</section>
			<section class="trash">
				<i class="pi pi-trash"></i>
				<div>Drag items here to delete</div>
			</section>
		</aside>
		<tera-infinite-canvas
			:is-disabled="isDecomposedLoading"
			@click="onCanvasClick"
			@save-transform="saveTransform"
			@mouseenter="setMouseOverCanvas(true)"
			@mouseleave="setMouseOverCanvas(false)"
			@drop="onDrop"
			@dragover.prevent
			@dragenter.prevent
			@focus="() => {}"
			@blur="() => {}"
		>
			<template #foreground>
				<!--FIXME: This container holding the toggles overlaps the top of the canvas so the drag area is slightly cutoff-->
				<section class="button-container">
					<!-- TODO: There will be a Diagram/Equation toggle here. There may be plans to make a component for this specific
						toggle though since in some designs it is used outside of tera-model-diagram and others are inside -->
					<SelectButton
						:model-value="currentModelFormat"
						@change="if ($event.value) onEditorFormatSwitch($event.value);"
						:options="modelFormatOptions"
						:disabled="isDecomposedLoading"
					/>
					<span class="btn-group">
						<Button label="Reset" outlined severity="secondary" @click="reset" />
						<Button label="Save" @click="$emit('save-new-model-output', model)" />
					</span>
				</section>
				<tera-progress-spinner v-if="isDecomposedLoading" class="spinner" is-centered :font-size="2">
					Please wait...
				</tera-progress-spinner>
			</template>
			<template #data>
				<tera-canvas-item
					v-for="(card, index) in cards"
					:key="card.id"
					:style="{
						width: 'fit-content',
						top: `${card.y}px`,
						left: `${card.x}px`
					}"
					@dragging="(event) => updatePosition(event, card)"
				>
					<tera-model-template
						:model="currentCanvas.cards[index].model"
						is-editable
						:is-decomposed="currentModelFormat === EditorFormat.Decomposed"
						:id="card.id"
						@update-name="
							(name: string) =>
								modelTemplatingService.updateDecomposedTemplateNameInKernel(
									kernelManager,
									currentCanvas.cards[index].model,
									flattenedCanvas.cards[0].model,
									name,
									outputCode,
									syncWithMiraModel
								)
						"
						@port-selected="(portId: string) => createNewEdge(card.id, portId)"
						@port-mouseover="(event: MouseEvent, cardWidth: number) => onPortMouseover(event, card, cardWidth)"
						@port-mouseleave="onPortMouseleave"
						@remove="
							() =>
								modelTemplatingService.removeTemplateInKernel(
									kernelManager,
									currentCanvas,
									card.id,
									outputCode,
									syncWithMiraModel
								)
						"
					/>
				</tera-canvas-item>
				<tera-canvas-item
					v-for="(junction, index) in junctions"
					:key="index"
					:style="{ width: 'fit-content', top: `${junction.y}px`, left: `${junction.x}px` }"
					@dragging="(event) => updatePosition(event, junction)"
				>
					<tera-model-junction :junction="junction" :cards="cards" />
				</tera-canvas-item>
			</template>
			<template #background>
				<path
					v-if="newEdge?.points"
					:d="drawPath(interpolatePointsForCurve(newEdge.points[0], newEdge.points[1]))"
					stroke="var(--text-color-subdued)"
					stroke-width="2"
					fill="none"
				/>
				<template v-for="{ edges } in junctions">
					<path
						v-for="(edge, index) in edges"
						:d="drawPath(edge.points)"
						stroke="var(--text-color-subdued)"
						stroke-width="2"
						:key="index"
						fill="none"
					/>
				</template>
			</template>
		</tera-infinite-canvas>
	</section>
</template>

<script setup lang="ts">
import { isEmpty, isEqual, cloneDeep } from 'lodash'; // debounce
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import { getAStarPath } from '@graph-scaffolder/core';
import * as d3 from 'd3';
import type { Position } from '@/types/common';
import type { Model } from '@/types/Types';
import type {
	ModelTemplateCanvas,
	ModelTemplateCard,
	ModelTemplateJunction,
	OffsetValues
} from '@/types/model-templating';
import * as modelTemplatingService from '@/services/model-templating';
import SelectButton from 'primevue/selectbutton';
import Button from 'primevue/button';
import { KernelSessionManager } from '@/services/jupyter';
import TeraInfiniteCanvas from '../widgets/tera-infinite-canvas.vue';
import TeraModelTemplate from './tera-model-template.vue';
import TeraModelJunction from './tera-model-junction.vue';
import TeraCanvasItem from '../widgets/tera-canvas-item.vue';
import TeraProgressSpinner from '../widgets/tera-progress-spinner.vue';

const props = defineProps<{
	model: Model;
	kernelManager: KernelSessionManager;
}>();

const emit = defineEmits(['output-code', 'sync-with-mira-model', 'save-new-model-output', 'reset']);

function outputCode(data: any) {
	emit('output-code', data);
}

function syncWithMiraModel(data: any) {
	emit('sync-with-mira-model', data);
}

enum EditorFormat {
	Decomposed = 'Decomposed',
	Flattened = 'Flattened'
}

let currentPortPosition: Position = { x: 0, y: 0 };
let isMouseOverCanvas = false;
let canvasTransform = { x: 0, y: 0, k: 1 };
let isMouseOverPort = false;
let junctionIdForNewEdge: string | null = null;
const decomposedPortOffsetValues = new Map<string, OffsetValues>();

const decomposedCanvas = ref<ModelTemplateCanvas>(modelTemplatingService.initializeCanvas());
const flattenedCanvas = ref<ModelTemplateCanvas>(modelTemplatingService.initializeCanvas());
const modelFormatOptions = ref([EditorFormat.Decomposed, EditorFormat.Flattened]);
const currentModelFormat = ref(EditorFormat.Decomposed);

const currentCanvas = computed(() =>
	currentModelFormat.value === EditorFormat.Decomposed ? decomposedCanvas.value : flattenedCanvas.value
);
const cards = computed<ModelTemplateCard[]>(() => currentCanvas.value.cards);
const junctions = computed<ModelTemplateJunction[]>(() => currentCanvas.value.junctions);

const sidebarTemplateToAdd = ref<ModelTemplateCard | null>(null);
const newEdge = ref();

const isDecomposedLoading = computed(() => props.model && isEmpty(decomposedCanvas.value.cards));
const isCreatingNewEdge = computed(() => newEdge.value && newEdge.value.points && newEdge.value.points.length === 2);

function collisionFn(p: Position): boolean {
	const buffer = 50;
	return cards.value.some(({ x, y }) => {
		const withinXRange = p.x >= x - buffer && p.x <= x + buffer;
		const withinYRange = p.y >= y - buffer && p.y <= y + buffer;
		return withinXRange && withinYRange;
	});
}

function interpolatePointsForCurve(a: Position, b: Position): Position[] {
	return getAStarPath(a, b, { collider: collisionFn });
}

const pathFn = d3
	.line<{ x: number; y: number }>()
	.x((d) => d.x)
	.y((d) => d.y)
	.curve(d3.curveBasis);

// Get around typescript complaints
const drawPath = (v: any) => pathFn(v) as string;

function createNewEdge(cardId: string, portId: string) {
	const target = { cardId, portId };

	if (!isCreatingNewEdge.value) {
		// Find the junction that we want to draw from
		junctions.value.forEach(({ edges, id }) => {
			for (let i = 0; i < edges.length; i++) {
				if (isEqual(target, edges[i].target)) {
					junctionIdForNewEdge = id;
					return;
				}
			}
		});

		// If a junction isn't found that means we have to create one
		if (!junctionIdForNewEdge) {
			modelTemplatingService.addJunction(currentCanvas.value, currentPortPosition);
			junctionIdForNewEdge = junctions.value[junctions.value.length - 1].id;

			// Add a default edge as well
			modelTemplatingService.addEdgeInView(
				currentCanvas.value,
				junctionIdForNewEdge,
				target,
				currentPortPosition,
				interpolatePointsForCurve
			);
		}

		// Creates the potential edge that the user is drawing
		const junctionToDrawFrom = junctions.value.find(({ id }) => id === junctionIdForNewEdge);
		if (junctionToDrawFrom) {
			newEdge.value = {
				target,
				points: [
					{ x: junctionToDrawFrom.x + 10, y: junctionToDrawFrom.y + 10 },
					{ x: currentPortPosition.x, y: currentPortPosition.y }
				]
			};
		}
	}
	// Creates the edge that the user drew
	else if (
		junctionIdForNewEdge &&
		target.cardId !== newEdge.value.target.cardId // Prevents connecting to the same card
	) {
		if (currentModelFormat.value === EditorFormat.Decomposed) {
			modelTemplatingService.addEdgeInKernel(
				props.kernelManager,
				currentCanvas.value,
				junctionIdForNewEdge,
				target,
				newEdge.value.target,
				currentPortPosition,
				outputCode,
				syncWithMiraModel,
				interpolatePointsForCurve
			);
		} else {
			modelTemplatingService.addEdgeInView(
				currentCanvas.value,
				junctionIdForNewEdge,
				target,
				currentPortPosition,
				interpolatePointsForCurve
			);
			// Once the second edge is drawn, reflect changes in decomposed view - once done, everything in the flattened view will be "merged"
			modelTemplatingService.reflectFlattenedEditInDecomposedView(
				props.kernelManager,
				flattenedCanvas.value,
				decomposedCanvas.value,
				decomposedPortOffsetValues,
				outputCode,
				syncWithMiraModel,
				interpolatePointsForCurve
			);
		}
		cancelNewEdge();
	}
}

function onPortMouseover(event: MouseEvent, card: ModelTemplateCard, cardWidth: number) {
	const el = event.target as HTMLElement;
	const portElement = (el.querySelector('.port') as HTMLElement) ?? el;
	const nodePosition: Position = { x: card.x, y: card.y };
	const totalOffsetY = portElement.offsetTop + portElement.offsetHeight / 2;

	currentPortPosition = {
		x: nodePosition.x + cardWidth + portElement.offsetWidth / 2,
		y: nodePosition.y + totalOffsetY
	};

	isMouseOverPort = true;
}

function onPortMouseleave() {
	isMouseOverPort = false;
}

function onCanvasClick() {
	if (isCreatingNewEdge.value) {
		cancelNewEdge();
	}
}

function cancelNewEdge() {
	newEdge.value = undefined;
	junctionIdForNewEdge = null;
	modelTemplatingService.junctionCleanUp(currentCanvas.value);
}

const setMouseOverCanvas = (val: boolean) => {
	isMouseOverCanvas = val;
};

function saveTransform(newTransform: { k: number; x: number; y: number }) {
	canvasTransform = newTransform;
}

function updateNewCardPosition(event) {
	if (!sidebarTemplateToAdd.value) return;
	sidebarTemplateToAdd.value.x = (event.offsetX - canvasTransform.x) / canvasTransform.k;
	sidebarTemplateToAdd.value.y = (event.offsetY - canvasTransform.y) / canvasTransform.k;
}

function onDrop(event) {
	if (!sidebarTemplateToAdd.value) return;

	updateNewCardPosition(event);

	if (currentModelFormat.value === EditorFormat.Decomposed) {
		modelTemplatingService.addDecomposedTemplateInKernel(
			props.kernelManager,
			decomposedCanvas.value,
			sidebarTemplateToAdd.value,
			outputCode,
			syncWithMiraModel
		);
	}
	// Add decomposed template to the flattened view
	else {
		// If we are in the flattened view just add it in the UI - it will be added in kernel once linked to the flattened model
		// Cards that aren't linked in the flattened view will be removed once the view switches to decomposed
		modelTemplatingService.prepareDecomposedTemplateAddition(flattenedCanvas.value, sidebarTemplateToAdd.value);
		modelTemplatingService.addTemplateInView(flattenedCanvas.value, sidebarTemplateToAdd.value);
	}

	sidebarTemplateToAdd.value = null;
}

const updatePosition = (
	{ x, y },
	node: any // node can be a ModelTemplate or a ModelTemplateJunction
) => {
	if (!isMouseOverCanvas) return;

	const isJunction = node.edges !== undefined;

	// Update node position
	node.x += x / canvasTransform.k;
	node.y += y / canvasTransform.k;

	// Update edge positions
	junctions.value.forEach(({ edges, id }) => {
		edges.forEach((edge) => {
			const lastPointIndex = edge.points.length - 1;

			// On junction move
			if (isJunction && id === node.id) {
				edge.points[0].x += x / canvasTransform.k;
				edge.points[0].y += y / canvasTransform.k;

				edge.points = interpolatePointsForCurve(edge.points[0], edge.points[lastPointIndex]);
			}
			// On card move
			if (!isJunction && edge.target.cardId === node.id) {
				edge.points[lastPointIndex].x += x / canvasTransform.k;
				edge.points[lastPointIndex].y += y / canvasTransform.k;
				edge.points = interpolatePointsForCurve(edge.points[0], edge.points[lastPointIndex]);
			}
		});
	});
};

// const debouncedUpdatePosition = debounce(updatePosition, 5); // FIXME: Stays on dragged stayed when let go

let prevX = 0;
let prevY = 0;
function mouseUpdate(event: MouseEvent) {
	if (isCreatingNewEdge.value) {
		const pointIndex = 1;
		if (isMouseOverPort) {
			newEdge.value!.points[pointIndex].x = currentPortPosition.x;
			newEdge.value!.points[pointIndex].y = currentPortPosition.y;
		} else {
			const dx = event.x - prevX;
			const dy = event.y - prevY;
			newEdge.value!.points[pointIndex].x += dx / canvasTransform.k;
			newEdge.value!.points[pointIndex].y += dy / canvasTransform.k;
		}
	}
	prevX = event.x;
	prevY = event.y;
}

function onEditorFormatSwitch(newFormat: EditorFormat) {
	currentModelFormat.value = newFormat;
	if (newFormat === EditorFormat.Decomposed)
		renderFlattenedCanvas(); // Removes unlinked decomposed templates
	else {
		// When switching to the flattened view, we save the decomposed port positions
		// so that edges can be drawn correctly when relecting flattened edits to the decomposed view
		decomposedPortOffsetValues.clear();
		const decomposedPortElements = document.getElementsByClassName('port selectable') as HTMLCollectionOf<HTMLElement>;

		Array.from(decomposedPortElements).forEach((element) =>
			decomposedPortOffsetValues.set(element.id, modelTemplatingService.getElementOffsetValues(element))
		);
	}
}

function reset() {
	emit('reset');
	renderDecomposedCanvas();
}

// Rendering functions populate the canvases with all their model templates, can be used to refresh the view
function renderFlattenedCanvas() {
	flattenedCanvas.value = modelTemplatingService.initializeCanvas();
	modelTemplatingService.updateFlattenedTemplateInView(flattenedCanvas.value, props.model);
}

function renderDecomposedCanvas() {
	decomposedCanvas.value = modelTemplatingService.initializeCanvas();
	modelTemplatingService.flattenedToDecomposedInKernel(
		props.kernelManager,
		decomposedCanvas.value,
		interpolatePointsForCurve
	);
}

// Triggered after syncWithMiraModel() in parent
watch(
	() => props.model,
	(newModel, oldModel) => {
		renderFlattenedCanvas();
		// If we are working with a new model id then we must decompose it since it's made of different templates (on output switch)
		if (oldModel?.id !== newModel?.id) renderDecomposedCanvas();
	}
);

onMounted(() => {
	document.addEventListener('mousemove', mouseUpdate);
	renderFlattenedCanvas();
	renderDecomposedCanvas();
});

onUnmounted(() => {
	document.removeEventListener('mousemove', mouseUpdate);
});
</script>

<style scoped>
.template-editor-wrapper {
	display: flex;
	height: 100%;
	overflow: hidden;
	position: relative;
}

:deep(.foreground-layer) {
	pointer-events: none;
	height: 100%;
}

.button-container {
	display: flex;
	justify-content: space-between;
	padding: var(--gap-2);
	pointer-events: none;
	& .pi-spin {
		color: var(--text-color-subdued);
	}
	& .btn-group {
		display: flex;
		gap: var(--gap-2);
	}
	& > * {
		pointer-events: auto;
	}
}

.spinner {
	position: absolute;
	top: 50%;
	left: 50%;
	transform: translate(-50%, -50%);
}

aside {
	min-width: 15rem;
	display: flex;
	flex-direction: column;
	background-color: #f4f7fa;
	border-right: 1px solid var(--surface-border-light);
	padding: var(--gap-4);
	gap: var(--gap-2);
	overflow: hidden;
	z-index: 1;
}

ul {
	list-style: none;
	display: flex;
	flex-direction: column;
	gap: var(--gap-2);
	margin-top: var(--gap-2);
}

h5 {
	display: flex;
	align-items: center;
	gap: var(--gap-1);
	font-weight: var(--font-weight);
}

header {
	color: var(--text-color-subdued);
	font-size: var(--font-caption);
}

h5,
header {
	padding: 0 var(--gap-4);
}

.pi-info-circle {
	color: var(--text-color-subdued);
	cursor: help;
}

.template-options {
	overflow: scroll;
	min-width: 15rem;

	& > ul {
		height: 85%;
		padding: var(--gap-1) 0 var(--gap-1) var(--gap-2);
	}
}

.trash {
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
	border: 1px dashed #9fa9b7;
	border-radius: var(--border-radius);
	background-color: var(--surface-a);
	text-align: center;
	padding: 1rem;

	& > .pi-trash {
		font-size: 1.5rem;
		margin-bottom: var(--gap-2);
	}
}
</style>
