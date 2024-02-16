<template>
	<Accordion multiple :active-index="[0, 1, 2]">
		<AccordionTab header="Description">
			<section class="description">
				<tera-show-more-text :text="description" :lines="5" />

				<template v-if="datasetType">
					<label class="p-text-secondary">Dataset type</label>
					<p>{{ datasetType }}</p>
				</template>
				<template v-if="author">
					<label class="p-text-secondary">Author</label>
					<p>{{ author }}</p>
				</template>
			</section>
		</AccordionTab>
		<!-- <AccordionTab header="Charts">
					TBD
				</AccordionTab> -->
		<AccordionTab header="Provenance">
			<tera-related-documents
				:documents="documents"
				:asset-type="AssetType.Dataset"
				:assetId="dataset?.id ?? ''"
				@enriched="fetchAsset"
			/>
		</AccordionTab>
		<AccordionTab header="Column Information">
			<tera-dataset-overview-table
				v-if="dataset"
				:dataset="dataset"
				@update-dataset="(dataset: Dataset) => emit('update-dataset', dataset)"
			/>
		</AccordionTab>
	</Accordion>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';
import { computed } from 'vue';
import TeraRelatedDocuments from '@/components/widgets/tera-related-documents.vue';
import type { CsvAsset, Dataset, DocumentAsset } from '@/types/Types';
import { AssetType } from '@/types/Types';
import { AcceptedExtensions, FeatureConfig } from '@/types/common';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import TeraShowMoreText from '@/components/widgets/tera-show-more-text.vue';
import * as textUtil from '@/utils/text';
import { useProjects } from '@/composables/project';
import TeraDatasetOverviewTable from './tera-dataset-overview-table.vue';

const props = defineProps<{
	dataset: Dataset | null;
	highlight?: string;
	featureConfig?: FeatureConfig;
	rawContent: CsvAsset | null;
}>();

const emit = defineEmits(['fetch-dataset', 'update-dataset']);
const card = computed(() => {
	if (props.dataset?.metadata?.data_card) {
		const cardWithUnknowns = props.dataset.metadata?.data_card;
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
const description = computed(() =>
	highlightSearchTerms(props.dataset?.description?.concat('\n', card.value?.DESCRIPTION ?? ''))
);
const datasetType = computed(() => card.value?.DATASET_TYPE ?? '');

const documents = computed(
	() =>
		useProjects()
			.getActiveProjectAssets(AssetType.Document)
			.filter((document: DocumentAsset) =>
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

const author = computed(() => card.value?.AUTHOR_NAME ?? '');

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
.description {
	display: flex;
	flex-direction: column;
	gap: var(--gap-small);
	margin-left: 1.5rem;
}
</style>
