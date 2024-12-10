<template>
	<div class="response-thought-container">
		<div class="thought" :class="{ hide: !props.showThought, show: props.showThought }">
			<slot />
			<Button text rounded icon="pi pi-times" @click="onClickClose" />
		</div>
	</div>
</template>
<script setup lang="ts">
import { defineProps, defineEmits } from 'vue';
import Button from 'primevue/button';

const props = defineProps<{
	showThought?: boolean;
}>();

const emit = defineEmits(['toggleThought']);

const onClickClose = () => {
	emit('toggleThought', !props.showThought);
};
</script>

<style scoped>
.thought {
	display: flex;
	justify-content: space-between;
	white-space: pre-line;
	background-color: var(--surface-50);
	border: 1px solid var(--surface-border-light);
	box-shadow: inset 0 0 4px 0 rgba(0, 0, 0, 0.05);
	border-radius: var(--border-radius);
	padding: var(--gap-4);
	color: var(--text-color);
	width: 100%;
	margin-top: var(--gap-2);
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
