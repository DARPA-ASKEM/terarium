<script setup lang="ts">
import Model, { ModelProps } from '@/components/models/Model.vue';
import TabContainer from '@/components/tabs/TabContainer.vue';
import { ref, watch, computed } from 'vue';
import { Tab } from '@/types/common';
import useResourcesStore from '@/stores/resources';
import { useTabStore } from '@/stores/tabs';

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
	tabStore.setTabs(tabContext, openTabs.value);
}

function getModelName(id: string): string | null {
	const currentModel = modelsInCurrentProject?.find((model) => model.id.toString() === id);
	if (currentModel) {
		return currentModel.name;
	}
	return null;
}

function setActiveTab(index: number) {
	activeTabIndex.value = index;
	tabStore.setActiveTabIndex(tabContext, index);
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
			tabStore.setTabs(tabContext, openTabs.value);
			activeTabIndex.value = openTabs.value.length - 1;
			tabStore.setActiveTabIndex(tabContext, activeTabIndex.value);
		} else {
			activeTabIndex.value = foundTabIndex;
			tabStore.setActiveTabIndex(tabContext, activeTabIndex.value);
		}
	}
});

const previousOpenTabs = tabStore.getTabs(tabContext);
if (previousOpenTabs) {
	openTabs.value = openTabs.value.concat(previousOpenTabs);
	setActiveTab(tabStore.getActiveTabIndex(tabContext));
}
</script>

<template>
	<TabContainer
		:tabs="Array.from(openTabs)"
		:component-to-render="Model"
		:active-tab="props.modelId"
		@tab-closed="(tab) => removeClosedTab(tab)"
		@tab-selected="(index) => setActiveTab(index)"
		:active-tab-index="activeTabIndex"
	>
	</TabContainer>
</template>

<style scoped></style>
