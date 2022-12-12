<script setup lang="ts">
/**
 * A container to render a component in a tabbed view
 * @prop {Tab[]} tabs - array of tab data
 * @prop {Object} componentToRender - the component that you want to render as a tab
 * @prop {Object} icon - optional - an icon to display next to the name of each tab
 * @prop {number} activeTabIndex - tab to make active
 * @prop {string} context -
 *
 * @typedef {Object} Tab
 * @property {string} tabName - name to display in tab header
 * @property {Object} props - optional - any props that should be passed into componentToRender
 *
 */
import { ref, computed, onBeforeUpdate, watch, onBeforeMount } from 'vue';
import TabComponent from '@/components/tabs/Tab.vue';
import { Tab } from '@/types/common';
import { useTabStore } from '@/stores/tabs';

const props = defineProps<{
	tabs: Tab[];
	componentToRender: Object;
	icon?: Object;
	activeTabIndex: number;
	context: string;
}>();

const emit = defineEmits(['closeTab']);

const newActiveTab = computed(() => props.activeTabIndex);
const currentActiveTab = ref(0);
const tabStore = useTabStore();

function addKeysToTabs(tabs: Tab[]) {
	return tabs.map((tab, key) => ({
		name: tab.name,
		props: tab.props,
		key
	}));
}

const tabsRef = ref(props.tabs);
// Add keys to each tab so that Vue can keep track of them
const keyedTabs = computed(() => addKeysToTabs(tabsRef.value));

function setActiveTab(tabIndex: number) {
	currentActiveTab.value = tabIndex;
	tabStore.setActiveTabIndex(props.context, tabIndex);
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
		currentActiveTab.value !== 0 &&
		(currentActiveTab.value > tabIndexToClose || currentActiveTab.value === lastTabIndex)
	) {
		setActiveTab(currentActiveTab.value - 1);
	}
	emit('closeTab', tabToClose);
}

function loadTabs() {
	const previousOpenTabs = tabStore.getTabs(props.context);
	if (previousOpenTabs) {
		tabsRef.value = previousOpenTabs;
	}
}

onBeforeUpdate(() => {
	loadTabs();
	tabsRef.value = props.tabs;
});

onBeforeMount(() => {
	loadTabs();
	setActiveTab(tabStore.getActiveTabIndex(props.context));
});

watch(newActiveTab, (index) => {
	// console.log('set active tab');
	setActiveTab(index);
});

watch(keyedTabs, () => {
	tabStore.setTabs(props.context, tabsRef.value);
});
</script>

<template>
	<!-- This div is so that child tabs can be positioned absolutely relative to the div -->
	<div>
		<TabComponent
			v-for="(tab, index) in keyedTabs"
			:name="tab.name"
			:index="index"
			:key="tab.key"
			:isActive="currentActiveTab === index"
			:num-tabs="keyedTabs.length"
			@click-tab-header="(tabIndex) => setActiveTab(tabIndex)"
			@click-tab-close="(tabIndex) => closeTab(tabIndex)"
		>
			<template #tabIcon>
				<component :is="icon"></component>
			</template>
			<component :is="componentToRender" v-bind="tab.props"></component>
		</TabComponent>
	</div>
</template>

<style scoped>
div {
	width: 100%;
}
</style>
