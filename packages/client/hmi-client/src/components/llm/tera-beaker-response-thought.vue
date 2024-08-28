<template>
	<div class="response-thought-container">
		<div ref="responseThought" class="thought" :class="{ hide: !props.showThought, show: props.showThought }">
			<slot />
		</div>
	</div>
</template>
<script setup lang="ts">
import { ref } from 'vue';

const responseThought = ref(<HTMLElement | null>null);

const props = defineProps<{
	showThought?: boolean;
}>();
</script>

<style scoped>
.thought {
	white-space: pre-line;
	background-color: #fff;
	border: 1px solid var(--surface-border);
	border-radius: var(--border-radius);
	padding: var(--gap);
	color: var(--text-color);
	width: 100%;
}
.thought:deep(ul li)::marker {
	margin-left: 2rem !important;
}
.hide {
	max-height: 0;
	overflow: hidden;
	margin: 0;
	opacity: 0;
	padding: 0;
	transition:
		opacity 0.3s,
		margin 0.5s ease-in,
		padding 0.5s ease-in,
		max-height 0.4s ease-in;
}

.show {
	transition:
		margin 0.5s ease-in,
		opacity 0.3s ease-in,
		max-height 0.5s ease-in;
}

.response-thought-container {
	display: flex;
	flex-direction: column;
}
</style>
