<template>
	<div>
		<i class="pi pi-spin pi-spinner"></i>
		<p>{{ message }}</p>
		<ProgressBar v-if="props.value" class="progress-bar" :value="props.value"></ProgressBar>
	</div>
</template>

<script setup lang="ts">
import { ProgressState } from '@/types/workflow';
import ProgressBar from 'primevue/progressbar';
import { computed } from 'vue';

const message = computed(() => `${props.status}...${props.value ? `(${props.value}%)` : ''}`);

const props = defineProps<{
	status: ProgressState;
	value: number;
}>();
</script>

<style scoped>
.progress-bar {
	width: 100%;
}
</style>
