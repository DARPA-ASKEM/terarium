<template>
	<section class="overview">
		<Accordion multiple :active-index="[0, 1]">
			<AccordionTab header="Description">
				<section class="description">
					<tera-show-more-text :text="description" :lines="5" />
				</section>
			</AccordionTab>
			<AccordionTab header="Provenance">
				<section class="provenance">
					<article v-if="!isEmpty(provenance)">
						<p v-html="provenance" />
					</article>
				</section>
			</AccordionTab>
		</Accordion>
		<section class="details">
			<ul></ul>
			<tera-related-documents
				:documents="documents"
				:asset-type="AssetType.Datasets"
				:assetId="dataset.id ?? ''"
				@enriched="fetchAsset"
			/>
		</section>
	</section>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';
import { computed } from 'vue';
import TeraRelatedDocuments from '@/components/widgets/tera-related-documents.vue';
import { AssetType, Dataset, DocumentAsset } from '@/types/Types';
import { AcceptedExtensions, FeatureConfig } from '@/types/common';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import TeraShowMoreText from '@/components/widgets/tera-show-more-text.vue';
import * as textUtil from '@/utils/text';
import { useProjects } from '@/composables/project';

const props = defineProps<{
	dataset: Dataset;
	highlight: string;
	featureConfig: FeatureConfig;
}>();

const emit = defineEmits(['fetch-dataset']);
const card = computed(() => {
	if (props.dataset.metadata?.card) {
		const cardWithUnknowns = props.dataset.metadata?.card;
		const cardWithUnknownsArr = Object.entries(cardWithUnknowns);

		for (let i = 0; i < cardWithUnknownsArr.length; i++) {
			const key = cardWithUnknownsArr[i][0];
			if (cardWithUnknowns[key] === 'UNKNOWN') {
				cardWithUnknowns[key] = null;
			}
		}
		return cardWithUnknowns;
	}
	return null;
});
const provenance = computed(() => card.value?.provenance ?? '');
const description = computed(() =>
	highlightSearchTerms(props.dataset?.description?.concat(' ', card.value?.description ?? ''))
);
const documents = computed(
	() =>
		useProjects()
			.activeProject.value?.assets?.documents?.filter((document: DocumentAsset) =>
				[AcceptedExtensions.PDF, AcceptedExtensions.TXT, AcceptedExtensions.MD].some(
					(extension) => {
						if (document.fileNames && !isEmpty(document.fileNames)) {
							return document.fileNames[0]?.endsWith(extension);
						}
						return false;
					}
				)
			)
			.map((document: DocumentAsset) => ({
				name: document.name,
				id: document.id
			})) ?? []
);

function highlightSearchTerms(text: string | undefined): string {
	if (!!props.highlight && !!text) {
		return textUtil.highlight(text, props.highlight);
	}
	return text ?? '';
}

function fetchAsset() {
	emit('fetch-dataset');
}
</script>

<style scoped>
.overview {
	display: flex;
	width: 100%;
	gap: var(--gap-large);
	& > * {
		flex: 1;
	}
}
</style>
