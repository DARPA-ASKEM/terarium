<script setup lang="ts">
import Model, { ModelProps } from '@/components/models/Model.vue';
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

const emit = defineEmits(['show-data-explorer']);

const resourcesStore = useResourcesStore();
const tabStore = useTabStore();

const newModelId = computed(() => props.assetId);
const openTabs = ref<Tab[]>([]);
const activeTabIndex = ref(0);
const modelsInCurrentProject = resourcesStore.activeProjectAssets?.models;
const activeProject = resourcesStore.activeProject;
const tabContext = `model${activeProject?.id}`;

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
				assetId: id
			}
		} as Tab;
		// Would have loved to use a Set here instead of an array, but equality does not work as expected for objects
		const foundTabIndex = openTabs.value.findIndex((tab) => {
			const tabProps = tab.props as ModelProps;
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
		:component-to-render="Model"
		:active-tab="props.assetId"
		@tab-closed="(tab) => removeClosedTab(tab)"
		@tab-selected="(index) => setActiveTab(index)"
		:active-tab-index="activeTabIndex"
	>
	</TabContainer>
	<section v-else class="recent-models-page">
		<resources-list
			:project="props.project"
			:resource-route="RouteName.ModelRoute"
			@show-data-explorer="emit('show-data-explorer')"
		/>
	</section>
</template>

<style scoped>
.recent-models-page {
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
