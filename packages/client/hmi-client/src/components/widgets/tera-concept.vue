<template>
	<main>
		<h6>Concept</h6>

		<template v-if="isPreview">
			<p :title="selectedConcept?.curie">{{ selectedConcept?.name }}</p>
		</template>

		<template v-else>
			<AutoComplete
				optionLabel="name"
				placeholder="Search concepts"
				size="small"
				v-model="selectedConcept"
				:suggestions="resultsDKG"
				@complete="searchDKG"
				@item-select="saveConcept"
				@keyup.enter="saveConcept"
				@blur="saveConcept"
			/>
			<!--
			<AutoComplete
				size="small"
				placeholder="Additional concepts"
				optionLabel="name"
				multiple
				v-model="query"
				:suggestions="results"
				@complete="async () => (results = await searchCuriesEntities(query))"
				@item-select="$emit('update-item', { key: 'concept', value: $event.value.curie })"
				@keyup.enter="applyValidConcept"
				@blur="applyValidConcept"
			/>
			-->
		</template>
	</main>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import AutoComplete, { AutoCompleteCompleteEvent } from 'primevue/autocomplete';
import type { DKG, Grounding } from '@/types/Types';
import { getNameOfCurieCached, searchCuriesEntities } from '@/services/concept';

const grounding = defineModel<Grounding>();

defineProps<{
	isPreview?: boolean;
}>();

const selectedConcept = ref<DKG>();

const resultsDKG = ref<DKG[]>([]);
async function searchDKG(event: AutoCompleteCompleteEvent) {
	resultsDKG.value = await searchCuriesEntities(event.query);
}

// Used if an option isn't selected from the Autocomplete suggestions but is typed in regularly
function saveConcept() {
	if (selectedConcept.value?.curie) {
		const [key, value] = selectedConcept.value.curie.split(':');
		const identifiers = { [key]: value };
		grounding.value = { ...grounding.value, identifiers };
	}
}

watch(
	() => grounding.value,
	(newGrounding) => {
		if (newGrounding) {
			const [key, value] = Object.entries(newGrounding.identifiers)[0];
			const curie = `${key}:${value}`;
			getNameOfCurieCached(curie).then((name) => {
				selectedConcept.value = { name, curie, description: '' };
			});
		}
	},
	{ immediate: true }
);
</script>

<style scoped>
main {
	align-items: center;
	display: flex;
	gap: var(--gap-2);
}

h6 {
	color: var(--text-color-subdued);
	font-size: var(--font-caption);
	font-weight: var(--font-weight);
}
</style>
