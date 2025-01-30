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
			<AutoComplete
				size="small"
				placeholder="Additional concepts"
				optionLabel="name"
				multiple
				v-model="selectedAdditionalConcepts"
				:suggestions="resultsDKG"
				@complete="searchDKG"
				@item-select="saveAdditionalConcepts"
				@keyup.enter="saveAdditionalConcepts"
				@blur="saveAdditionalConcepts"
			/>
		</template>
	</main>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import AutoComplete, { AutoCompleteCompleteEvent } from 'primevue/autocomplete';
import type { DKG, Grounding } from '@/types/Types';
import {
	getDKGFromGroundingContext,
	getDKGFromGroundingIdentifier,
	parseCurieToIdentifier,
	parseListDKGToGroundingContext,
	searchCuriesEntities
} from '@/services/concept';

defineProps<{
	isPreview?: boolean;
}>();

// Define the v-model of the component
const grounding = defineModel<Grounding>();

const resultsDKG = ref<DKG[]>([]);
async function searchDKG(event: AutoCompleteCompleteEvent) {
	resultsDKG.value = await searchCuriesEntities(event.query);
}

const selectedConcept = ref<DKG>();
function saveConcept() {
	const identifiers = parseCurieToIdentifier(selectedConcept.value?.curie);
	grounding.value = { ...(grounding.value as Grounding), identifiers };
}

const selectedAdditionalConcepts = ref<DKG[]>([]);
function saveAdditionalConcepts() {
	const context = parseListDKGToGroundingContext(selectedAdditionalConcepts.value);
	grounding.value = { ...(grounding.value as Grounding), context };
}

watch(
	() => grounding.value,
	(newGrounding) => {
		if (newGrounding) {
			getDKGFromGroundingIdentifier(newGrounding.identifiers).then((dkg) => {
				selectedConcept.value = dkg;
			});

			getDKGFromGroundingContext(newGrounding.context).then((dkgList) => {
				selectedAdditionalConcepts.value = dkgList;
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
