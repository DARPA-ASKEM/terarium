<template>
	<tera-asset-card
		:asset="asset"
		:resourceType="resourceType"
		:active="isPreviewed"
		:highlight="searchTerm"
		@click="emit('toggle-asset-preview')"
	>
		<Button @click.stop="toggle" icon="pi pi-plus" text rounded />
		<Menu ref="menu" :model="projectOptions" :popup="true" />
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
	projectOptions: { label: string; items: MenuItem[] }[];
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
