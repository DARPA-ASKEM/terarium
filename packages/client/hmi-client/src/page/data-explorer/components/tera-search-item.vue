<template>
	<tera-asset-card
		:asset="asset"
		:resourceType="resourceType"
		:active="isPreviewed"
		:source="source"
		:highlight="searchTerm"
		@click="emit('toggle-asset-preview')"
	>
		<Button @click.stop="toggle" icon="pi pi-plus" text rounded :loading="isAddingAsset" />
		<Menu class="search-item-menu" ref="menu" :model="projectOptions" :popup="true" />
	</tera-asset-card>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import Button from 'primevue/button';
import Menu from 'primevue/menu';
import { ResultType, ResourceType } from '@/types/common';
import TeraAssetCard from '@/page/data-explorer/components/tera-asset-card.vue';
import { MenuItem } from 'primevue/menuitem';

defineProps<{
	asset: ResultType;
	source: string;
	projectOptions: { label: string; items: MenuItem[] }[];
	isAddingAsset: boolean;
	isPreviewed: boolean;
	resourceType: ResourceType;
	searchTerm?: string;
}>();

const emit = defineEmits(['select-asset', 'toggle-asset-preview']);

const menu = ref();
const toggle = (event: Event) => {
	menu.value.toggle(event);
	emit('select-asset');
};
</script>

<style scoped>
.p-button:deep(.pi-check) {
	color: var(--primary-color);
}
</style>
