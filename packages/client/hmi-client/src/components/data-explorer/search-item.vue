<script setup lang="ts">
import { watch, ref, computed, ComputedRef } from 'vue';
import { isEmpty } from 'lodash';
import { XDDArticle, XDDArtifact, XDDUrlExtraction, XDDExtractionType } from '@/types/XDD';
import { Model } from '@/types/Model';
import { Dataset } from '@/types/Dataset';
import { isXDDArticle, isDataset, isModel } from '@/utils/data-util';
import { ResultType, ResourceType } from '@/types/common';
import AssetCard from '@/components/data-explorer/asset-card.vue';

// This type is for easy frontend integration with the rest of the extraction types (just for use here)
type UrlExtraction = {
	askemClass: XDDExtractionType;
	urlExtraction: XDDUrlExtraction;
};

const props = defineProps<{
	asset: XDDArticle & Model & Dataset;
	isPreviewed: boolean;
	isInCart: boolean;
	resourceType: ResourceType;
	selectedSearchItems: ResultType[];
}>();

const emit = defineEmits(['toggle-selected-asset', 'toggle-asset-preview']);

const relatedAssetPage = ref<number>(0);

const chosenExtractionFilter = ref<XDDExtractionType | 'Asset'>('Asset');

const urlExtractions = computed(() => {
	const urls: UrlExtraction[] = [];

	if (props.asset.relatedExtractions) {
		const documentsWithUrls = props.asset.relatedExtractions.filter(
			(ex) =>
				ex.askemClass === XDDExtractionType.Document &&
				ex.properties.documentBibjson.knownEntities !== undefined &&
				!isEmpty(ex.properties.documentBibjson.knownEntities.urlExtractions)
		);

		for (let i = 0; i < documentsWithUrls.length; i++) {
			const knownEntities = documentsWithUrls[i].properties.documentBibjson.knownEntities;

			if (knownEntities) {
				for (let j = 0; i < knownEntities.urlExtractions.length; j++) {
					urls.push({
						askemClass: XDDExtractionType.URL,
						urlExtraction: knownEntities.urlExtractions[j]
					});
				}
			}
		}
	}
	return urls;
});

const extractions: ComputedRef<UrlExtraction[] & XDDArtifact[]> = computed(() => {
	if (props.asset.relatedExtractions) {
		const allExtractions = [
			...(props.asset.relatedExtractions as UrlExtraction[] & XDDArtifact[]),
			...(urlExtractions.value as UrlExtraction[] & XDDArtifact[])
		];

		if (chosenExtractionFilter.value === 'Asset') return allExtractions;

		return allExtractions.filter((ex) => ex.askemClass === chosenExtractionFilter.value);
	}
	return [];
});

const relatedAsset = computed(() => extractions.value[relatedAssetPage.value]);
const snippets = computed(() => props.asset.highlight && [...props.asset.highlight].splice(0, 3));

// Reset page number on new search and when chosenExtractionFilter is changed
watch(
	() => [props.asset, chosenExtractionFilter.value],
	() => {
		relatedAssetPage.value = 0;
	}
);

function previewMovement(movement: number) {
	const newPage = relatedAssetPage.value + movement;
	if (newPage > -1 && newPage < extractions.value.length) {
		relatedAssetPage.value = newPage;
	}
	// console.log(relatedAsset.value);
}

function updateExtractionFilter(extractionType: XDDExtractionType) {
	chosenExtractionFilter.value =
		chosenExtractionFilter.value === extractionType ? 'Asset' : extractionType;
}

const isSelected = () =>
	props.selectedSearchItems.find((item) => {
		if (isXDDArticle(item)) {
			const itemAsArticle = item as XDDArticle;
			return itemAsArticle.title === props.asset.title;
		}
		if (isDataset(item)) {
			const itemAsDataset = item as Dataset;
			return itemAsDataset.id === props.asset.id;
		}
		if (isModel(item)) {
			const itemAsModel = item as Model;
			return itemAsModel.id === props.asset.id;
		}
		return false;
	});
</script>

<template>
	<asset-card
		:asset="asset"
		:resourceType="resourceType"
		:active="isPreviewed"
		@click="emit('toggle-asset-preview')"
	>
		<button type="button" v-if="isInCart">
			<!--there are talks of having the plus and three dot menu available wherever-->
			<i class="pi pi-ellipsis-v"></i>
		</button>
		<button v-else type="button" @click.stop="emit('toggle-selected-asset')">
			<i class="pi pi-plus" v-show="!isSelected()"></i>
			<i class="pi pi-check checkmark-color" v-show="isSelected()"></i>
		</button>
	</asset-card>
</template>

<style scoped>
button {
	border: none;
	background-color: transparent;
	height: min-content;
	padding: 0;
}

i {
	padding: 0.2rem;
	border-radius: 3px;
	z-index: 2;
	font-size: 12px;
}

.preview-and-options button i {
	font-size: 14px;
}

.pi[active='true'] {
	background-color: var(--primary-color-light);
}

i:hover {
	cursor: pointer;
	background-color: hsla(0, 0%, 0%, 0.1);
}

.checkmark-color {
	color: var(--primary-color);
}
</style>
