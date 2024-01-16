<template>
	<div>
		<tera-columnar-panel>
			<Accordion multiple :active-index="[0, 1]">
				<AccordionTab header="Description">
					<section class="description">
						<tera-show-more-text :text="description" :lines="5" />
					</section>
				</AccordionTab>
				<AccordionTab v-if="!isEmpty(provenance)" header="Provenance">
					<section class="provenance">
						<article>
							<p v-html="provenance" />
						</article>
					</section>
				</AccordionTab>
			</Accordion>
			<section class="details-column">
				<tera-grey-card class="details">
					<ul>
						<li class="multiple">
							<span>
								<label>Rows</label>
								<div class="framework">{{ rawContent?.rowCount }}</div>
							</span>
							<span>
								<label>Columns</label>
								<div>{{ rawContent?.stats?.length }}</div>
							</span>
							<span>
								<label>Date created</label>
								<div>{{ new Date(dataset?.timestamp as Date).toLocaleString('en-US') }}</div>
							</span>
						</li>
						<li>
							<label>Created by</label>
							<div><tera-show-more-text v-if="authors" :text="authors" :lines="2" /></div>
						</li>
						<li>
							<label>Source Name</label>
							<div>{{ card?.authorEmail }}</div>
						</li>
						<li>
							<label>Source URL</label>
							<div>{{ dataset?.metadata?.processed_by }}</div>
						</li>
					</ul>
				</tera-grey-card>
				<tera-related-documents
					:documents="documents"
					:asset-type="AssetType.Datasets"
					:assetId="dataset?.id ?? ''"
					@enriched="fetchAsset"
				/>
			</section>
		</tera-columnar-panel>
		<h4>Column Information</h4>
		<tera-dataset-overview-table v-if="dataset?.columns" :data="dataset.columns" />
	</div>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';
import { computed } from 'vue';
import TeraRelatedDocuments from '@/components/widgets/tera-related-documents.vue';
import type { AssetType, CsvAsset, Dataset, DocumentAsset } from '@/types/Types';
import { AcceptedExtensions, FeatureConfig } from '@/types/common';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import TeraShowMoreText from '@/components/widgets/tera-show-more-text.vue';
import * as textUtil from '@/utils/text';
import { useProjects } from '@/composables/project';
import TeraGreyCard from '@/components/widgets/tera-grey-card.vue';
import TeraColumnarPanel from '../widgets/tera-columnar-panel.vue';
import TeraDatasetOverviewTable from './tera-dataset-overview-table.vue';

const props = defineProps<{
	dataset: Dataset | null;
	highlight?: string;
	featureConfig?: FeatureConfig;
	rawContent: CsvAsset | null;
}>();

const emit = defineEmits(['fetch-dataset']);
const card = computed(() => {
	if (props.dataset?.metadata?.card) {
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

const authors = computed(() => {
	const authorsArray = props.dataset?.metadata?.annotations?.authors ?? [];

	if (card.value?.authorAuthor) authorsArray.unshift(card.value?.authorAuthor);

	return authorsArray.join(', ');
});

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
.details-column {
	display: flex;
	flex-direction: column;
	gap: var(--gap-small);
	> .details {
		> ul {
			list-style: none;
			padding: 0.5rem 1rem;
			display: flex;
			flex-direction: column;
			gap: var(--gap-small);

			& > li.multiple {
				display: flex;

				& > span {
					flex: 1 0 0;
				}
			}

			& > li label {
				color: var(--text-color-subdued);
				font-size: var(--font-caption);

				& + *:empty:before {
					content: '--';
				}
			}
		}
	}
}
</style>
