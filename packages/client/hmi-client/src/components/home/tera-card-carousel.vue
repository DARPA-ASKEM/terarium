<template>
	<main>
		<section class="carousel" ref="carouselRef">
			<div v-if="activeCarouselPage > 1" class="chevron-left" @click="scroll(-1)">
				<i class="pi pi-chevron-left" />
			</div>
			<div v-if="activeCarouselPage !== amountOfCardPages" class="chevron-right" @click="scroll(1)">
				<i class="pi pi-chevron-right" />
			</div>
			<ul v-if="isLoading">
				<li v-for="i in amountOfSkeletonCards" :key="i">
					<slot name="skeleton-card" />
				</li>
			</ul>
			<ul v-else ref="cardListRef">
				<slot name="card-list-items" />
			</ul>
		</section>
		<section v-if="amountOfCardPages > 1" class="page-indicators">
			<!--page starts counting at 1 because we are looping through a number-->
			<Button
				v-for="page in amountOfCardPages"
				:key="page"
				icon="pi pi-circle-fill"
				:active="activeCarouselPage === page"
				class="page-indicator p-button-icon-only p-button-text p-button-rounded"
				@click="page !== activeCarouselPage && scroll(page - activeCarouselPage)"
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

// If cards end up varying in width cardWidth could be a prop
const cardWidth = 238; // 17rem
const rightGapWidth = 21; // 1.5rem

const carouselRef = ref();
const cardListRef = ref();
const carouselWidth = ref(0);
const activeCarouselPage = ref(1);

const amountOfCardsToMove = computed(() => Math.floor(carouselWidth.value / (cardWidth + rightGapWidth)));

const scrollIncrement = computed(() => (cardWidth + rightGapWidth) * amountOfCardsToMove.value);

const amountOfCardPages = computed(() => {
	const allCardsWidth = cardListRef?.value?.clientWidth;
	const pages = Math.ceil(allCardsWidth / scrollIncrement.value);
	if (Number.isNaN(pages) || pages === Infinity) return 1;
	return pages;
});

function handleResize() {
	carouselWidth.value = carouselRef.value.clientWidth ?? 0;
}

function scroll(pagesToMove: number) {
	if (!cardListRef.value) return;

	// Don't scroll if last element is already within viewport
	if (pagesToMove > 0 && cardListRef.value.lastElementChild) {
		const carouselBounds = carouselRef?.value.getBoundingClientRect();
		const cardListBounds = cardListRef.value.lastElementChild.getBoundingClientRect();
		if (
			cardListBounds &&
			carouselBounds &&
			cardListBounds.x + cardListBounds.width < carouselBounds.x + carouselWidth.value
		) {
			return;
		}
	}
	activeCarouselPage.value += pagesToMove;
	const pixelsToTranslate = -scrollIncrement.value * pagesToMove;
	const marginLeftString = cardListRef.value.style.marginLeft === '' ? '0' : cardListRef.value.style.marginLeft;
	const newMarginLeft = parseFloat(marginLeftString) + pixelsToTranslate;
	// Don't let the list scroll far enough left that we see space before the first card.
	cardListRef.value.style.marginLeft = `${newMarginLeft > 0 ? 0 : newMarginLeft}px`;
}

watch(
	() => props.amountOfCards,
	() => handleResize()
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
	color: var(--gray-0);
	opacity: 100;
}

.pi-chevron-left,
.pi-chevron-right {
	margin: 0 1rem;
	font-size: 2.5rem;
	opacity: 0;
	transition: opacity 0.2s ease;
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
