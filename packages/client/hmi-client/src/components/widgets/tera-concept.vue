<template>
	<main>
		<label>Concept</label>

		<template v-if="isPreview"> </template>

		<template v-else>
			<AutoComplete
				size="small"
				placeholder="Search concepts"
				v-model="query"
				:suggestions="results"
				optionLabel="name"
				@complete="async () => (results = await searchCuriesEntities(query))"
				@item-select="$emit('update-item', { key: 'concept', value: $event.value.curie })"
				@keyup.enter="applyValidConcept"
				@blur="applyValidConcept"
			/>
		</template>
	</main>
</template>

<script setup lang="ts">
import { getCurieFromGroundingIdentifier, getNameOfCurieCached, searchCuriesEntities } from '@/services/concept';
import AutoComplete from 'primevue/autocomplete';
import { Grounding } from '@/types/Types';
import { watch } from 'vue';

const props = defineProps<{
	isPreview?: boolean;
	grounding: Grounding;
}>();

const emit = defineEmits(['update']);

// Used if an option isn't selected from the Autocomplete suggestions but is typed in regularly
function applyValidConcept() {
	// Allows to empty the concept
	if (query.value === '') {
		emit('update', { key: 'concept', value: '' });
	}
	// If what was typed was one of the results then choose that result
	else {
		const concept = results.value.find((result) => result.name === query.value);
		if (concept) {
			emit('update', { key: 'concept', value: concept.curie });
		}
	}
}

watch(
	props.grounding,
	async (identifiers) => {
		if (identifiers) query.value = await getNameOfCurieCached(getCurieFromGroundingIdentifier(identifiers));
	},
	{ immediate: true }
);
</script>

<style scoped></style>
