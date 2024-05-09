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
import { watch, ref, computed } from 'vue';
import Button from 'primevue/button';

const props = defineProps<{
	llmThoughts: any[];
}>();
const showThoughts = ref(false);
const thought = computed(() => {
	let aString = '';
	props.llmThoughts.forEach((ele) => {
		const llmResponse = ele.content?.thought ?? ele.content?.text ?? '';
		aString = aString.concat(llmResponse, '\n \n');
	});
	return aString;
});

// Set model, modelNodeOptions
watch(
	() => props.llmThoughts,
	async () => {
		console.log(props.llmThoughts);
	},
	{ immediate: true }
);
</script>

<style scoped>
.thought-bubble {
	white-space: pre-line;
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
	padding: var(--gap-small);
	margin-top: var(--gap-small);
}
</style>
