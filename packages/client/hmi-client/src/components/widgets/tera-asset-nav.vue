<template>
	<nav>
		<a v-for="id in navIdsInView" :key="id" @click="scrollTo(id)">
			{{ id.replace('-', ' ') }}
		</a>
	</nav>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue';

const props = defineProps<{
	navIds: string[];
	elementWithNavIds: HTMLElement;
}>();

const navIdsInView = ref<string[]>([]);

async function scrollTo(id: string) {
	const element = props.elementWithNavIds.querySelector(`#${id}`);
	if (!element) return;
	element.scrollIntoView({ behavior: 'smooth' });
}

onMounted(async () => {
	await nextTick();
	navIdsInView.value = props.navIds.filter((id) => props.elementWithNavIds.querySelector(`#${id}`));
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
