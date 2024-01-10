<template>
	<section>
		<template v-if="node.inputs[0].value">
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
import { WorkflowNode } from '@/types/workflow';
import Button from 'primevue/button';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import { ModelConfigOperationState } from './model-config-operation';

defineProps<{
	node: WorkflowNode<ModelConfigOperationState>;
}>();
const emit = defineEmits(['append-output-port', 'open-drilldown']);
</script>
