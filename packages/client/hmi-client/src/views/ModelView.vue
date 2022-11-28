<script setup lang="ts">
import Model from '@/views/Model.vue';
import TabContainer from '@/components/tabs/TabContainer.vue';

const openModels = [
	{
		tabName: 'Model one',
		props: {
			modelId: '1'
		}
	}
];

openModels.push({
	tabName: 'Model two',
	props: {
		modelId: '2'
	}
});

const tabComponent = Model;
</script>

<template>
	<TabContainer :meta-content="openModels" :component-to-render="tabComponent"> </TabContainer>
</template>

<style scoped></style>
