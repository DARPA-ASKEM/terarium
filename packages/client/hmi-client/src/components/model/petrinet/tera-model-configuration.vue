<template>
	<main>
		<div v-if="stratifiedModelType">Stratified configs (WIP)</div>
		<tera-stratified-model-configuration
			v-if="stratifiedModelType"
			:stratified-model-type="stratifiedModelType"
			:model="model"
			:feature-config="featureConfig"
			@new-model-configuration="emit('new-model-configuration')"
		/>
		<div v-if="stratifiedModelType"><br />All values</div>
		<tera-regular-model-configuration
			:model="model"
			:feature-config="featureConfig"
			@new-model-configuration="emit('new-model-configuration')"
		/>
	</main>
</template>

<script setup lang="ts">
import { watch, ref, onMounted, computed } from 'vue';
import { isEmpty, cloneDeep } from 'lodash';
// import { StratifiedModelType } from '@/model-representation/petrinet/petrinet-service';
import { ModelConfiguration, Model } from '@/types/Types';
import { getStratificationType } from '@/model-representation/petrinet/petrinet-service';
import {
	createModelConfiguration,
	// updateModelConfiguration,
	addDefaultConfiguration
} from '@/services/model-configurations';
import { getModelConfigurations } from '@/services/model';
import { FeatureConfig } from '@/types/common';
import TeraStratifiedModelConfiguration from './model-configurations/tera-stratified-model-configuration.vue';
import TeraRegularModelConfiguration from './model-configurations/tera-regular-model-configuration.vue';

const props = defineProps<{
	featureConfig: FeatureConfig;
	model: Model;
	calibrationConfig?: boolean;
}>();

const emit = defineEmits(['new-model-configuration', 'update-model-configuration']);

// const modelConfigInputValue = ref<string>('');
const modelConfigurations = ref<ModelConfiguration[]>([]);
const cellEditStates = ref<any[]>([]);
const extractions = ref<any[]>([]);
const openValueConfig = ref(false);
// const modalVal = ref({ id: '', configIndex: 0, nodeType: NodeType.State });

// const activeIndex = ref(0);
const configItems = ref<any[]>([]);

// const configurations = computed<Model[]>(
// 	() => modelConfigurations.value?.map((m) => m.configuration) ?? []
// );

const stratifiedModelType = computed(() => props.model && getStratificationType(props.model));

// const modelConfigInputValue = ref<string>('');
// const modelConfigurations = ref<ModelConfiguration[]>([]);
// const cellEditStates = ref<any[]>([]);
// const extractions = ref<any[]>([]);
// const openValueConfig = ref(false);
// const modalVal = ref({ id: '', configIndex: 0, nodeType: NodeType.State });

// const emit = defineEmits(['new-model-configuration', 'update-model-configuration']);

async function addModelConfiguration(config: ModelConfiguration) {
	await createModelConfiguration(
		props.model.id,
		`Copy of ${config.name}`,
		config.description as string,
		config.configuration
	);
	setTimeout(() => {
		emit('new-model-configuration');
		initializeConfigSpace();
	}, 800);
}

function resetCellEditing() {
	const row = { name: false };

	// Can't use fill here because the same row object would be referenced throughout the array
	const cellEditStatesArr = new Array(modelConfigurations.value.length);
	for (let i = 0; i < modelConfigurations.value.length; i++) cellEditStatesArr[i] = cloneDeep(row);
	cellEditStates.value = cellEditStatesArr;
}

async function initializeConfigSpace() {
	let tempConfigurations = await getModelConfigurations(props.model.id);

	configItems.value = tempConfigurations.map((config) => ({
		label: config.name,
		command: () => {
			addModelConfiguration(config);
		}
	}));

	// Ensure that we always have a "default config" model configuration
	if (isEmpty(tempConfigurations) || !tempConfigurations.find((d) => d.name === 'Default config')) {
		await addDefaultConfiguration(props.model);
		tempConfigurations = await getModelConfigurations(props.model.id);
	}

	modelConfigurations.value = tempConfigurations;

	resetCellEditing();

	openValueConfig.value = false;
	// modalVal.value = { id: '', configIndex: 0, nodeType: NodeType.State };
	extractions.value = [{ name: 'Default', value: '' }];
}
defineExpose({ initializeConfigSpace });

watch(
	() => props.model.id,
	() => initializeConfigSpace()
);

onMounted(() => {
	initializeConfigSpace();
});
</script>

<style scoped></style>
