<template>
	<div class="chart-settings-item">
		<h6>{{ settings.name }}</h6>
		<div class="btn-group">
			<Button label="Open chart settings" outlined severity="secondary" size="small" @click="$emit('open')" />
			<Button icon="pi pi-trash" rounded text @click="$emit('delete')" />
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
defineEmits(['open', 'delete']);
</script>

<style scoped>
.chart-settings-item {
	border-left: 4px solid #667085;
	padding: 0.75rem;
	padding-left: 1rem;

	margin-bottom: 0.5rem;
	margin-top: 0.5rem;

	background: #fafafa;
}
.btn-group {
	display: flex;
	justify-content: space-between;
}
</style>
