<template>
	<Button
		v-if="thought"
		link
		size="small"
		class="thought-button"
		:label="`${showThoughts ? 'Hide' : 'Show'} thoughts`"
		@click="() => (showThoughts = !showThoughts)"
	/>
	<p v-if="showThoughts" class="thought-bubble">
		<span v-if="llmQuery.length > 0" class="thought-bubble-title">
			{{ llmQuery }}
			<br />
		</span>
		{{ thought }}
	</p>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import Button from 'primevue/button';

const props = defineProps<{
	llmThoughts: any[];
	llmQuery: string;
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
</script>

<style scoped>
.thought-bubble {
	padding: 0 var(--gap-2) var(--gap-2) var(--gap-2);
}
.thought-bubble-title {
	font-weight: var(--font-weight-semibold);
}
.thought-button {
	padding: var(--gap-2);
}
</style>
