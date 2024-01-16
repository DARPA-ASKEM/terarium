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
						<li v-for="(modelTemplate, index) in modelTemplateOptions" :key="index">
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
				v-for="(modelTemplate, index) in modelTemplates"
				:key="index"
				:style="{
					width: 'fit-content',
					top: `${modelTemplate.metadata.templateCard.y}px`,
					left: `${modelTemplate.metadata.templateCard.x}px`
				}"
				@dragging="(event) => updatePosition(modelTemplate.metadata.templateCard, event)"
			>
				<tera-model-template
					:model="modelTemplate"
					is-editable
					@update-name="(name: string) => updateName(name, index)"
					@port-selected="
						(portId: string) => createNewEdge(modelTemplate.metadata.templateCard, portId)
					"
					@port-mouseover="
						(event: MouseEvent, cardWidth: number) =>
							onPortMouseover(event, modelTemplate.metadata.templateCard, cardWidth)
					"
					@port-mouseleave="onPortMouseleave"
				/>
			</tera-canvas-item>
			<tera-canvas-item
				v-for="(junction, index) in junctions"
				:key="index"
				:style="{ width: 'fit-content', top: `${junction.y}px`, left: `${junction.x}px` }"
			>
				<tera-model-junction :junction="junction" />
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
					:d="drawPath(interpolatePointsForCurve(edge.points[0], edge.points[1]))"
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
import { cloneDeep, isEqual } from 'lodash';
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { getAStarPath } from '@graph-scaffolder/core';
import * as d3 from 'd3';
import { Position } from '@/types/workflow'; // temp
import type { Model } from '@/types/Types';
import TeraInfiniteCanvas from '../widgets/tera-infinite-canvas.vue';
import TeraModelTemplate from './tera-model-template.vue';
import TeraModelJunction from './tera-model-junction.vue';
import TeraCanvasItem from '../widgets/tera-canvas-item.vue';
import naturalConversion from './templates/natural-conversion.json';
import naturalProduction from './templates/natural-production.json';
import naturalDegredation from './templates/natural-degradation.json';
import controlledConversion from './templates/controlled-conversion.json';
import controlledProduction from './templates/controlled-production.json';
import controlledDegredation from './templates/controlled-degradation.json';
import observable from './templates/observable.json';

defineProps<{
	model?: Model;
}>();

interface ModelTemplate {
	id: number;
	name: string;
	x: number;
	y: number;
}

// Edge sources are always junctions so you'd reference the junction id for that
interface ModelTemplateEdge {
	target: {
		cardName: string;
		cardId: number;
		portId: string;
	};
	points: Position[];
}

interface ModelTemplateJunction {
	id: number;
	x: number;
	y: number;
	edges: ModelTemplateEdge[];
}

const modelTemplateOptions = [
	naturalConversion,
	naturalProduction,
	naturalDegredation,
	controlledConversion,
	controlledProduction,
	controlledDegredation,
	observable
].map((modelTemplate: any) => {
	// TODO: Add templateCard attribute to Model later
	modelTemplate.metadata.templateCard = {
		id: -1,
		name: modelTemplate.header.name,
		x: 0,
		y: 0
		// ports: [...modelTemplate.model.states, ...modelTemplate.semantics.ode.parameters]
	};
	return modelTemplate;
});

let currentPortPosition: Position = { x: 0, y: 0 };
let isMouseOverCanvas = false;
let canvasTransform = { x: 0, y: 0, k: 1 };
let isMouseOverPort = false;
let junctionIdForNewEdge: number | null = null;

const modelTemplates = ref<any[]>([]); // ([{ id: 1, name: 'Template name', x: 300, y: 40 }]);
const junctions = ref<ModelTemplateJunction[]>([]);

const newModelTemplate = ref();
const newEdge = ref();
const isCreatingNewEdge = computed(
	() => newEdge.value && newEdge.value.points && newEdge.value.points.length === 2
);
// const newJunctionPos = computed(() => ({ x: 0, y: 0 + junctions.value.length * 100 }));

