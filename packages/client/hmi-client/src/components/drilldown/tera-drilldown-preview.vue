<template>
	<div class="preview">
		<div class="content-container">
			<header ref="header">
				<h5>Preview</h5>
				<Dropdown
					v-if="options"
					class="output-dropdown"
					:model-value="output"
					:options="options"
					@update:model-value="emit('update:output', $event)"
				></Dropdown>
			</header>
			<main :style="{ height: mainHeight }">
				<slot />
			</main>
		</div>
		<footer>
			<Button v-if="canSaveAsset" outlined label="Save Model" @click="emit('save-asset')"></Button>
			<div class="right-aligned-buttons">
				<Button outlined label="Cancel" @click="emit('cancel')"></Button>
				<Button label="Apply Changes and Close" @click="emit('apply-changes')"></Button>
			</div>
		</footer>
	</div>
</template>

<script setup lang="ts">
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import { onUpdated, ref } from 'vue';

defineProps<{
	options?: string[]; // subject to change based on how we want to pass in output data
	output?: string;
	canSaveAsset?: boolean;
}>();

const emit = defineEmits(['cancel', 'apply-changes', 'save-asset', 'update:output']);

const mainHeight = ref();
const header = ref();

onUpdated(async () => {
	const headerHeight = header.value?.offsetHeight;
	mainHeight.value = `calc(100% - ${headerHeight}px)`;
});
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
	background-color: var(--surface-50);
	flex-grow: 1;
	padding: 1rem;
	border-radius: var(--border-radius-medium);
	box-shadow: 0px 0px 4px 0px rgba(0, 0, 0, 0.25) inset;
	overflow: hidden;
}

footer {
	display: flex;
}
.right-aligned-buttons {
	margin-left: auto;
	display: flex;
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
}

.output-dropdown:deep(.p-inputtext) {
	padding: 0.75rem 1rem;
	font-size: var(--font-body-small);
}

.output-dropdown:deep(.pi) {
	font-size: var(--font-body-small);
}
</style>
