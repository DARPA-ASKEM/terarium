<template>
	<section>
		<h6>{{ target }}</h6>
		<tera-input
			label="Name"
			:model-value="getInitialName(model, target)"
			@update:model-value="$emit('update-initial-metadata', { key: 'name', value: $event })"
		/>
		<tera-input
			label="Description"
			:model-value="getInitialDescription(model, target)"
			@update:model-value="
				$emit('update-initial-metadata', {
					key: 'description',
					value: $event
				})
			"
		/>
		<tera-input
			label="Unit"
			:model-value="getInitialUnits(model, target)"
			@update:model-value="$emit('update-initial-metadata', { key: 'units', value: $event })"
		/>
		<!--TODO: Add support for editing concepts--->
		<tera-input label="Concept" :model-value="getInitialConcept(model, target)" disabled />
	</section>
</template>

<script setup lang="ts">
import { Model } from '@/types/Types';
import {
	getInitialName,
	getInitialDescription,
	getInitialUnits,
	getInitialConcept
} from '@/model-representation/service';
import TeraInput from '@/components/widgets/tera-input.vue';

defineProps<{
	model: Model;
	target: string;
}>();
</script>

<style scoped>
section {
	display: grid;
	grid-template-areas:
		'symbol name description description'
		'unit		unit concept .';
	grid-template-columns: max-content 30% 30% auto;
	gap: var(--gap-small);
	align-items: center;
}

h6 {
	grid-area: symbol;
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