function collisionFn(p: Position) {
	const buffer = 50;
	for (let i = 0; i < modelTemplates.value.length; i++) {
		const checkingNode = modelTemplates.value[i].metadata.templateCard;
		// FIXME: Thi is  a hack to get around hierarhical geometries, will need to
		// relax this guard.
		// if (node.nodes && node.nodes.length > 0) continue;
		if (p.x >= checkingNode.x - buffer && p.x <= checkingNode.x + checkingNode.width + buffer) {
			if (p.y >= checkingNode.y - buffer && p.y <= checkingNode.y + checkingNode.height + buffer) {
				return true;
			}
		}
	}
	return false;
}

function interpolatePointsForCurve(a: Position, b: Position): Position[] {
	// const controlXOffset = 0;
	return getAStarPath(a, b, collisionFn);
	// return [a, { x: a.x + controlXOffset, y: a.y }, { x: b.x - controlXOffset, y: b.y }, b];
}

const pathFn = d3
	.line<{ x: number; y: number }>()
	.x((d) => d.x)
	.y((d) => d.y)
	.curve(d3.curveBasis);

// Get around typescript complaints
const drawPath = (v: any) => pathFn(v) as string;

function updateName(name: string, index: number) {
	modelTemplates.value[index].metadata.templateCard.name = name;
}

function createNewEdge(card: ModelTemplate, portId: string) {
	const target = { cardName: card.name, cardId: card.id, portId };

	// Handles the edge that goes from port to junction
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
			// Draws edge from a port to a newly created junction
			junctionIdForNewEdge = junctions.value.length + 1;
			junctions.value.push({
				id: junctionIdForNewEdge,
				x: currentPortPosition.x + 500,
				y: currentPortPosition.y - 10,
				edges: [
					{
						target,
						points: [
							{ x: currentPortPosition.x + 510, y: currentPortPosition.y },
							{ x: currentPortPosition.x, y: currentPortPosition.y }
						]
					}
				]
			});
		}

		const index = junctions.value.findIndex(({ id }) => id === junctionIdForNewEdge);
		newEdge.value = {
			target,
			points: [
				{ x: junctions.value[index].x + 10, y: junctions.value[index].y + 10 },
				{ x: currentPortPosition.x, y: currentPortPosition.y }
			]
		};
	}
	// Handles the edge going from junction to port
	else if (
		junctionIdForNewEdge &&
		target.cardId !== newEdge.value.target.cardId // Prevents ports from the same card connecting
	) {
		console.log(junctionIdForNewEdge);
		const index = junctions.value.findIndex(({ id }) => id === junctionIdForNewEdge);
		junctions.value[index].edges.push({
			target,
			points: [
				{ x: junctions.value[index].x + 10, y: junctions.value[index].y + 10 },
				{ x: currentPortPosition.x, y: currentPortPosition.y }
			]
		});

		cancelNewEdge();
	}
}

function onPortMouseover(event: MouseEvent, card: ModelTemplate, cardWidth: number) {
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

	// Removes junction that doesn't connect to anything
	junctions.value = junctions.value.filter(({ edges }) => edges.length > 1);
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

	newModelTemplate.value.metadata.templateCard.id = modelTemplates.value.length + 1;
	modelTemplates.value.push(cloneDeep(newModelTemplate.value));
	newModelTemplate.value = null;
}

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

const updatePosition = ({ x, y }, card: ModelTemplate) => {
	if (!isMouseOverCanvas) return;

	// Update card position
	card.x += x / canvasTransform.k;
	card.y += y / canvasTransform.k;

	// Update edge positions
	junctions.value.forEach(({ edges }) => {
		edges.forEach((edge) => {
			if (edge.target.cardId === card.id) {
				edge.points[edge.points.length - 1].x += x / canvasTransform.k;
				edge.points[edge.points.length - 1].y += y / canvasTransform.k;
			}
		});
	});
};

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
