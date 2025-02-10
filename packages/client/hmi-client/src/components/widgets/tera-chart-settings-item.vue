<template>
	<div class="chart-settings-item" :style="{ 'border-left': borderStyle }">
		<h6>{{ settings.name }}</h6>
		<slot name="main"></slot>
		<div v-if="areButtonsEnabled" class="btn-group">
			<Button icon="pi pi-cog" rounded text @click="$emit('open')" />
			<Button icon="pi pi-times" rounded text @click="$emit('remove', settings.id)" />
		</div>
	</div>
</template>

<script setup lang="ts">
import Button from 'primevue/button';
import { computed } from 'vue';

const props = defineProps({
	settings: {
		type: Object,
		default: () => ({})
	},
	areButtonsEnabled: {
		type: Boolean,
		default: true
	}
});

defineEmits(['open', 'remove']);

const borderStyle = computed(() => (props.settings.primaryColor ? `4px solid ${props.settings.primaryColor}` : ''));
</script>

<style scoped>
.chart-settings-item {
	border-left: 4px solid #667085;
	padding: var(--gap-3);
	padding-left: var(--gap-4);
	background: var(--surface-0);
	display: flex;
	align-items: center;
	justify-content: space-between;
	width: 100%;
}
.chart-settings-item h6 {
	width: 80%;
}

.btn-group {
	display: flex;
}
</style>
