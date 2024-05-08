<template>
	<div class="toolbar">
		<p v-if="showThoughts" class="thought-bubble">
			{{ thought }}
		</p>
		<Button
			v-if="thought"
			link
			:label="`${showThoughts ? 'Hide' : 'Show'} thoughts`"
			@click="() => (showThoughts = !showThoughts)"
		/>
	</div>
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
/* Note that as this is intended to be used in the notebook lets default its margin here */
.toolbar {
	padding-left: var(--gap-medium);
}
.thought-bubble {
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
	padding: var(--gap-small);
	margin-top: var(--gap-small);
}
</style>
