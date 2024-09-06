<template>
	<div class="chart-settings-item">
		<h6>{{ settings.name }}</h6>
		<div class="btn-group">
			<Button label="Open chart settings" outlined severity="secondary" size="small" @click="$emit('open')" />
			<Button icon="pi pi-trash" rounded text @click="$emit('remove', settings.id)" />
		</div>
	</div>
</template>

<script setup lang="ts">
import Button from 'primevue/button';

defineProps({
	settings: {
		type: Object,
		default: () => ({})
	}
});
defineEmits(['open', 'remove']);
</script>

<style scoped>
.chart-settings-item {
	border-left: 4px solid #667085;
	padding: 0.75rem;
	padding-left: 1rem;
	background: var(--surface-50);
}
.btn-group {
	display: flex;
	justify-content: space-between;
}
</style>
