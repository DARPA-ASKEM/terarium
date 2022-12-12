<script setup lang="ts">
import Model from '@/components/models/Model.vue';
import TabContainer from '@/components/tabs/TabContainer.vue';
import { ref, watch, computed } from 'vue';
import { Tab } from '@/types/common';
import useResourcesStore from '@/stores/resources';
import { useTabStore } from '@/stores/tabs';

interface ModelProps {
	modelId: string;
}

const props = defineProps<{
	modelId?: string;
}>();

const resourcesStore = useResourcesStore();
const tabStore = useTabStore();

const newModelId = computed(() => props.modelId);
const openTabs = ref<Tab[]>([]);
const activeTabIndex = ref(0);
const modelsInCurrentProject = resourcesStore.activeProjectAssets?.models;
const activeProject = resourcesStore.activeProject;
const tabContext = `model${activeProject?.id}`;

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
	if (id) {
		const newTab = {
			name: getModelName(id),
			props: {
				modelId: id
			}
		} as Tab;
		// Would have loved to use a Set here instead of an array, but equality does not work as expected for objects
		const foundTabIndex = openTabs.value.findIndex((tab) => {
			const tabProps = tab.props as ModelProps;
			return tabProps.modelId === props.modelId;
		});
		if (foundTabIndex === -1) {
			openTabs.value.push(newTab);
			activeTabIndex.value = openTabs.value.length - 1;
		} else {
			activeTabIndex.value = foundTabIndex;
		}
	}
});

const previousOpenTabs = tabStore.getTabs(tabContext);
if (previousOpenTabs) {
	openTabs.value = openTabs.value.concat(previousOpenTabs);
}
</script>

<template>
	<TabContainer
		:tabs="Array.from(openTabs)"
		:component-to-render="Model"
		:active-tab="props.modelId"
		@close-tab="(tab) => removeClosedTab(tab)"
		:active-tab-index="activeTabIndex"
		:context="tabContext"
	>
	</TabContainer>
</template>

<style scoped></style>
