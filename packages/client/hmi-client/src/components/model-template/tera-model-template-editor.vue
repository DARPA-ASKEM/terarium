<template>
	<tera-infinite-canvas
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
			<aside>
				<section v-if="model?.header?.schema_name">
					<header>Model framework</header>
					<h5>{{ model.header.schema_name }}<i class="pi pi-info-circle"></i></h5>
				</section>
				<section class="template-options">
					<header>Model templates</header>
					<ul>
						<li
							v-for="(modelTemplate, index) in modelTemplatingService.modelTemplateOptions"
							:key="index"
						>
							<tera-model-template
								:model="modelTemplate"
								:is-editable="false"
								draggable="true"
								@dragstart="newModelTemplate = modelTemplate"
							/>
						</li>
					</ul>
				</section>
				<section class="trash">
					<i class="pi pi-trash"></i>
					<div>Drag items here to delete</div>
				</section>
			</aside>
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
					:model="modelTemplateEditor.models[index]"
					is-editable
					@update-name="
						(name: string) =>
							modelTemplatingService.updateCardName(modelTemplateEditor, name, card.id)
					"
					@port-selected="(portId: string) => createNewEdge(card, portId)"
					@port-mouseover="
						(event: MouseEvent, cardWidth: number) => onPortMouseover(event, card, cardWidth)
					"
					@port-mouseleave="onPortMouseleave"
					@remove="modelTemplatingService.removeCard(modelTemplateEditor, card.id)"
				/>
			</tera-canvas-item>
			<tera-canvas-item
				v-for="(junction, index) in junctions"
				:key="index"
				:style="{ width: 'fit-content', top: `${junction.y}px`, left: `${junction.x}px` }"
				@dragging="(event) => updatePosition(event, junction)"
			>
				<tera-model-junction :junction="junction" :template-cards="cards" />
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
</template>

<script setup lang="ts">
import { cloneDeep, isEqual } from 'lodash'; // debounce
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { getAStarPath } from '@graph-scaffolder/core';
import * as d3 from 'd3';
import type { Position } from '@/types/common';
import type { Model } from '@/types/Types';
import type {
	ModelTemplateEditor,
	ModelTemplateCard,
	ModelTemplateJunction
} from '@/types/model-templating';
import * as modelTemplatingService from '@/services/model-templating';
import TeraInfiniteCanvas from '../widgets/tera-infinite-canvas.vue';
import TeraModelTemplate from './tera-model-template.vue';
import TeraModelJunction from './tera-model-junction.vue';
import TeraCanvasItem from '../widgets/tera-canvas-item.vue';

defineProps<{
	model?: Model;
}>();

let currentPortPosition: Position = { x: 0, y: 0 };
let isMouseOverCanvas = false;
let canvasTransform = { x: 0, y: 0, k: 1 };
let isMouseOverPort = false;
let junctionIdForNewEdge: string | null = null;

const modelTemplateEditor = ref<ModelTemplateEditor>(
	modelTemplatingService.emptyModelTemplateEditor()
);

const cards = computed<ModelTemplateCard[]>(
	() => modelTemplateEditor.value.models.map(({ metadata }) => metadata.templateCard) ?? []
);
const junctions = computed<ModelTemplateJunction[]>(() => modelTemplateEditor.value.junctions);

const newModelTemplate = ref();
const newEdge = ref();
const isCreatingNewEdge = computed(
	() => newEdge.value && newEdge.value.points && newEdge.value.points.length === 2
);

function collisionFn(p: Position) {
	const buffer = 50;
function collisionFn(p: Position): boolean {
    const buffer = 50;
    
    return cards.value.some(({ x, y, width, height }) => {
        const withinXRange = p.x >= x - buffer && p.x <= x + width + buffer;
        const withinYRange = p.y >= y - buffer && p.y <= y + height + buffer;
        return withinXRange && withinYRange;
    });
}
		if (p.x >= checkingNode.x - buffer && p.x <= checkingNode.x + checkingNode.width + buffer) {
			if (p.y >= checkingNode.y - buffer && p.y <= checkingNode.y + checkingNode.height + buffer) {
				return true;
			}
		}
	}
	return false;
}

function interpolatePointsForCurve(a: Position, b: Position): Position[] {
	return getAStarPath(a, b, collisionFn);
}

const pathFn = d3
	.line<{ x: number; y: number }>()
	.x((d) => d.x)
	.y((d) => d.y)
	.curve(d3.curveBasis);

// Get around typescript complaints
const drawPath = (v: any) => pathFn(v) as string;

function createNewEdge(card: ModelTemplateCard, portId: string) {
	const target = { cardId: card.id, portId };

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
			modelTemplatingService.addJunction(modelTemplateEditor.value, currentPortPosition);
			junctionIdForNewEdge = junctions.value[junctions.value.length - 1].id;

			// Add a default edge as well
			modelTemplatingService.addEdge(
				modelTemplateEditor.value,
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
		modelTemplatingService.addEdge(
			modelTemplateEditor.value,
			junctionIdForNewEdge,
			target,
			currentPortPosition,
			interpolatePointsForCurve
		);
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
	modelTemplatingService.junctionCleanUp(modelTemplateEditor.value);
}

const setMouseOverCanvas = (val: boolean) => {
	isMouseOverCanvas = val;
};

function saveTransform(newTransform: { k: number; x: number; y: number }) {
	canvasTransform = newTransform;
}

function updateNewCardPosition(event) {
	newModelTemplate.value.metadata.templateCard.x =
		(event.offsetX - canvasTransform.x) / canvasTransform.k;
	newModelTemplate.value.metadata.templateCard.y =
		(event.offsetY - canvasTransform.y) / canvasTransform.k;
}

function onDrop(event) {
	updateNewCardPosition(event);
	modelTemplatingService.addCard(modelTemplateEditor.value, cloneDeep(newModelTemplate.value));
	newModelTemplate.value = null;
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

onMounted(() => {
	document.addEventListener('mousemove', mouseUpdate);
});
onUnmounted(() => {
	document.removeEventListener('mousemove', mouseUpdate);
});
</script>

<style scoped>
aside {
	width: 15rem;
	display: flex;
	flex-direction: column;
	height: 100%;
	background-color: #f4f7fa;
	border-right: 1px solid var(--surface-border-alt);
	padding: var(--gap) 0;
	gap: 0.5rem;
	overflow: hidden;
}

ul {
	list-style: none;
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
	margin-top: 0.5rem;
}

h5 {
	display: flex;
	align-items: center;
	gap: 0.25rem;
	font-weight: var(--font-weight);
}

header {
	color: var(--text-color-subdued);
	font-size: var(--font-caption);
}

.pi-info-circle {
	color: var(--text-color-subdued);
	cursor: help;
}

.template-options {
	overflow: hidden;

	& > header {
		padding: 0 var(--gap);
	}

	& > ul {
		height: 85%;
		padding: 0.25rem 0 0.25rem var(--gap-small);
		overflow-y: scroll;
	}
}

.trash {
	margin: auto 0.5rem 0 0.5rem;
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
	border: 1px dashed #9fa9b7;
	border-radius: var(--border-radius);
	background-color: #eff2f5;
	text-align: center;
	padding: 1rem;

	& > .pi-trash {
		font-size: 1.5rem;
		margin-bottom: 0.5rem;
	}
}
</style>
