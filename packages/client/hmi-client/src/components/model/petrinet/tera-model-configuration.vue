<template>
	<main></main>
</template>

<script setup lang="ts">
// import { watch, onMounted } from 'vue';
// import { StratifiedModelType } from '@/model-representation/petrinet/petrinet-service';
// import { ModelConfiguration, Model } from '@/types/Types';
// import { FeatureConfig } from '@/types/common';

// const props = defineProps<{
// 	featureConfig: FeatureConfig;
// 	model: Model;
// 	stratifiedModelType: StratifiedModelType;
// 	calibrationConfig?: boolean;
// }>();

// const modelConfigInputValue = ref<string>('');
// const modelConfigurations = ref<ModelConfiguration[]>([]);
// const cellEditStates = ref<any[]>([]);
// const extractions = ref<any[]>([]);
// const openValueConfig = ref(false);
// const modalVal = ref({ id: '', configIndex: 0, nodeType: NodeType.State });

// const emit = defineEmits(['new-model-configuration', 'update-model-configuration']);

// async function initializeConfigSpace() {
// 	let tempConfigurations = await getModelConfigurations(props.model.id);

// 	configItems.value = tempConfigurations.map((config) => ({
// 		label: config.name,
// 		command: () => {
// 			addModelConfiguration(config);
// 		}
// 	}));

// 	// Ensure that we always have a "default config" model configuration
// 	if (isEmpty(tempConfigurations) || !tempConfigurations.find((d) => d.name === 'Default config')) {
// 		await addDefaultConfiguration(props.model);
// 		tempConfigurations = await getModelConfigurations(props.model.id);
// 	}

// 	modelConfigurations.value = tempConfigurations;

// 	resetCellEditing();

// 	openValueConfig.value = false;
// 	modalVal.value = { id: '', configIndex: 0, nodeType: NodeType.State };
// 	extractions.value = [{ name: 'Default', value: '' }];
// }
// defineExpose({ initializeConfigSpace });

// watch(
// 	() => props.model.id,
// 	() => initializeConfigSpace()
// );

// onMounted(() => {
// 	initializeConfigSpace();
// });
</script>

<style scoped></style>
