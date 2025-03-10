<template>
	<tera-resizable-panel v-if="!isHidden" :start-height="100" :resize-from-top="true" class="container output-panel">
		<h6 class="pt-1">
			<span>{{ name }}</span>
			<span>
				<Button rounded text label="Clear" @click="clearConsole" />
				<Button rounded text icon="pi pi-times" @click="isHidden = true" />
			</span>
		</h6>
		<code class="code-section">{{ localTraceback }}</code>
	</tera-resizable-panel>
	<div v-if="isHidden" class="container output-panel-closed">
		<h6>
			<span>{{ name }}</span>
			<Button rounded text icon="pi pi-angle-double-up pb-2" @click="isHidden = false" />
		</h6>
	</div>
</template>

<script setup lang="ts">
import { watch, ref } from 'vue';
import Button from 'primevue/button';
import TeraResizablePanel from '@/components/widgets/tera-resizable-panel.vue';

const props = defineProps<{
	name?: string;
	traceback?: string;
}>();

const name = props.name ?? 'Output console';
const isHidden = ref<boolean>(false);
const localTraceback = ref<string>(props.traceback ?? '');

function clearConsole() {
	localTraceback.value = '';
}

watch(
	() => props.traceback,
	() => {
		localTraceback.value = props.traceback ?? '';
	}
);
</script>

<style scoped>
.container {
	background-color: var(--surface-50);
	padding: var(--gap-2);
	border-radius: var(--border-radius-medium);
	border: 1px solid var(--surface-border-light);
	/* Shadow/medium */
	box-shadow:
		0 2px 4px -1px rgba(0, 0, 0, 0.06),
		0 4px 6px -1px rgba(0, 0, 0, 0.08);
}

.output-panel {
	overflow-y: auto;
	background: var(--surface-50);
}
.output-panel-closed {
	padding-bottom: var(--gap-0);
}
.code-section {
	white-space: pre-wrap;
}

h6 {
	display: flex;
	justify-content: space-between;
}

.hide {
	display: none;
}
</style>
