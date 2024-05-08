<template>
	<p v-if="showThoughts" class="thought-bubble">
		{{ thought }}
	</p>
	<Button
		v-if="thought"
		link
		:label="`${showThoughts ? 'Hide' : 'Show'} thoughts`"
		@click="() => (showThoughts = !showThoughts)"
	/>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import Button from 'primevue/button';

const props = defineProps<{
	llmThought?: any;
}>();
const showThoughts = ref(false);
const thought = computed(() => props?.llmThought?.content?.thought ?? '');
</script>

<style scoped>
.thought-bubble {
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
	padding: var(--gap-small);
	margin-top: var(--gap-small);
}
</style>
