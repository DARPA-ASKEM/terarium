<template>
	<nav>
		<a v-for="id in navIds" :key="id" @click="scrollTo(id)">
			{{ id.replaceAll('-', ' ') }}
		</a>
	</nav>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue';

const props = defineProps<{
	elementWithNavIds: HTMLElement;
}>();

const navIds = ref<string[]>([]);

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
		const textNodes = Array.from(header.childNodes).filter(
			(node) => node.nodeType === Node.TEXT_NODE
		);
		let text = textNodes.map((node) => node.textContent).join('');
		if (!text) {
			const span = header.querySelector('span');
			if (span?.textContent) text = span.textContent.trim();
		}
		if (!text) return;
		const id = `${text.replace(/\s+/g, '-')}`;
		header.setAttribute('id', id);
	});
	navIds.value = Array.from(headers).map((header) => header.id);
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
