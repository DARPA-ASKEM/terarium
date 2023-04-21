<template>
	<TeraAssetCard
		:asset="asset"
		:resourceType="resourceType"
		:active="isPreviewed"
		:highlight="searchTerm"
		@click="emit('toggle-asset-preview')"
	>
		<Button
			:icon="`pi ${statusIcon}`"
			class="p-button-icon-only p-button-text p-button-rounded"
			@click.stop="emit('toggle-selected-asset')"
		/>
	</TeraAssetCard>
</template>

<script setup lang="ts">
import { DocumentType } from '@/types/Document';
import { computed } from 'vue';
import Button from 'primevue/button';
import { Model } from '@/types/Model';
import { Dataset } from '@/types/Dataset';
import { isDocument, isDataset, isModel } from '@/utils/data-util';
import { ResultType, ResourceType } from '@/types/common';
import TeraAssetCard from '@/page/data-explorer/components/tera-asset-card.vue';

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

const statusIcon = computed(() => (isSelected() ? 'pi-check' : 'pi-plus'));
</script>

<style scoped>
.p-button:deep(.pi-check) {
	color: var(--primary-color);
}
</style>
