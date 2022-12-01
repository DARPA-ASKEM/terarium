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
	},
	{
		name: 'Model four',
		props: {
			modelId: '4'
		}
	},
	{
		name: 'Model five',
		props: {
			modelId: '5'
		}
	},
	{
		name: 'Model six',
		props: {
			modelId: '6'
		}
	},
	{
		name: 'Model seven',
		props: {
			modelId: '7'
		}
	},
	{
		name: 'Model eight',
		props: {
			modelId: '8'
		}
	},
	{
		name: 'Model nine',
		props: {
			modelId: '9'
		}
	},
	{
		name: 'Model ten',
		props: {
			modelId: '10'
		}
	},
	{
		name: 'Model eleven',
		props: {
			modelId: '11'
		}
	},
	{
		name: 'Model twelve',
		props: {
			modelId: '12'
		}
	}
];
</script>

<template>
	<TabContainer :tabs="openModels" :component-to-render="Model" :icon="IconMachineLearningModel">
	</TabContainer>
</template>

<style scoped></style>
