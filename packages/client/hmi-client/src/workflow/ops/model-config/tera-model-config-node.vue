<template>
	<section>
		<template v-if="node.inputs[0].value">
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
import { WorkflowNode, WorkflowPortStatus } from '@/types/workflow';
import Button from 'primevue/button';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import { watch } from 'vue';
import { ModelConfigOperationState } from './model-config-operation';

const props = defineProps<{
	node: WorkflowNode<ModelConfigOperationState>;
}>();
const emit = defineEmits(['open-drilldown', 'append-input-port']);

watch(
	() => props.node.inputs,
	() => {
		if (
			props.node.inputs
				.filter((input) => input.type === 'datasetId')
				.every((input) => input.status === WorkflowPortStatus.CONNECTED)
		) {
			emit('append-input-port', { type: 'datasetId', label: 'Dataset', isOptional: true });
		}
	},
	{ deep: true }
);
</script>
