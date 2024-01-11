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
				v-for="(template, index) in modelTemplates"
				:key="index"
				:style="{
					width: 'fit-content',
					top: `${template.metadata.templateCard.y}px`,
					left: `${template.metadata.templateCard.x}px`
				}"
				@dragging="(event) => updatePosition(template.metadata.templateCard, event)"
			>
				<tera-model-template
					:model="template"
					is-editable
					@update-name="(name: string) => updateName(name, index)"
					@port-selected="createNewEdge(template.metadata.templateCard)"
					@port-mouseover="
						(event: MouseEvent, cardWidth: number) =>
							onPortMouseover(event, template.metadata.templateCard, cardWidth)
					"
					@port-mouseleave="onPortMouseleave"
				/>
			</tera-canvas-item>
		</template>
		<template #background>
			<path
				v-if="newEdge?.points"
				:d="drawPath(interpolatePointsForCurve(newEdge.points[0], newEdge.points[1]))"
				stroke="#1B8073"
				stroke-width="2"
				marker-start="url(#circle)"
				marker-end="url(#arrow)"
				fill="none"
			/>
			<path
				v-for="(edge, index) in edges"
				:d="drawPath(interpolatePointsForCurve(edge.points[0], edge.points[1]))"
				stroke="#1B8073"
				stroke-width="2"
				marker-start="url(#circle)"
				:key="index"
				fill="none"
			/>
		</template>
	</tera-infinite-canvas>
</template>

<script setup lang="ts">
import { cloneDeep } from 'lodash';
import { ref, computed, onMounted, onUnmounted } from 'vue';
import * as d3 from 'd3';
import { Position } from '@/types/workflow'; // temp
import { Model } from '@/types/Types';
import TeraInfiniteCanvas from '../widgets/tera-infinite-canvas.vue';
import TeraModelTemplate from './tera-model-template.vue';
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

interface ModelTemplateEdge {
	id: number;
	source: number;
	target: number;
	points: Position[];
}

const modelTemplateOptions = [
	naturalConversion,
	naturalProduction,
	naturalDegredation,
	controlledConversion,
	controlledProduction,
	controlledDegredation,
	observable
].map((modelTemplate) => {
	modelTemplate.metadata.templateCard = {
		id: -1,
		name: modelTemplate.header.name,
		x: 0,
		y: 0
	};
	return modelTemplate;
});

let currentPortPosition: Position = { x: 0, y: 0 };
let isMouseOverCanvas = false;
let canvasTransform = { x: 0, y: 0, k: 1 };
let isMouseOverPort = false;

const modelTemplates = ref<any[]>([]); // ([{ id: 1, name: 'Template name', x: 300, y: 40 }]);
const edges = ref<ModelTemplateEdge[]>([]);

const newModelTemplate = ref();
const newEdge = ref();
const isCreatingNewEdge = computed(
	() => newEdge.value && newEdge.value.points && newEdge.value.points.length === 2
);

function interpolatePointsForCurve(a: Position, b: Position): Position[] {
	const controlXOffset = 0;
	return [a, { x: a.x + controlXOffset, y: a.y }, { x: b.x - controlXOffset, y: b.y }, b];
}

const pathFn = d3
	.line<{ x: number; y: number }>()
	.x((d) => d.x)
	.y((d) => d.y);
// .curve(d3.curveBasis);

// Get around typescript complaints
const drawPath = (v: any) => pathFn(v) as string;

function updateName(name: string, index: number) {
	modelTemplates.value[index].metadata.templateCard.name = name;
}

function createNewEdge(card: ModelTemplate) {
	if (!isCreatingNewEdge.value) {
		newEdge.value = {
			id: -1,
			source: card.id,
			points: [
				{ x: currentPortPosition.x, y: currentPortPosition.y },
				{ x: currentPortPosition.x, y: currentPortPosition.y }
			]
		};
	} else {
		edges.value.push({
			id: edges.value.length + 1,
			points: newEdge.value.points,
			source: newEdge.value.source,
			target: card.id
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

const updatePosition = (card: ModelTemplate, { x, y }) => {
	if (!isMouseOverCanvas) return;

	// Update node position
	card.x += x / canvasTransform.k;
	card.y += y / canvasTransform.k;

	// Update edge positions
	edges.value.forEach((edge) => {
		if (edge.source === card.id) {
			edge.points[0].x += x / canvasTransform.k;
			edge.points[0].y += y / canvasTransform.k;
		}
		if (edge.target === card.id) {
			edge.points[edge.points.length - 1].x += x / canvasTransform.k;
			edge.points[edge.points.length - 1].y += y / canvasTransform.k;
		}
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
	/* max-height: 85%;
	overflow: hidden;

	& > header {
		padding: 0 var(--gap);
	}

	& > ul {
		padding: 0.25rem 0 0 var(--gap-small);
		overflow-y: scroll;
	} */
}

.trash {
	margin-top: auto;
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
	border: 1px dashed #9fa9b7;
	border-radius: var(--border-radius);
	background-color: #eff2f5;
	text-align: center;
	padding: 1rem 0;

	& > .pi-trash {
		font-size: 1.5rem;
		margin-bottom: 0.5rem;
	}
}
</style>
