<template>
	<nav>
		<div @click="pp">
			<Button icon="pi pi-file-edit" class="p-button-icon-only p-button-text p-button-rounded" />
			<Button icon="pi pi-folder" class="p-button-icon-only p-button-text p-button-rounded" />
			<Button
				icon="pi pi-sort-amount-down"
				class="p-button-icon-only p-button-text p-button-rounded"
			/>
			<Button icon="pi pi-arrows-v" class="p-button-icon-only p-button-text p-button-rounded" />
		</div>
		<ul></ul>
	</nav>
</template>

<script setup lang="ts">
import { ProjectType } from '@/types/Project';
import Button from 'primevue/button';

const props = defineProps<{
	project: ProjectType | null;
}>();

function pp() {
	console.log(props.project);
}
</script>

<style scoped>
nav {
	margin: 0 1rem 1rem 1rem;
}
</style>
