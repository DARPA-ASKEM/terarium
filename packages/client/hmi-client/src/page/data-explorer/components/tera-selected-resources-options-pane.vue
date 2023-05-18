<template>
	<div class="breakdown-pane-container">
		<ul>
			<li v-for="(asset, idx) in selectedSearchItems" class="cart-item" :key="idx">
				<tera-asset-card
					:asset="(asset as Document & Model & Dataset)"
					:resourceType="(getType(asset) as ResourceType)"
				>
					<button type="button" @click.stop="(e) => toggleContextMenu(e, idx)">
						<i class="pi pi-ellipsis-v" />
					</button>
					<Menu ref="contextMenu" :model="getMenuItemsForItem(asset)" :popup="true" />
				</tera-asset-card>
			</li>
		</ul>
	</div>
</template>

<script setup lang="ts">
import { onMounted, PropType, ref } from 'vue';
import { isDataset, isModel, isDocument } from '@/utils/data-util';
import { ResourceType, ResultType } from '@/types/common';
import { Model } from '@/types/Model';
import { Document, Dataset } from '@/types/Types';
import { IProject } from '@/types/Project';
import * as ProjectService from '@/services/project';
import Menu from 'primevue/menu';
import TeraAssetCard from '@/page/data-explorer/components/tera-asset-card.vue';

defineProps({
	selectedSearchItems: {
		type: Array as PropType<ResultType[]>,
		required: true
	}
});

const emit = defineEmits([
	'toggle-data-item-selected',
	'find-related-content',
	'find-similar-content'
]);

const contextMenu = ref();

const projectsList = ref<IProject[]>([]);

const getMenuItemsForItem = (item: ResultType) => [
	{
		label: 'Remove',
		command: () => emit('toggle-data-item-selected', { item, type: 'selected' })
	},
	{
		label: 'Find related content',
		command: () => emit('find-related-content', { item, type: 'selected' })
	},
	{
		label: 'Find similar content', // only for documents
		command: () => emit('find-similar-content', { item, type: 'selected' })
	}
];

const getType = (item: ResultType) => {
	if (isModel(item)) {
		return ResourceType.MODEL;
	}
	if (isDataset(item)) {
		return ResourceType.DATASET;
	}
	if (isDocument(item)) {
		return ResourceType.XDD;
	}
	return ResourceType.ALL;
};

onMounted(async () => {
	const all = await ProjectService.getAll();
	if (all !== null) {
		projectsList.value = all;
	}
});

const toggleContextMenu = (event, idx: number) => {
	contextMenu.value[idx].toggle(event);
};
</script>

<style scoped>
.invalid-project {
	background-color: gray;
	cursor: not-allowed;
}

.add-selected-buttons {
	display: flex;
	flex-direction: column;
}

.add-selected-buttons button {
	margin-bottom: 5px;
	padding-top: 4px;
	padding-bottom: 4px;
}

.breakdown-pane-container {
	min-height: 0;
	display: flex;
	flex-direction: column;
}

.cart-item {
	border-bottom: 1px solid var(--surface-ground);
}

button {
	border: none;
	background-color: transparent;
	height: min-content;
}

i {
	padding: 0.25rem;
	border-radius: var(--border-radius-bigger);
	color: var(--text-color-subdued);
}

i:hover {
	cursor: pointer;
	color: var(--text-color-primary);
	background-color: var(--surface-hover);
}
</style>
