<template>
	<main>
		<p>Concept</p>

		<section v-if="isPreview">
			<p>{{ identifierName }}</p>
			<ul>
				<li v-for="(context, index) in contextNames" :key="index">
					{{ context }}
				</li>
			</ul>
		</section>

		<section v-else>
			<AutoComplete size="small" placeholder="Search concepts" optionLabel="name" />
			<!--				v-model="query"-->
			<!--				:suggestions="results"-->
			<!--				@complete="async () => (results = await searchCuriesEntities(query))"-->
			<!--				@item-select="$emit('update-item', { key: 'concept', value: $event.value.curie })"-->
			<!--				@keyup.enter="applyValidConcept"-->
			<!--				@blur="applyValidConcept"-->
			<AutoComplete size="small" placeholder="Additional concepts" optionLabel="name" />
			<!--				v-model="query"-->
			<!--				:suggestions="results"-->
			<!--				@complete="async () => (results = await searchCuriesEntities(query))"-->
			<!--				@item-select="$emit('update-item', { key: 'concept', value: $event.value.curie })"-->
			<!--				@keyup.enter="applyValidConcept"-->
			<!--				@blur="applyValidConcept"-->
		</section>
	</main>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import AutoComplete from 'primevue/autocomplete';
import { type Grounding } from '@/types/Types';
import { getCurieFromGroundingIdentifier, getNameOfCurieCached } from '@/services/concept';

const grounding = defineModel<Grounding>();

defineProps<{
	isPreview?: boolean;
}>();

// const emit = defineEmits(['update']);

// Used if an option isn't selected from the Autocomplete suggestions but is typed in regularly
// function applyValidConcept() {
// 	// Allows to empty the concept
// 	if (query.value === '') {
// 		emit('update', { key: 'concept', value: '' });
// 	}
// 	// If what was typed was one of the results then choose that result
// 	else {
// 		const concept = results.value.find((result) => result.name === query.value);
// 		if (concept) {
// 			emit('update', { key: 'concept', value: concept.curie });
// 		}
// 	}
// }

const identifierName = ref<string>('');
const contextNames = ref<string[]>([]);

watch(
	() => grounding.value,
	(newGrounding) => {
		if (newGrounding) {
			getNameOfCurieCached(getCurieFromGroundingIdentifier(newGrounding.identifiers)).then((name) => {
				identifierName.value = name;
			});
			contextNames.value = Object.entries(newGrounding.context ?? {}).map(([key, value]) => `${key}: ${value}`);
		}
	},
	{ immediate: true }
);
</script>

<style scoped>
main > p {
	color: var(--text-color-subdued);
}
</style>
