<script setup lang="ts">
import Model from '@/components/models/Model.vue';
import TabContainer from '@/components/tabs/TabContainer.vue';
import { ref, watch, computed, onMounted } from 'vue';
import { Tab } from '@/types/common';
import useResourcesStore from '@/stores/resources';

interface ModelProps {
	assetId: string;
}

const props = defineProps<{
	assetId: string;
}>();

const resourcesStore = useResourcesStore();

const newModelId = computed(() => props.assetId);
const openTabs = ref<Tab[]>([]);
const activeTabIndex = ref(0);
const modelsInCurrentProject = resourcesStore.activeProjectAssets?.models;

function removeClosedTab(tab: Tab) {
	const tabIndexToRemove = openTabs.value.indexOf(tab);
	openTabs.value.splice(tabIndexToRemove, 1);
}

function getModelName(id: string): string | null {
	const currentModel = modelsInCurrentProject?.find((model) => model.id.toString() === id);
	if (currentModel) {
		return currentModel.name;
	}
	return null;
}

watch(newModelId, (id) => {
	const newTab = {
		name: getModelName(id),
		props: {
			assetId: id
		}
	} as Tab;
	// Would have loved to use a Set here instead of an array, but equality was not working
	const foundTabIndex = openTabs.value.findIndex((tab) => {
		const tabProps = tab.props as ModelProps;
		return tabProps.assetId === props.assetId;
	});
	if (foundTabIndex === -1) {
		openTabs.value.push(newTab);
		activeTabIndex.value = openTabs.value.length - 1;
	} else {
		activeTabIndex.value = foundTabIndex;
	}
});

onMounted(async () => {
	// If no model id provided in props, do not attempt to make inital tab
	if (props.assetId !== '') {
		const initialTab = {
			name: getModelName(props.assetId),
			props: {
				assetId: props.assetId
			}
		} as Tab;

		openTabs.value.push(initialTab);
	} // end if
});
</script>

<template>
	<TabContainer
		class="tab-container"
		:tabs="Array.from(openTabs)"
		:component-to-render="Model"
		:active-tab="props.assetId"
		@close-tab="(tab) => removeClosedTab(tab)"
		:active-tab-index="activeTabIndex"
	>
	</TabContainer>
</template>

<style scoped>
.tab-container {
	height: 100%;
}
</style>
