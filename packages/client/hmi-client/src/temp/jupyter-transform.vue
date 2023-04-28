<template>
	{{ assetName }}
	{{ assetType }}
	<!-- <main> -->
	<chatty-input placeholder-message="How can I help you today?" />
	<!-- </main> -->
</template>

<script setup lang="ts">
// import SliderPanel from '@/components/widgets/slider-panel.vue';
import { IProject, ProjectAssetTypes } from '@/types/Project';
import ChattyInput from '@/components/widgets/chatty-input.vue';
import { ref, watch, computed } from 'vue';

console.log('Loaded');

// Asset props are extracted from route
const props = defineProps<{
	project: IProject;
	assetName?: string;
	assetId?: string;
	assetType?: ProjectAssetTypes;
}>();

watch(
	() => [
		props.assetId, // Once the route name changes, add/switch to another tab
		props.project
	],
	() => {
		console.log(props.project, props.assetId);
	}
);
</script>

<style scoped>
section {
	display: flex;
	flex-direction: column;
	flex: 1;
	overflow: auto;
}

.asset {
	padding-top: 1rem;
}

.p-tabmenu:deep(.p-tabmenuitem) {
	display: inline;
	max-width: 15rem;
}

.p-tabmenu:deep(.p-tabmenu-nav .p-tabmenuitem .p-menuitem-link) {
	padding: 1rem;
	text-decoration: none;
}

.p-tabmenu:deep(.p-menuitem-text) {
	height: 1rem;
	display: inline-block;
	overflow: hidden;
	text-overflow: ellipsis;
}
</style>
