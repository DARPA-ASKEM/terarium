<template>
	<section ref="cardRef" class="card-container" :style="cardStyle">
		<section class="card">
			<div class="draggable"><i class="pi pi-pause" /></div>
			<main>
				<header>Template title</header>
				<section>Diagram/Equations</section>
			</main>
			<Button icon="pi pi-ellipsis-v" rounded text />
		</section>
		<ul>
			<li v-for="(variable, index) in fakeVariables" :key="index">
				{{ variable }}
			</li>
		</ul>
	</section>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import Button from 'primevue/button';

const props = defineProps<{
	card: { x: number; y: number };
}>();

const emit = defineEmits(['dragging']);

const fakeVariables = ['X', 'Y', 'p'];
let isDragging = false;
let tempX = 0;
let tempY = 0;

const cardRef = ref();

const cardStyle = computed(() => ({
	minWidth: '106px',
	top: `${props.card.y}px`,
	left: `${props.card.x}px`
}));

// const setDrag = (val: boolean) => {
// 	isDragging = val;
// };

const startDrag = (evt: MouseEvent) => {
	tempX = evt.x;
	tempY = evt.y;
	isDragging = true;
};

const drag = (evt: MouseEvent) => {
	if (!isDragging) return;

	const dx = evt.x - tempX;
	const dy = evt.y - tempY;

	emit('dragging', { x: dx, y: dy });

	tempX = evt.x;
	tempY = evt.y;
};

const stopDrag = (/* evt: MouseEvent */) => {
	tempX = 0;
	tempY = 0;
	isDragging = false;
};

onMounted(() => {
	if (!cardRef.value) return;
	cardRef.value.addEventListener('mousedown', startDrag);
	document.addEventListener('mousemove', drag);
	cardRef.value.addEventListener('mouseup', stopDrag);
});

onBeforeUnmount(() => {
	if (cardRef.value) {
		cardRef.value.removeEventListener('mousedown', startDrag);
		document.removeEventListener('mousemove', drag);
		cardRef.value.removeEventListener('mouseup', stopDrag);
	}
});
</script>

<style scoped>
.card-container {
	display: flex;
	font-size: var(--font-caption);
	user-select: none;
}

.card {
	display: flex;
	background-color: var(--surface-section);
	border-radius: var(--border-radius-medium);
	outline: 1px solid var(--surface-border-alt);
	min-width: 12rem;
	position: relative;
	box-shadow:
		0px 1px 3px 0px rgba(0, 0, 0, 0.08),
		0px 1px 2px 0px rgba(0, 0, 0, 0.04);

	& > main {
		width: 100%;
		margin: 0.5rem;
		display: flex;
		flex-direction: column;
		gap: 1rem;

		& > header {
			font-weight: var(--font-weight);
		}

		& > * {
			margin: 0 auto;
		}
	}

	& > .p-button {
		display: none;
		position: absolute;
		bottom: 0;
		right: 0;
	}
}

ul {
	list-style: none;
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
	margin: 0.5rem 0;

	& > li {
		background-color: var(--surface-section);
		border: 1px solid var(--surface-border-alt);
		border-top-right-radius: var(--border-radius);
		border-bottom-right-radius: var(--border-radius);
		padding: 0.15rem 0.25rem;
		color: var(--text-color-subdued);
		box-shadow:
			0px 1px 3px 0px rgba(0, 0, 0, 0.08),
			0px 1px 2px 0px rgba(0, 0, 0, 0.04);
		/* Font should be "Latin Modern Math" */
		font-family: serif;
		font-style: italic;
	}
}

.draggable {
	width: 0.75rem;
	border-top-left-radius: var(--border-radius-medium);
	border-bottom-left-radius: var(--border-radius-medium);
	background-color: var(--surface-highlight);
	display: flex;
	align-items: center;

	& > .pi {
		font-size: 0.75rem;
		color: var(--text-color-subdued);
	}
}
</style>

<style>
/* When a card is placed in the data layer of the infinite canvas 
(eg. ports shouldn't look selectable if card is in the sidebar) */
.data-layer .card-container {
	position: absolute;

	& .card:hover > .p-button {
		display: block;
	}

	& > ul > li:hover {
		background-color: var(--surface-highlight);
		cursor: pointer;
	}
}
</style>
