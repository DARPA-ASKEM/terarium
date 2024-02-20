<template>
	<section>
		<template v-if="!isEmpty(node.state)">
			<ul>
				<li v-for="config of modelConfigs" :key="config.id">
					{{ config.name }}
				</li>
			</ul>
			<Button label="Open" @click="emit('open-drilldown')" severity="secondary" outlined />
		</template>
		<template v-else>
			<tera-operator-placeholder :operation-type="node.operationType">
				Attach a model
			</tera-operator-placeholder>
		</template>
	</section>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';
import { watch, ref } from 'vue';
import { WorkflowNode } from '@/types/workflow';
import { ModelConfiguration } from '@/types/Types';
import { getModelConfigurations } from '@/services/model';
import Button from 'primevue/button';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import { ModelConfigOperationState } from './model-config-operation';

const props = defineProps<{
	node: WorkflowNode<ModelConfigOperationState>;
}>();
const emit = defineEmits(['append-output', 'open-drilldown']);

const modelConfigs = ref<ModelConfiguration[]>([]);

const refresh = async (modelId: string) => {
	modelConfigs.value = await getModelConfigurations(modelId);
	modelConfigs.value.forEach((config) => {
		emit('append-output', {
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
