<template>
	<div class="breakdown-pane-container">
		<ul>
			<li v-for="(asset, idx) in selectedSearchItems" class="cart-item" :key="idx">
				<asset-card
					:asset="(asset as DocumentType & Model & Dataset)"
					:resourceType="(getType(asset) as ResourceType)"
				>
					<button type="button" @click.stop="(e) => toggleContextMenu(e, idx)">
						<i class="pi pi-ellipsis-v" />
					</button>
					<Menu ref="contextMenu" :model="getMenuItemsForItem(asset)" :popup="true" />
				</asset-card>
			</li>
		</ul>
	</div>
</template>

<script setup lang="ts">
import { onMounted, PropType, ref } from 'vue';
import { isDataset, isModel, isDocument } from '@/utils/data-util';
import { ResourceType, ResultType } from '@/types/common';
import { Model } from '@/types/Model';
import { DocumentType } from '@/types/Document';
import { Project } from '@/types/Project';
import * as ProjectService from '@/services/project';
import { Dataset } from '@/types/Dataset';
import Menu from 'primevue/menu';
import AssetCard from '@/page/data-explorer/components/asset-card.vue';

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

const projectsList = ref<Project[]>([]);

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
		return (item as Model).type;
	}
	if (isDataset(item)) {
		return (item as Dataset).type;
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

<style lang="scss" scoped>
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
	padding: 0;
}

i {
	padding: 0.2rem;
	border-radius: var(--border-radius);
}

i:hover {
	cursor: pointer;
	background-color: hsla(0, 0%, 0%, 0.1);
	background-color: hsla(0, 0%, 0%, 0.1);
}
</style>
