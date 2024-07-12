<template>
	<section>
		<h6>{{ id }}</h6>
		<tera-input
			title="Name"
			placeholder="Add a name"
			:model-value="name ?? ''"
			@update:model-value="$emit('update-parameter', { key: 'name', value: $event })"
		/>
		<tera-input
			title="Description"
			placeholder="Add a description"
			:model-value="description ?? ''"
			@update:model-value="$emit('update-parameter', { key: 'description', value: $event })"
		/>
		<tera-input
			label="Unit"
			placeholder="Add a unit"
			:model-value="units?.expression ?? ''"
			@update:model-value="$emit('update-parameter', { key: 'units', value: $event })"
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
import type { ModelParameter } from '@/types/Types';
import TeraInput from '@/components/widgets/tera-input.vue';
import { getCurieFromGroundingIdentifier, getNameOfCurieCached } from '@/services/concept';

const props = defineProps<{
	parameter: ModelParameter;
	showStratifiedVariables?: boolean;
}>();

defineEmits(['update-parameter', 'toggle-stratified-variables']);

const { id, name, description, grounding, units } = props.parameter;
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

section.has-toggle {
	grid-template-areas:
		'symbol name description description'
		'toggle	unit concept open-matrix';
	grid-template-columns: max-content 30% auto 8rem;
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

.toggle {
	grid-area: toggle;
	margin-left: auto;
}

[label='Open Matrix'] {
	grid-area: open-matrix;
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
