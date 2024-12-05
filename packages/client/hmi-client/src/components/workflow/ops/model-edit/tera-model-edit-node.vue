<template>
	<section>
		<tera-operator-model-preview v-if="model" :model="model" />
		<tera-operator-placeholder v-else :node="node">
			<template v-if="!node.inputs[0].value">Attach a model</template>
		</tera-operator-placeholder>
		<template v-if="node.inputs[0].value">
			<Button @click="emit('open-drilldown')" label="Edit" severity="secondary" outlined />
		</template>
	</section>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import Button from 'primevue/button';
import { WorkflowNode, WorkflowPortStatus } from '@/types/workflow';
import { ModelEditOperationState } from '@/components/workflow/ops/model-edit/model-edit-operation';
import { Model } from '@/types/Types';
import { getActiveOutput } from '@/components/workflow/util';
import TeraOperatorModelPreview from '@/components/operator/tera-operator-model-preview.vue';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import { getModel } from '@/services/model';

const emit = defineEmits(['append-input-port', 'open-drilldown']);

const props = defineProps<{
	node: WorkflowNode<ModelEditOperationState>;
}>();

const model = ref<Model | null>(null);

const fetchModel = async () => {
	model.value = null;
	const modelId = getActiveOutput(props.node)?.value?.[0];
	if (modelId) {
		model.value = await getModel(modelId);
	}
};

// Updates output selection
watch(
	() => props.node.active,
	() => {
		fetchModel();
	},
	{ immediate: true }
);
watch(
	() => props.node.inputs,
	() => {
		const inputs = props.node.inputs;
		if (inputs.every((input) => input.status === WorkflowPortStatus.CONNECTED) && inputs[0].type === 'modelId') {
			emit('append-input-port', { type: 'modelId', label: 'Model (optional)' });
		}
	},
	{ deep: true }
);
</script>
