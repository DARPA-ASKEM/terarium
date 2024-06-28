<template>
	<nav>
		<a
			v-for="[id, navOption] in navIds"
			:class="{ 'chosen-item': id === chosenItem }"
			:key="id"
			@click="scrollTo(id)"
		>
			{{ navOption }}
		</a>
	</nav>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue';

const props = defineProps<{
	elementWithNavIds: HTMLElement;
}>();

const navIds = ref<Map<string, string>>(new Map());
const chosenItem = ref<string | null>(null);

async function scrollTo(id: string) {
	const element = props.elementWithNavIds.querySelector(`#${id}`);
	if (!element) return;
	element.scrollIntoView({ behavior: 'smooth' });
}

function updateChosenItem() {
	let closestItem: string | null = null;
	let smallestDistance = props.elementWithNavIds.scrollHeight;
	const containerTop = props.elementWithNavIds.getBoundingClientRect().top;

	navIds.value.forEach((_, id) => {
		const element = props.elementWithNavIds.querySelector(`#${id}`)?.parentElement?.parentElement; // Gets accordion panel
		if (!element) return;

		const elementTop = Math.abs(element.getBoundingClientRect().top);
		const elementBottom = element.getBoundingClientRect().bottom - 10; // Extend the bottom slightly

		if (
			elementBottom >= containerTop && // Make sure element is below the scrollbar
			elementTop < smallestDistance // Update closestItem if this element is closer than the previous closest
		) {
			smallestDistance = elementTop;
			closestItem = id;
		}
	});
	chosenItem.value = closestItem;
}

onMounted(async () => {
	await nextTick();
	// Find all the headers to navigate to and assign them an id
	const headers = props.elementWithNavIds.querySelectorAll('.p-accordion-header > a');
	headers.forEach((header) => {
		// Extract header name
		const textNodes = Array.from(header.childNodes).filter(
			(node) => node.nodeType === Node.TEXT_NODE
		);
		let text = textNodes.map((node) => node.textContent).join('');
		if (!text) {
			const span = header.querySelector('span');
			if (span?.textContent) text = span.textContent;
		}
		if (!text) return;
		// Inject id into header based on header name
		const id = `header-nav-${text.replaceAll(' ', '-').trim()}`;
		header.setAttribute('id', id);
		// Add to map (HTML id -> navigation option/header name)
		navIds.value.set(id, text);
	});
	props.elementWithNavIds.addEventListener('scroll', updateChosenItem);
});

onUnmounted(() => {
	props.elementWithNavIds.removeEventListener('scroll', updateChosenItem);
});
</script>

<style scoped>
nav {
	display: flex;
	flex-direction: column;
	width: fit-content;
	gap: 1rem;
	padding: var(--gap) var(--gap-large) 0 var(--gap-small);
	/* Responsible for stickiness */
	position: sticky;
	top: 0;
	height: fit-content;
}

.chosen-item {
	font-weight: var(--font-weight-semibold);
	color: var(--primary-color);
}
</style>
