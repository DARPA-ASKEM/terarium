<template>
	<section :class="{ 'has-toggle': isBase, 'no-second-row': isStratified }">
		<h6>{{ id }}</h6>
		<tera-input
			label="Name"
			:model-value="name ?? ''"
			@update:model-value="$emit('update-state', { key: 'name', value: $event })"
		/>
		<!--FIXME: description property should be added to the state type-->
		<tera-input
			label="Description"
			:model-value="''"
			@update:model-value="
				$emit('update-state', {
					key: 'description',
					value: $event
				})
			"
			disabled
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
				@update:model-value="$emit('update-state', { key: 'units', value: $event })"
			/>
			<!--TODO: Add support for editing concepts-->
			<tera-input label="Concept" :model-value="grounding?.identifiers[0] ?? ''" disabled />
		</template>
	</section>
</template>

<script setup lang="ts">
import { PetriNetState, RegNetVertex } from '@/types/Types';
import TeraInput from '@/components/widgets/tera-input.vue';
import Button from 'primevue/button';

const props = defineProps<{
	state: PetriNetState | RegNetVertex;
	isBase?: boolean;
	isStratified?: boolean;
	showStratifiedVariables?: boolean;
}>();

defineEmits(['update-state', 'toggle-stratified-variables', 'open-matrix']);

const { id, name, grounding, initial } = props.state; // description property should be added to the state type
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
