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
import { ref, computed, onBeforeUpdate, watch, onBeforeMount } from 'vue';
import TabComponent from '@/components/tabs/Tab.vue';
import { Tab } from '@/types/common';

const props = defineProps<{
	tabs: Tab[];
	componentToRender: Object;
	icon?: Object;
	activeTabIndex: number;
}>();

const emit = defineEmits<{
	(e: 'tabClosed', index: number): void;
	(e: 'tabSelected', index: number): void;
}>();

const newActiveTab = computed(() => props.activeTabIndex);
const currentActiveTab = ref(0);

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
	emit('tabSelected', tabIndex);
}

function closeTab(tabIndexToClose: number) {
	emit('tabClosed', tabIndexToClose);
}

onBeforeUpdate(() => {
	tabsRef.value = props.tabs;
});

onBeforeMount(() => {
	setActiveTab(props.activeTabIndex);
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
			:key="tab.key"
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
