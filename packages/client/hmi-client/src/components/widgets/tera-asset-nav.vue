<template>
	<nav>
		<a v-for="content in assetNavIdsInView" :key="content" @click="scrollTo(content)">
			{{ content.replace('-', ' ') }}
		</a>
	</nav>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue';

const props = defineProps<{
	assetNavIds: string[];
}>();

const assetNavIdsInView = ref<string[]>([]);

async function scrollTo(elementId: string) {
	const element = document.getElementById(elementId);
	if (!element) return;
	element.scrollIntoView({ behavior: 'smooth' });
}

onMounted(async () => {
	await nextTick();
	assetNavIdsInView.value = props.assetNavIds.filter((id) => document.getElementById(id));
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
