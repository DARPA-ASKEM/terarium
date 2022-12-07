<script setup lang="ts">
import Model from '@/components/models/Model.vue';
import TabContainer from '@/components/tabs/TabContainer.vue';
import IconMachineLearningModel from '@carbon/icons-vue/es/machine-learning-model/16';
import { ref, watch, computed } from 'vue';
import { Tab } from '@/types/common';

interface ModelProps {
	modelId: string;
}

const props = defineProps<{
	modelId: string;
}>();

const initialTab = {
	name: props.modelId,
	props: {
		modelId: props.modelId
	}
} as Tab;

const newModelId = computed(() => props.modelId);
const openTabs = ref<Tab[]>([initialTab]);
const activeTabIndex = ref(0);

function removeClosedTab(tab: Tab) {
	const tabIndexToRemove = openTabs.value.indexOf(tab);
	openTabs.value.splice(tabIndexToRemove, 1);
}

watch(newModelId, (id) => {
	const newTab = {
		name: id,
		props: {
			modelId: id
		}
	} as Tab;
	// Would have loved to use a Set here instead of an array, but equality was not working
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
});
</script>

<template>
	<TabContainer
		class="tab-container"
		:tabs="Array.from(openTabs)"
		:component-to-render="Model"
		:icon="IconMachineLearningModel"
		:active-tab="props.modelId"
		:active-tab-index="activeTabIndex"
		@close-tab="(tab) => removeClosedTab(tab)"
	>
	</TabContainer>
</template>

<style scoped>
.tab-container {
	height: 100%;
}
</style>
