<script setup lang="ts">
/**
 * A container to render a component in a tabbed view
 * @prop {Tab[]} tabs - array of tab data
 * @prop {Object} componentToRender - the component that you want to render as a tab
 * @prop {Object} icon - optional - an icon to display next to the name of each tab
 * @prop {number} activeTabIndex - tab to make active
 *
 * @typedef {Object} Tab
 * @property {string} tabName - name to display in tab header
 * @property {Object} props - optional - any props that should be passed into componentToRender
 *
 */
import { ref, computed, onBeforeUpdate, watch } from 'vue';
import TabComponent from '@/components/tabs/Tab.vue';
import { Tab } from '@/types/common';

const props = defineProps<{
	tabs: Tab[];
	componentToRender: Object;
	icon?: Object;
	activeTabIndex: number;
}>();

const emit = defineEmits(['closeTab']);

const newActiveTab = computed(() => props.activeTabIndex);
const currentActiveTab = ref(0);

function addKeysToTabs(tabs: Tab[]) {
	return tabs.map((tab, index) => ({
		name: tab.name,
		props: tab.props,
		tabKey: index
	}));
}

const tabsRef = ref(props.tabs);
// Add keys to each tab so that Vue can keep track of them
const keyedTabs = computed(() => addKeysToTabs(tabsRef.value));

function setActiveTab(tabIndex: number) {
	currentActiveTab.value = tabIndex;
}

function closeTab(tabIndexToClose: number) {
	const tabToClose = tabsRef.value[tabIndexToClose];
	const lastTabIndex = tabsRef.value.length - 1;
	tabsRef.value.splice(tabIndexToClose, 1);
	// If the tab that is closed is to the left of the active tab, decrement the active tab index by one, so that the active tab preserves its position.
	// E.g. if the active tab is the last tab, it will remain the last tab. If the active tab is second last, it will remain second last.
	// If the tab that is closed is to the right of the active tab, no special logic is needed to preserve the active tab's position.
	// If the active tab is closed, the next tab to the right becomes the active tab.
	// This replicates the tab behaviour in Chrome.
	if (
		(currentActiveTab.value !== 0 && currentActiveTab.value > tabIndexToClose) ||
		currentActiveTab.value === lastTabIndex
	) {
		currentActiveTab.value--;
	}
	emit('closeTab', tabToClose);
}

onBeforeUpdate(() => {
	tabsRef.value = props.tabs;
});

watch(newActiveTab, (index) => {
	setActiveTab(index);
});
</script>

<template>
	<!-- This div is so that child tabs can be positioned absolutely relative to the div -->
	<div>
		<TabComponent
			v-for="(tab, index) in keyedTabs"
			:name="tab.name"
			:index="index"
			:key="tab.tabKey"
			:isActive="currentActiveTab === index"
			:num-tabs="keyedTabs.length"
			@click-tab-header="(tabIndex) => setActiveTab(tabIndex)"
			@click-tab-close="(tabIndex) => closeTab(tabIndex)"
		>
			<template #tabIcon>
				<component :is="icon"></component>
			</template>
			<component class="tab-content" :is="componentToRender" v-bind="tab.props"></component>
		</TabComponent>
	</div>
</template>

<style scoped>
div {
	width: 100%;
}

.tab-content {
	height: 100%;
}
</style>
