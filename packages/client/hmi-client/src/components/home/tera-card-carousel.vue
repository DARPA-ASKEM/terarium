<template>
	<main>
		<section class="carousel" ref="carouselRef">
			<div v-if="activeCarouselPage > 1" class="chevron-left" @click="scroll($event, 'left')">
				<i class="pi pi-chevron-left" />
			</div>
			<div
				v-if="activeCarouselPage !== amountOfCardPages"
				class="chevron-right"
				@click="scroll($event, 'right')"
			>
				<i class="pi pi-chevron-right" />
			</div>
			<ul v-if="isLoading">
				<li v-for="i in amountOfSkeletonCards" :key="i">
					<slot name="skeleton-card" />
				</li>
			</ul>
			<ul v-else ref="cardListRef">
				<slot name="list-of-cards" />
			</ul>
		</section>
		<section v-if="amountOfCardPages" class="page-indicators">
			<!--page starts counting at 1 because we are looping through a number-->
			<Button
				v-for="page in amountOfCardPages"
				:key="page"
				icon="pi pi-circle-fill"
				:active="activeCarouselPage === page"
				class="page-indicator p-button-icon-only p-button-text p-button-rounded"
				@click="onCarouselPaginate(page)"
			/>
		</section>
	</main>
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted, onBeforeUnmount } from 'vue';
import Button from 'primevue/button';

const props = defineProps({
	isLoading: {
		type: Boolean,
		default: false
	},
	amountOfCards: {
		type: Number,
		default: 0
	},
	amountOfSkeletonCards: {
		type: Number,
		default: 3
	}
});

const cardWidth = 238; // 17rem
const rightGapWidth = 21; // 1.5rem

const carouselRef = ref();
const cardListRef = ref();
const carouselWidth = ref(0);
const activeCarouselPage = ref(1);

const amountOfCardsToMove = computed(() =>
	carouselWidth.value ? Math.floor(carouselWidth.value / (cardWidth + rightGapWidth)) : 3
);

const scrollIncriment = computed(() => (cardWidth + rightGapWidth) * amountOfCardsToMove.value);

const amountOfCardPages = computed(() => {
	const allCardsWidth = cardListRef?.value?.clientWidth;
	return Math.ceil(allCardsWidth / scrollIncriment.value);
});

function handleResize() {
	if (carouselRef?.value.clientWidth) {
		carouselWidth.value = carouselRef?.value.clientWidth;
	}
}

const scroll = (event: MouseEvent, direction: 'right' | 'left', pagesToMove: number = 1) => {
	const chevronElement = event.target as HTMLElement;
	const cardListElement =
		chevronElement.nodeName === 'svg'
			? chevronElement.parentElement?.querySelector('ul')
			: chevronElement.parentElement?.parentElement?.querySelector('ul');

	if (cardListElement === null || cardListElement === undefined) return;

	let pixelsToTranslate = 0;
	// Don't scroll if last element is already within viewport
	if (direction === 'right') {
		if (cardListElement.lastElementChild) {
			const carouselBounds = carouselRef?.value.getBoundingClientRect();
			const bounds = cardListElement.lastElementChild.getBoundingClientRect();
			if (
				bounds &&
				carouselBounds &&
				bounds.x + bounds.width < carouselBounds.x + carouselWidth.value
			) {
				return;
			}
		}
		activeCarouselPage.value += pagesToMove;
		pixelsToTranslate = -scrollIncriment.value * pagesToMove;
	} else if (direction === 'left') {
		activeCarouselPage.value -= pagesToMove;
		pixelsToTranslate = scrollIncriment.value * pagesToMove;
	}

	const marginLeftString =
		cardListElement.style.marginLeft === '' ? '0' : cardListElement.style.marginLeft;
	const currentMarginLeft = parseFloat(marginLeftString);
	const newMarginLeft = currentMarginLeft + pixelsToTranslate;
	// Don't let the list scroll far enough left that we see space before the
	//	first card.
	cardListElement.style.marginLeft = `${newMarginLeft > 0 ? 0 : newMarginLeft}px`;
};

function onCarouselPaginate(page: number) {
	activeCarouselPage.value = page;
}

watch(
	() => props.amountOfCards,
	() => {
		carouselWidth.value = carouselRef?.value.clientWidth;
	}
);

onMounted(() => window.addEventListener('resize', handleResize));

onBeforeUnmount(() => window.removeEventListener('resize', handleResize));
</script>

<style scoped>
.carousel {
	position: relative;
	display: flex;
}

.carousel ul {
	align-items: center;
	display: flex;
	gap: 1.5rem;
	transition: margin-left 0.8s;
}

.carousel ul:deep(li) {
	list-style: none;
}

.chevron-left,
.chevron-right {
	width: 4rem;
	position: absolute;
	z-index: 2;
	cursor: pointer;
	height: 100%;
	display: flex;
	align-items: center;
}

.chevron-left {
	left: -1rem;
	border-radius: 0rem 10rem 10rem 0rem;
}

.chevron-right {
	right: -1rem;
	border-radius: 10rem 0rem 0rem 10rem;
}

.carousel:hover .chevron-left,
.carousel:hover .chevron-right {
	background-color: var(--chevron-hover);
}

.carousel:hover .chevron-left > .pi-chevron-left,
.carousel:hover .chevron-right > .pi-chevron-right {
	color: var(--primary-color);
	opacity: 100;
}

.pi-chevron-left,
.pi-chevron-right {
	margin: 0 1rem;
	font-size: 2rem;
	opacity: 0;
	transition: opacity 0.2s ease;
}

.pi-chevron-left:hover,
.pi-chevron-right:hover {
	color: var(--primary-color);
}

.page-indicators {
	margin-top: 1.5rem;
	justify-content: center;
	gap: 0.5rem;
	display: flex;
}

.page-indicator.p-button.p-button-icon-only {
	color: var(--petri-nodeFill);
}

.page-indicator.p-button.p-button-icon-only[active='true'] {
	color: var(--primary-color);
}
</style>
