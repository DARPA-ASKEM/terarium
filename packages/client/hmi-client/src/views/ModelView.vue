<script setup lang="ts">
import { ref } from 'vue';
import Model from '@/components/models/Model.vue';
import TabContainer from '@/components/tabs/TabContainer.vue';
import IconMachineLearningModel from '@carbon/icons-vue/es/machine-learning-model/16';
import { Tab } from '@/types/common';

const openModels = ref<Tab[]>([
	{
		name: 'Model one',
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
	},
	{
		name: 'New model'
	}
]);
</script>

<template>
	<TabContainer :tabs="openModels" :component-to-render="Model" :icon="IconMachineLearningModel">
	</TabContainer>
</template>

<style scoped></style>
