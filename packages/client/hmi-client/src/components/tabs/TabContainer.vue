<script setup lang="ts">
import { ref, computed } from 'vue';
import Tab from '@/components/tabs/Tab.vue';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
const props = defineProps<{
	metaContent: [
		{
			tabName: string;
			tabKey: number;
			props?: Object;
		}
	];
	componentToRender: Object;
	icon?: Object;
}>();

const activeTab = ref(0);
const tabContent = computed(() => props.metaContent);

const emit = defineEmits(['closeTab']);

function setActiveTab(tabIndex: number) {
	activeTab.value = tabIndex;
}

function closeTab(tabIndexToClose: number) {
	tabContent.value.splice(tabIndexToClose, 1);
	// If the tab that is closed is to the left of the active tab, decrement the active tab index by one, so that the active tab preserves its position.
	// E.g. if the active tab is the last tab, it will remain the last tab. If the active tab is second last, it will remain second last.
	// If the tab that is closed is to the right of the active tab, no special logic is needed to preserve the active tab's position.
	// If the active tab is closed, the next tab to the right becomes the active tab.
	// This replicates the tab behaviour in Chrome.
	if (activeTab.value !== 0 && activeTab.value >= tabIndexToClose) {
		activeTab.value--;
	}
	emit('closeTab', tabIndexToClose);
}
</script>

<template>
	<!-- This div is so that child tabs can be positioned absolutely relative to the div -->
	<div>
		<Tab
			v-for="(tab, index) in tabContent"
			:name="tab.tabName"
			:index="index"
			:key="tab.tabKey"
			:isActive="activeTab === index"
			@click-tab-header="(tabIndex) => setActiveTab(tabIndex)"
			@click-tab-close="(tabIndex) => closeTab(tabIndex)"
		>
			<template #tabIcon>
				<component :is="icon"></component>
			</template>
			<component :is="componentToRender" v-bind="tab.props"></component>
		</Tab>
	</div>
</template>

<style scoped></style>
