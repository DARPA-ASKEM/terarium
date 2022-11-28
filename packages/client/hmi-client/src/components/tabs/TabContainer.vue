<script setup lang="ts">
import Tab from '@/components/tabs/Tab.vue';

const props = defineProps<{
	metaContent: [
		{
			tabName: string;
			props?: Object;
		}
	];
	componentToRender: Object;
}>();

console.log(props.componentToRender);
</script>

<template>
	<Tab v-for="(meta, index) in metaContent" :name="meta.tabName" :index="index" :key="index">
		<component :is="componentToRender" v-bind="meta.props"></component>
	</Tab>
</template>

<style scoped></style>
