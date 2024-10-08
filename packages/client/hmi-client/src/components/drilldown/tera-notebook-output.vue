<template>
	<tera-resizable-panel :start-height="100" :resize-from-top="true" class="container">
		<h6>
			<span>{{ props.name }}</span>
			<Button rounded text icon="pi pi-times" @click="emit('close')" />
		</h6>
		<code>{{ props.traceback }}</code>
	</tera-resizable-panel>
</template>

<script setup lang="ts">
import Button from 'primevue/button';
import TeraResizablePanel from '@/components/widgets/tera-resizable-panel.vue';

const props = defineProps<{
	name?: string;
	traceback?: string;
}>();
const emit = defineEmits(['close']);
</script>

<style scoped>
.container {
	background-color: var(--surface-100);
	padding: var(--gap-small);
	border-radius: var(--border-radius);
	border: 1px solid var(--surface-border-light);
	/* Shadow/medium */
	box-shadow:
		0 2px 4px -1px rgba(0, 0, 0, 0.06),
		0 4px 6px -1px rgba(0, 0, 0, 0.08);
}

h6 {
	display: flex;
	justify-content: space-between;
}

.hide {
	display: none;
}
</style>
