<template>
	<tera-infinite-canvas
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
				<section>
					<header>Model templates</header>
					<ul>
						<li v-for="(_, index) in 5" :key="index">
							<tera-model-template-card :card="newCard" draggable="true" />
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
				:key="index"
				:style="{
					width: 'fit-content',
					top: `${card.y}px`,
					left: `${card.x}px`
				}"
				@dragging="(event) => updatePosition(card, event)"
			>
				<tera-model-template-card :card="card" />
			</tera-canvas-item>
		</template>
	</tera-infinite-canvas>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { cloneDeep } from 'lodash';
import { Model } from '@/types/Types';
import TeraInfiniteCanvas from '../widgets/tera-infinite-canvas.vue';
import TeraModelTemplateCard from './tera-model-template-card.vue';
import TeraCanvasItem from '../widgets/tera-canvas-item.vue';

defineProps<{
	model?: Model;
}>();

interface ModelTemplateCard {
	// Position on canvas
	x: number;
	y: number;
}

const newCard: ModelTemplateCard = { x: 0, y: 0 };
let isMouseOverCanvas: boolean = false;
let canvasTransform = { x: 0, y: 0, k: 1 };

const cards = ref<ModelTemplateCard[]>([{ x: 300, y: 40 }]);

const setMouseOverCanvas = (val: boolean) => {
	isMouseOverCanvas = val;
};

function saveTransform(newTransform: { k: number; x: number; y: number }) {
	canvasTransform = newTransform;
}

function updateNewCardPosition(event) {
	newCard.x = (event.offsetX - canvasTransform.x) / canvasTransform.k;
	newCard.y = (event.offsetY - canvasTransform.y) / canvasTransform.k;
}

function onDrop(event) {
	updateNewCardPosition(event);
	cards.value.push(cloneDeep(newCard));
}

const updatePosition = (card: ModelTemplateCard, { x, y }) => {
	if (!isMouseOverCanvas) return;
	card.x += x / canvasTransform.k;
	card.y += y / canvasTransform.k;
};
</script>

<style scoped>
aside {
	width: 15rem;
	display: flex;
	flex-direction: column;
	height: 100%;
	background-color: #f4f7fa;
	border-right: 1px solid var(--surface-border-alt);
	padding: 1rem;
	gap: 0.5rem;
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
