<script setup lang="ts">
import { ref } from 'vue';
import Tab from '@/components/tabs/Tab.vue';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
const props = defineProps<{
	metaContent: [
		{
			tabName: string;
			props?: Object;
		}
	];
	componentToRender: Object;
	icon?: Object;
}>();

const activeTab = ref(1);
</script>

<template>
	<!-- This div is so that child tabs can be positioned absolutely relative to the div -->
	<div>
		<Tab
			v-for="(meta, index) in metaContent"
			:name="meta.tabName"
			:index="index"
			:key="index"
			:isActive="activeTab === index"
			@clicked-tab-header="(tabIndex) => (activeTab = tabIndex)"
		>
			<template #tabIcon>
				<component :is="icon"></component>
			</template>
			<component :is="componentToRender" v-bind="meta.props"></component>
		</Tab>
	</div>
</template>

<style scoped></style>
