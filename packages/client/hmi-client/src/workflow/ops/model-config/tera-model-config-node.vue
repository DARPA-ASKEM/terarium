<template>
	<ul>
		<li v-for="config of modelConfigs" :key="config.id">
			{{ config.name }}
		</li>
	</ul>
</template>

<script setup lang="ts">
import { watch, ref } from 'vue';
import { WorkflowNode } from '@/types/workflow';
import { ModelConfiguration } from '@/types/Types';
import { getModelConfigurations } from '@/services/model';
import { ModelConfigOperationState } from './model-config-operation';

const props = defineProps<{
	node: WorkflowNode<ModelConfigOperationState>;
}>();
const emit = defineEmits(['append-output-port']);

const modelConfigs = ref<ModelConfiguration[]>([]);

const refresh = async (modelId: string) => {
	modelConfigs.value = await getModelConfigurations(modelId);
	modelConfigs.value.forEach((config) => {
		emit('append-output-port', {
			type: 'modelConfigId',
			label: config.name,
			value: [config.id]
		});
	});
};

watch(
	() => [props.node.inputs[0].value],
	async () => {
		if (props.node.inputs[0].value) {
			const modelId = props.node.inputs[0].value[0];
			await refresh(modelId);
		}
	}
);
</script>
