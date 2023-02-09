<script setup lang="ts">
import { DocumentType } from '@/types/Document';
import { Model } from '@/types/Model';
import { Dataset } from '@/types/Dataset';
import { isDocument, isDataset, isModel } from '@/utils/data-util';
import { ResultType, ResourceType } from '@/types/common';
import AssetCard from '@/views/data-explorer/data-explorer-components/asset-card.vue';

const props = defineProps<{
	asset: DocumentType & Model & Dataset;
	isPreviewed: boolean;
	resourceType: ResourceType;
	selectedSearchItems: ResultType[];
	searchTerm?: string;
}>();

const emit = defineEmits(['toggle-selected-asset', 'toggle-asset-preview']);

const isSelected = () =>
	props.selectedSearchItems.find((item) => {
		if (isDocument(item)) {
			const itemAsDocument = item as DocumentType;
			return itemAsDocument.title === props.asset.title;
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
	<AssetCard
		:asset="asset"
		:resourceType="resourceType"
		:active="isPreviewed"
		:highlight="searchTerm"
		@click="emit('toggle-asset-preview')"
	>
		<button type="button" @click.stop="emit('toggle-selected-asset')">
			<i class="pi pi-plus" v-show="!isSelected()" />
			<i class="pi pi-check checkmark-color" v-show="isSelected()" />
		</button>
	</AssetCard>
</template>

<style scoped>
button {
	border: none;
	background-color: transparent;
	height: min-content;
	padding: 0;
}

button i {
	padding: 0.2rem;
	border-radius: 3px;
	font-size: 14px;
}

button i:hover {
	cursor: pointer;
	background-color: hsla(0, 0%, 0%, 0.1);
}

.checkmark-color {
	color: var(--primary-color);
}
</style>
