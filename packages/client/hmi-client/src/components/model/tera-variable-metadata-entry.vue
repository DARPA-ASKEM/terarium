<template>
	<section>
		<h6>{{ id }}</h6>
		<tera-input
			title="Name"
			placeholder="Add a name"
			:model-value="name ?? ''"
			@update:model-value="$emit('update-variable', { key: 'name', value: $event })"
		/>
		<tera-input
			label="Unit"
			placeholder="Add a unit"
			:model-value="unitExpression ?? ''"
			@update:model-value="$emit('update-variable', { key: 'units', value: $event })"
			:disabled="unitExpression === undefined"
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
		<!--FIXME: description property should be added to the state type-->
		<!--disabling may not always be a good idea since you may want to create the property if it is a valid one-->
		<tera-input
			title="Description"
			placeholder="Add a description"
			:model-value="description ?? ''"
			@update:model-value="$emit('update-variable', { key: 'description', value: $event })"
			:disabled="description === undefined"
		/>
	</section>
</template>

<script setup lang="ts">
import TeraInput from '@/components/widgets/tera-input.vue';
import type { ModelVariable } from '@/types/Model';
import { getCurieFromGroundingIdentifier, getNameOfCurieCached } from '@/services/concept';

const props = defineProps<{
	variable: ModelVariable;
}>();

defineEmits(['update-variable']);

const { id, name, description, grounding, unitExpression } = props.variable;
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
