<template>
	<nav>
		<a v-for="[id, navOption] in navIds" :key="id" @click="scrollTo(id)">
			{{ navOption }}
		</a>
	</nav>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue';

const props = defineProps<{
	elementWithNavIds: HTMLElement;
}>();

const navIds = ref<Map<string, string>>(new Map());

async function scrollTo(id: string) {
	const element = props.elementWithNavIds.querySelector(`#${id}`);
	if (!element) return;
	element.scrollIntoView({ behavior: 'smooth' });
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
</style>
