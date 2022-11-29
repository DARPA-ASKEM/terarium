<script setup lang="ts">
import Model from '@/views/Model.vue';
import TabContainer from '@/components/tabs/TabContainer.vue';
import IconMachineLearningModel from '@carbon/icons-vue/es/machine-learning-model/16';

const mockOpenModels = [
	{
		tabName: 'Model one',
		props: {
			modelId: '1'
		}
	},
	{
		tabName: 'Model two',
		props: {
			modelId: '2'
		}
	},
	{
		tabName: 'Model three',
		props: {
			modelId: '3'
		}
	}
];

const tabComponent = Model;
</script>

<template>
	<TabContainer
		:meta-content="mockOpenModels"
		:component-to-render="tabComponent"
		:icon="IconMachineLearningModel"
	>
	</TabContainer>
</template>

<style scoped></style>
