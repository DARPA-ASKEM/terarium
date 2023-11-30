<template>
	<div class="preview">
		<div class="content-container">
			<header>
				<h5>{{ title ?? 'Preview' }}</h5>
				<Dropdown
					v-if="options"
					class="output-dropdown"
					:model-value="output"
					:options="options"
					@update:model-value="emit('update:output', $event)"
				></Dropdown>
			</header>
			<main>
				<slot />
			</main>
		</div>
		<footer>
			<slot name="footer" />
		</footer>
	</div>
</template>

<script setup lang="ts">
import Dropdown from 'primevue/dropdown';

defineProps<{
	title?: string;
	options?: string[]; // subject to change based on how we want to pass in output data
	output?: string;
	canSaveAsset?: boolean;
}>();

const emit = defineEmits(['update:output']);
</script>

<style scoped>
.preview {
	height: 100%;
	display: flex;
	flex-direction: column;
	overflow: hidden;
	gap: 0.5rem;
}
.content-container {
	display: flex;
	flex-direction: column;
	background-color: var(--surface-50);
	flex-grow: 1;
	padding: 1rem;
	border-radius: var(--border-radius-medium);
	box-shadow: 0px 0px 4px 0px rgba(0, 0, 0, 0.25) inset;
	overflow: hidden;
}

footer {
	display: flex;
	justify-content: flex-end;
	gap: 0.5rem;
}

header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding-bottom: 0.5rem;
}

.content-container > main {
	overflow-y: auto;
	flex-grow: 1;
}

.output-dropdown:deep(.p-inputtext) {
	padding: 0.75rem 1rem;
	font-size: var(--font-body-small);
}

.output-dropdown:deep(.pi) {
	font-size: var(--font-body-small);
}
</style>
