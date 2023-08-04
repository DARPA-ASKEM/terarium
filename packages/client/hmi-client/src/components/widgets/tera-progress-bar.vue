<template>
	<div>
		<i class="pi pi-spin pi-spinner"></i>
		<span>{{ props.status }}...{{ '(' + props.value + '%)' }}</span>
		<ProgressBar class="progress-bar" :value="props.value"></ProgressBar>
	</div>
</template>

<script setup lang="ts">
import { ProgressState } from '@/types/workflow';
import ProgressBar from 'primevue/progressbar';

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
