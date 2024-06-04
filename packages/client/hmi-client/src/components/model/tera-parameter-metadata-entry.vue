<template>
	<section :class="{ 'has-toggle': isBase, 'no-second-row': isStratified }">
		<h6>{{ id }}</h6>
		<tera-input
			label="Name"
			:model-value="name ?? ''"
			@update:model-value="$emit('update-parameter', { key: 'name', value: $event })"
		/>
		<tera-input
			label="Description"
			:model-value="description ?? ''"
			@update:model-value="$emit('update-parameter', { key: 'description', value: $event })"
		/>
		<template v-if="!isStratified">
			<Button
				class="toggle"
				v-if="isBase"
				:icon="showStratifiedVariables ? 'pi pi-chevron-down' : 'pi pi-chevron-right'"
				text
				rounded
				@click="$emit('toggle-stratified-variables')"
			/>
			<tera-input
				label="Unit"
				:model-value="units?.expression ?? ''"
				@update:model-value="$emit('update-parameter', { key: 'units', value: $event })"
			/>
			<!--TODO: Add support for editing concepts-->
			<tera-input
				label="Concept"
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
			<Button v-if="isBase" size="small" label="Open matrix" text @click="$emit('open-matrix')" />
		</template>
	</section>
</template>

<script setup lang="ts">
import type { ModelParameter } from '@/types/Types';
import TeraInput from '@/components/widgets/tera-input.vue';
import { getCurieFromGroundingIdentifier, getNameOfCurieCached } from '@/services/concept';
import Button from 'primevue/button';

const props = defineProps<{
	parameter: ModelParameter;
	isBase?: boolean;
	isStratified?: boolean;
	showStratifiedVariables?: boolean;
}>();

defineEmits(['update-parameter', 'toggle-stratified-variables', 'open-matrix']);

const { id, name, description, grounding, units } = props.parameter;
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
		'toggle	unit concept open-matrix';
	grid-template-columns: max-content 30% auto 8rem;
}

section.no-second-row {
	gap: 0 var(--gap-small);
}

h6 {
	grid-area: symbol;
	justify-self: center;
}

.toggle {
	grid-area: toggle;
	margin-left: auto;
}

[label='Open Matrix'] {
	grid-area: open-matrix;
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
