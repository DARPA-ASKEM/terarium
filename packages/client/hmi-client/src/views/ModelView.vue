<script setup lang="ts">
import { ref } from 'vue';
import Model from '@/components/models/Model.vue';
import TabContainer from '@/components/tabs/TabContainer.vue';
import IconMachineLearningModel from '@carbon/icons-vue/es/machine-learning-model/16';
import { Tab } from '@/types/common';

const openModels = ref<Tab[]>([
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
	},
	{
		tabName: 'New model'
	}
]);
</script>

<template>
	<TabContainer :tabs="openModels" :component-to-render="Model" :icon="IconMachineLearningModel">
	</TabContainer>
</template>

<style scoped></style>
