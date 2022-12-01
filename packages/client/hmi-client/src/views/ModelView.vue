<script setup lang="ts">
import Model from '@/components/models/Model.vue';
import TabContainer from '@/components/tabs/TabContainer.vue';
import IconMachineLearningModel from '@carbon/icons-vue/es/machine-learning-model/16';

const openModels = [
	{
		name: 'A really, really long model name',
		props: {
			modelId: '1'
		}
	},
	{
		name: 'Model two',
		props: {
			modelId: '2'
		}
	},
	{
		name: 'Model three',
		props: {
			modelId: '3'
		}
	}
];
</script>

<template>
	<TabContainer :tabs="openModels" :component-to-render="Model" :icon="IconMachineLearningModel">
	</TabContainer>
</template>

<style scoped></style>
