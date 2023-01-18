<script setup lang="ts">
import Dataset from '@/components/dataset/Dataset.vue';
import TabContainer from '@/components/tabs/TabContainer.vue';
import { ref, watch, computed } from 'vue';
import { Tab } from '@/types/common';
import useResourcesStore from '@/stores/resources';
import { Project } from '@/types/Project';
import { RouteName } from '@/router/routes';
import { useTabStore } from '@/stores/tabs';
import { isEmpty } from 'lodash';
import ResourcesList from '@/components/resources/resources-list.vue';

const props = defineProps<{
	assetId?: string;
	project: Project;
}>();

const resourcesStore = useResourcesStore();
const tabStore = useTabStore();

const newDatasetId = computed(() => props.assetId);
const openTabs = ref<Tab[]>([]);
const activeTabIndex = ref(0);
const datasetsInCurrentProject = resourcesStore.activeProjectAssets?.datasets;
const activeProject = resourcesStore.activeProject;
const tabContext = `dataset${activeProject?.id}`;

// @ts-ignore
// eslint-disable-next-line @typescript-eslint/no-unused-vars
tabStore.$subscribe((mutation, state) => {
	const savedTabs = state.tabMap.get(tabContext);
	if (savedTabs) {
		openTabs.value = savedTabs;
	}
	activeTabIndex.value = tabStore.getActiveTabIndex(tabContext);
});

function removeClosedTab(tabIndexToRemove: number) {
	tabStore.removeTab(tabContext, tabIndexToRemove);
}

function getDocumentName(id: string): string | null {
	console.log(datasetsInCurrentProject);
	const currentDocument = datasetsInCurrentProject?.find((doc) => doc.id.toString() === id);
	if (currentDocument) {
		return currentDocument.name;
	}
	return null;
}

function setActiveTab(index: number) {
	activeTabIndex.value = index;
	tabStore.setActiveTabIndex(tabContext, index);
}

watch(newDatasetId, (id) => {
	if (id) {
		const newTab = {
			name: getDocumentName(id),
			props: {
				assetId: id
			}
		} as Tab;
		// Would have loved to use a Set here instead of an array, but equality does not work as expected for objects
		const foundTabIndex = openTabs.value.findIndex((tab) => {
			const tabProps = tab.props as { assetId: string };
			return tabProps.assetId === props.assetId;
		});
		if (foundTabIndex === -1) {
			openTabs.value.push(newTab);
			tabStore.setTabs(tabContext, openTabs.value);
			tabStore.setActiveTabIndex(tabContext, openTabs.value.length - 1);
		} else {
			tabStore.setActiveTabIndex(tabContext, foundTabIndex);
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
		v-if="!isEmpty(Array.from(openTabs))"
		class="tab-container"
		:tabs="Array.from(openTabs)"
		:component-to-render="Dataset"
		:active-tab="props.assetId"
		@tab-closed="(tab) => removeClosedTab(tab)"
		@tab-selected="(index) => setActiveTab(index)"
		:active-tab-index="activeTabIndex"
	>
	</TabContainer>
	<section v-else class="recent-documents-page">
		<resources-list :project="props.project" :resource-route="RouteName.ModelRoute" />
	</section>
</template>

<style scoped>
.recent-documents-page {
	margin: 10px;
	display: flex;
	flex-direction: column;
	gap: 1rem;
	padding: 1rem;
	background: var(--un-color-body-surface-primary);
}

.tab-container {
	height: 100%;
}
</style>
