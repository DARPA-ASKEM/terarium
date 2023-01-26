<script setup lang="ts">
import { watch, ref } from 'vue';
import { XDDArticle, XDDExtractionType } from '@/types/XDD';
import { Model } from '@/types/Model';
import { Dataset } from '@/types/Dataset';
import { isXDDArticle, isDataset, isModel } from '@/utils/data-util';
import { ResultType, ResourceType } from '@/types/common';
import AssetCard from '@/components/data-explorer/asset-card.vue';

const props = defineProps<{
	asset: XDDArticle & Model & Dataset;
	isPreviewed: boolean;
	isInCart: boolean;
	resourceType: ResourceType;
	selectedSearchItems: ResultType[];
	searchTerm?: string;
}>();

const emit = defineEmits(['toggle-selected-asset', 'toggle-asset-preview']);

const relatedAssetPage = ref<number>(0);

const chosenExtractionFilter = ref<XDDExtractionType | 'Asset'>('Asset');

// Reset page number on new search and when chosenExtractionFilter is changed
watch(
	() => [props.asset, chosenExtractionFilter.value],
	() => {
		relatedAssetPage.value = 0;
	}
);

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
		:highlight="searchTerm"
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
