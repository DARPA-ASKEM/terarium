<template>
	<div class="critera-of-interest-card">
		<header class="flex">
			<tera-toggleable-input :model-value="card.name" tag="h4" @update:model-value="emit('update', { name: $event })" />
			<Button class="ml-auto" text icon="pi pi-trash" @click="emit('delete')" />
		</header>

		<div>
			For configuration
			<Dropdown
				:options="card.configurations"
				:model-value="card.selectedConfiguration"
				@update:model-value="emit('update', { selectedConfiguration: $event })"
				placeholder="Select..."
			/>
			rank interventions based on the
			<Dropdown
				:options="rankOptions"
				:model-value="card.rank"
				option-label="label"
				option-value="value"
				@update:model-value="emit('update', { rank: $event })"
			/>
			value of
			<Dropdown
				:options="card.variables"
				:model-value="card.selectedVariable"
				@update:model-value="emit('update', { selectedVariable: $event })"
				placeholder="Select..."
			/>
			at
			<Dropdown
				:options="timepointOptions"
				:model-value="card.timepoint"
				option-label="label"
				option-value="value"
				@update:model-value="emit('update', { timepoint: $event })"
			/>
			timepoint.
		</div>
	</div>
</template>

<script setup lang="ts">
import TeraToggleableInput from '@/components/widgets/tera-toggleable-input.vue';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import { CriteriaOfInterestCard, RankOption, TimepointOption } from './compare-datasets-operation';

const timepointOptions = [
	{ label: 'the last', value: TimepointOption.LAST },
	{ label: 'the first', value: TimepointOption.FIRST }
];

const rankOptions = [
	{ label: 'minimum', value: RankOption.MINIMUM },
	{ label: 'maximum', value: RankOption.MAXIMUM }
];

const emit = defineEmits(['delete', 'update']);
defineProps<{
	card: CriteriaOfInterestCard;
}>();
</script>

<style scoped>
.critera-of-interest-card {
	background-color: var(--surface-0);
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius-medium);
	padding: var(--gap-2) var(--gap-4);
	gap: var(--gap-2);
	display: flex;
	flex-direction: column;
}
</style>
