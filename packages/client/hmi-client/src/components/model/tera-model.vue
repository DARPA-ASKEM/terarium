<template>
	<tera-asset
		name="name"
		:feature-config="featureConfig"
		:is-naming-asset="false"
		:stretch-content="modelView === ModelView.MODEL"
	>
	</tera-asset>
</template>

<script setup lang="ts">
import { /* watch, onUpdated, */ PropType, ref, computed } from 'vue';
import TeraAsset from '@/components/asset/tera-asset.vue';
// import * as ProjectService from '@/services/project';
// import { createModel, getModel, getModelConfigurations, updateModel } from '@/services/model';
import { /* ResultType, */ FeatureConfig } from '@/types/common';
import { IProject } from '@/types/Project';

enum ModelView {
	DESCRIPTION,
	MODEL,
	NOTEBOOK
}

const props = defineProps({
	project: {
		type: Object as PropType<IProject> | null,
		default: null
	},
	assetId: {
		type: String,
		default: 'sir-model-id'
	},
	highlight: {
		type: String,
		default: ''
	},
	featureConfig: {
		type: Object as PropType<FeatureConfig>,
		default: { isPreview: false } as FeatureConfig
	}
});

const modelView = ref(ModelView.DESCRIPTION);

const existingModelNames = computed(() => {
	const modelNames: string[] = [];
	props.project.assets?.models.forEach((item) => {
		modelNames.push(item.name);
	});
	return modelNames;
});

console.log(existingModelNames);
</script>

<style scoped></style>
