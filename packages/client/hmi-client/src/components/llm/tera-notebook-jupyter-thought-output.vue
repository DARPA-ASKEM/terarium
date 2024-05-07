<template>
	<div v-if="showThoughts" class="thought-bubble">
		<p>{{ thought }}</p>
	</div>
	<Button
		v-if="thought"
		link
		:label="`${showThoughts ? 'Hide' : 'Show'} thoughts`"
		@click="() => (showThoughts = !showThoughts)"
	/>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue';
import Button from 'primevue/button';

const props = defineProps<{
	llmThought?: any;
}>();

const showThoughts = ref(false);
const thought = computed(() => props.llmThought.content.thought ?? 'No thought');

onMounted(async () => {
	console.log(props.llmThought);
	console.log(props.llmThought.content.thought);
});
</script>

<style scoped>
.thought-bubble {
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
	padding: 0.75rem;
}
</style>
