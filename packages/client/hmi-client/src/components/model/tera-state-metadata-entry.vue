<template>
	<section>
		<h6>{{ id }}</h6>
		<tera-input
			title="Name"
			placeholder="Add a name"
			:model-value="name ?? ''"
			@update:model-value="$emit('update-state', { key: 'name', value: $event })"
		/>
		<!--FIXME: description property should be added to the state type-->
		<tera-input
			title="Description"
			:model-value="''"
			placeholder="Add a description"
			@update:model-value="
				$emit('update-state', {
					key: 'description',
					value: $event
				})
			"
			disabled
		/>
		<tera-input
			label="Unit"
			placeholder="Add a unit"
			:model-value="initial?.expression ?? ''"
			@update:model-value="$emit('update-state', { key: 'units', value: $event })"
		/>
		<!--TODO: Add support for editing concepts-->
		<tera-input
			label="Concept"
			placeholder="Select a concept"
			icon="pi pi-search"
			:model-value="grounding?.identifiers[0] ?? ''"
			disabled
		/>
	</section>
</template>

<script setup lang="ts">
import { PetriNetState, RegNetVertex } from '@/types/Types';
import TeraInput from '@/components/widgets/tera-input.vue';

const props = defineProps<{
	state: PetriNetState | RegNetVertex;
	showStratifiedVariables?: boolean;
}>();

defineEmits(['update-state', 'toggle-stratified-variables']);

const { id, name, grounding, initial } = props.state; // description property should be added to the state type
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
		'toggle	unit concept .';
}

h6 {
	grid-area: symbol;

	&::after {
		content: '|';
		color: var(--text-color-light);
		margin-left: var(--gap-2);
	}
}

button {
	grid-area: toggle;
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
