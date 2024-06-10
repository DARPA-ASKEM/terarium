<template>
	<section :class="{ 'has-toggle': isBase, 'no-second-row': isStratified }">
		<h6>{{ id }}</h6>
		<tera-input
			label="Name"
			:model-value="name ?? ''"
			@update:model-value="$emit('update-initial-metadata', { key: 'name', value: $event })"
		/>
		<tera-input
			label="Description"
			:model-value="getInitialDescription(model, id)"
			@update:model-value="
				$emit('update-initial-metadata', {
					key: 'description',
					value: $event
				})
			"
		/>
		<template v-if="!isStratified">
			<Button
				v-if="isBase"
				:icon="showStratifiedVariables ? 'pi pi-chevron-down' : 'pi pi-chevron-right'"
				text
				rounded
				@click="$emit('toggle-stratified-variables')"
			/>
			<tera-input
				label="Unit"
				:model-value="initial?.expression ?? ''"
				@update:model-value="$emit('update-initial-metadata', { key: 'units', value: $event })"
			/>
			<!--TODO: Add support for editing concepts-->
			<tera-input label="Concept" :model-value="grounding?.identifiers[0] ?? ''" disabled />
		</template>
	</section>
</template>

<script setup lang="ts">
import { Model, PetriNetState } from '@/types/Types';
import { getInitialDescription } from '@/model-representation/service';
import TeraInput from '@/components/widgets/tera-input.vue';
import Button from 'primevue/button';

const props = defineProps<{
	model: Model;
	state: PetriNetState;
	isBase?: boolean;
	isStratified?: boolean;
	showStratifiedVariables?: boolean;
}>();

defineEmits(['update-initial-metadata', 'toggle-stratified-variables', 'open-matrix']);

const { id, name, grounding, initial } = props.state;

console.log(props.model);
</script>

<style scoped>
section {
	display: grid;
	grid-template-areas:
		'symbol name description description'
		'unit	unit concept .';
	grid-template-columns: max-content 30% 30% auto;
	gap: var(--gap-small);
	align-items: center;
}

section.has-toggle {
	grid-template-areas:
		'symbol name description description'
		'toggle	unit concept .';
}

section.no-second-row {
	gap: 0 var(--gap-small);
}

h6 {
	grid-area: symbol;
	justify-self: center;
}

button {
	grid-area: toggle;
}

:deep([label='Name']) {
	grid-area: name;
}

:deep([label='Description']) {
	grid-area: description;
}

:deep([label='Unit']) {
	grid-area: unit;
}

:deep([label='Concept']) {
	grid-area: concept;
}
</style>
