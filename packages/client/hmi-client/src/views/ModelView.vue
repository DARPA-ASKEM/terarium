<script setup lang="ts">
import { ref } from 'vue';
import Model from '@/components/models/Model.vue';
import TabContainer from '@/components/tabs/TabContainer.vue';
import IconMachineLearningModel from '@carbon/icons-vue/es/machine-learning-model/16';

const mockOpenModels = ref([
	{
		tabName: 'Model one',
		tabKey: 1,
		props: {
			modelId: '1'
		}
	},
	{
		tabName: 'Model two',
		tabKey: 2,
		props: {
			modelId: '2'
		}
	},
	{
		tabName: 'Model three',
		tabKey: 3,
		props: {
			modelId: '3'
		}
	}
]);
</script>

<template>
	<TabContainer
		:meta-content="mockOpenModels"
		:component-to-render="Model"
		:icon="IconMachineLearningModel"
		@close-tab="(tabIndex) => mockOpenModels.splice(tabIndex, 1)"
	>
	</TabContainer>
</template>

<style scoped></style>
