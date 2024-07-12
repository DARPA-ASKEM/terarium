<template>
	<section>
		<h6>{{ id }}</h6>
		<tera-input
			title="Name"
			placeholder="Add a name"
			:model-value="name ?? ''"
			@update:model-value="$emit('update-variable', { key: 'name', value: $event })"
		/>
		<!--FIXME: description property should be added to the state type-->
		<!--disabling may not always be a good idea since you may want to create the property if it is a valid one-->
		<tera-input
			title="Description"
			placeholder="Add a description"
			:model-value="description ?? ''"
			@update:model-value="$emit('update-variable', { key: 'description', value: $event })"
			:disabled="description === undefined"
		/>
		<tera-input
			label="Unit"
			placeholder="Add a unit"
			:model-value="unit ?? ''"
			@update:model-value="$emit('update-variable', { key: 'units', value: $event })"
			:disabled="unit === undefined"
		/>
		<!--TODO: Add support for editing concepts-->
		<tera-input
			label="Concept"
			placeholder="Select a concept"
			icon="pi pi-search"
			disabled
			:model-value="
				grounding?.identifiers
					? getNameOfCurieCached(
							new Map<string, string>(),
							getCurieFromGroundingIdentifier(grounding.identifiers)
						)
					: ''
			"
		/>
	</section>
</template>

<script setup lang="ts">
import TeraInput from '@/components/widgets/tera-input.vue';
import { getCurieFromGroundingIdentifier, getNameOfCurieCached } from '@/services/concept';

defineProps<{
	id: string;
	name?: string;
	description?: string;
	grounding?: any;
	unit?: string; // 'units' key may not work all the time dor states,
	showStratifiedVariables?: boolean;
}>();

defineEmits(['update-variable']);
</script>

<style scoped>
section {
	display: grid;
	grid-template-areas:
		'symbol name unit . concept'
		'description description description description description';
	grid-template-columns: max-content max-content max-content auto max-content;
	gap: var(--gap-2);
	align-items: center;
}

h6 {
	grid-area: symbol;
	justify-self: center;
	&::after {
		content: '|';
		color: var(--text-color-light);
		margin-left: var(--gap-2);
	}
}

:deep([title='Name']) {
	grid-area: name;
}

:deep([title='Description']) {
	grid-area: description;
}

:deep([label='Unit']) {
	grid-area: unit;
}

:deep([label='Concept']) {
	grid-area: concept;
}
</style>
