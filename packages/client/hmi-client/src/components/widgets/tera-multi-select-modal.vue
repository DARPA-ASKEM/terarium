<template>
	<Dialog
		:visible="isVisible"
		:show-header="false"
		position="bottom"
		:modal="false"
		:draggable="false"
		:closable="false"
		class="multi-selection-dialog"
	>
		<section class="flex align-items-center justify-content-center gap-2">
			<span> {{ selectedResources?.length }} items selected. </span>
			<Button
				v-for="(button, index) in buttons"
				:key="index"
				class="p-button-secondary multi-select-action"
				:icon="button.icon"
				@click="button.callback"
				>{{ button.label }}</Button
			>
		</section>
	</Dialog>
</template>

<script setup lang="ts">
import Button from 'primevue/button';
import Dialog from 'primevue/dialog';

defineProps<{
	isVisible: boolean;
	selectedResources: any[] | undefined;
	buttons?: { icon?: string; label: string; callback?: Function }[];
}>();
</script>

<style scoped>
.p-dialog.multi-selection-dialog .p-dialog-content {
	opacity: 0.9;
	background: inherit;
	padding: 1.33rem;
}
.p-dialog.p-component.multi-selection-dialog {
	max-width: 50vw;
	background-color: #e3efe480;
	margin-bottom: 5rem;
}

.p-button.p-button-secondary.multi-select-action {
	background-color: var(--surface-a);
	color: var(--text-color-primary);
	border: 1px solid var(--surface-border);
}
</style>
