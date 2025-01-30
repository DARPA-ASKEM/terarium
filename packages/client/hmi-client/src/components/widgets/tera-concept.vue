<template>
	<main>
		<h6>Concept</h6>
		<template v-if="isPreview">
			<p :title="selectedConcept?.curie">{{ selectedConcept?.name }}</p>
			<h6 v-if="!isEmpty(selectedAdditionalConcepts)">Additional concepts</h6>
			<p>{{ selectedAdditionalConcepts.map((dkg) => dkg.name).join(', ') }}</p>
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

			<!-- Three states of additional concepts buttons: Hide / Show / Add additional concepts -->
			<Button
				text
				size="small"
				:label="
					showAdditionalConcepts
						? 'Hide additional concepts'
						: isAdditionalConceptsEmpty
							? 'Add additional concepts'
							: 'Show additional concepts'
				"
				@click="showAdditionalConcepts = !showAdditionalConcepts"
			/>

			<AutoComplete
				v-if="showAdditionalConcepts"
				multiple
				optionLabel="name"
				placeholder="Additional concepts"
				size="small"
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
import { computed, ref, watch } from 'vue';
import AutoComplete, { AutoCompleteCompleteEvent } from 'primevue/autocomplete';
import type { DKG, Grounding } from '@/types/Types';
import {
	getDKGFromGroundingContext,
	getDKGFromGroundingIdentifier,
	parseCurieToIdentifier,
	parseListDKGToGroundingContext,
	searchCuriesEntities
} from '@/services/concept';
import { isEmpty } from 'lodash';
import Button from 'primevue/button';

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
const showAdditionalConcepts = ref(false);
const isAdditionalConceptsEmpty = computed(() => isEmpty(selectedAdditionalConcepts.value));
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
