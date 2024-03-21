<template>
	<Accordion multiple :active-index="[0, 1, 2, 3]">
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
		<AccordionTab header="Column information" v-if="!dataset?.esgfId">
			<tera-dataset-overview-table
				v-if="dataset"
				:dataset="dataset"
				@update-dataset="(dataset: Dataset) => emit('update-dataset', dataset)"
			/>
		</AccordionTab>
		<template v-if="dataset?.esgfId">
			<AccordionTab header="Preview">
				<img :src="image" alt="" />
			</AccordionTab>
			<AccordionTab header="Metadata">
				<div v-for="(value, key) in dataset.metadata" :key="key" class="row">
					<div class="col key">
						{{ snakeToCapitalized(key) }}
					</div>
					<div class="col">
						<template v-if="typeof value === 'object'">
							<ul>
								<li v-for="(item, index) in Object.values(value)" :key="index">
									{{ item }}
								</li>
							</ul>
						</template>
						<template v-else-if="Array.isArray(value)">
							<ul>
								<li v-for="(item, index) in value" :key="index">
									{{ item }}
								</li>
							</ul>
						</template>
						<template v-else>
							{{ value }}
						</template>
					</div>
				</div>
			</AccordionTab>
		</template>
	</Accordion>
</template>

<script setup lang="ts">
import { upperFirst, lowerCase } from 'lodash';
import { computed } from 'vue';
import TeraRelatedDocuments from '@/components/widgets/tera-related-documents.vue';
import type { Dataset, ProjectAsset } from '@/types/Types';
import { AssetType } from '@/types/Types';
import { FeatureConfig } from '@/types/common';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import TeraShowMoreText from '@/components/widgets/tera-show-more-text.vue';
import * as textUtil from '@/utils/text';
import { useProjects } from '@/composables/project';
import TeraDatasetOverviewTable from './tera-dataset-overview-table.vue';

const props = defineProps<{
	dataset: Dataset | null;
	image?: string;
	highlight?: string;
	featureConfig?: FeatureConfig;
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

const documents = computed<{ name: string; id: string }[]>(
	() =>
		useProjects()
			.getActiveProjectAssets(AssetType.Document)
			.map((projectAsset: ProjectAsset) => ({
				name: projectAsset.assetName,
				id: projectAsset.assetId
			})) ?? []
);

const author = computed(() => card.value?.AUTHOR_NAME ?? '');

function highlightSearchTerms(text: string | undefined): string {
	if (!!props.highlight && !!text) {
		return textUtil.highlight(text, props.highlight);
	}
	return text ?? '';
}

function snakeToCapitalized(snakeCaseString) {
	const words = snakeCaseString.split('_');
	return upperFirst(lowerCase(words.join(' ')));
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

.row {
	display: flex;
	justify-content: space-between;
	border-bottom: 1px solid var(--surface-border);
	padding: var(--gap-small) 0;
}

.key {
	font-weight: bold;
}

.col {
	flex: 1;
}
</style>
