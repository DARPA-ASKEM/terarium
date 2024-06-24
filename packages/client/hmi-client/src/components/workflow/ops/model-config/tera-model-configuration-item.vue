<template>
	<div class="config-card" :class="{ 'card-selected': selected }">
		<div>
			<span class="flex align-items-center"
				><h6>{{ configuration.name }}</h6>
				<Button class="ml-auto" text icon="pi pi-ellipsis-v" @click.stop></Button
			></span>
			<p>{{ configuration.description }}</p>
			<p>{{ formatTimestamp(configuration.createdOn) }}</p>
		</div>
		<Divider />
	</div>
</template>

<script setup lang="ts">
import { ModelConfiguration } from '@/types/Types';
import { formatTimestamp } from '@/utils/date';
import Button from 'primevue/button';
import Divider from 'primevue/divider';

defineProps<{
	configuration: ModelConfiguration;
	selected?: boolean;
}>();
</script>

<style scoped>
.config-card {
	&:hover {
		cursor: pointer;
		background-color: var(--gray-100);
	}
	&.card-selected {
		background-color: var(--gray-100);
		border-left: 4px solid var(--primary-color);
	}

	> div {
		padding-left: var(--gap-small);
	}
}
:deep(.p-divider) {
	&.p-divider-horizontal {
		margin-top: var(--gap);
		margin-bottom: 0;
		color: var(--gray-300);
	}
}
</style>
