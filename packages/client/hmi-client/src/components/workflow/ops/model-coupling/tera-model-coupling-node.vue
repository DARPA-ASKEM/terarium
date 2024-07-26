<template>
	<tera-operator-placeholder :node="node"> Attach two or more models </tera-operator-placeholder>
	<Button v-if="node.inputs[0].value" @click="emit('open-drilldown')" label="Edit" severity="secondary" outlined />
</template>

<script setup lang="ts">
import { watch } from 'vue';
import Button from 'primevue/button';
import { WorkflowNode } from '@/types/workflow';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';

const props = defineProps<{
	node: WorkflowNode<any>;
}>();
const emit = defineEmits(['open-drilldown', 'append-input-port', 'update-state']);

watch(
	() => props.node.inputs,
	() => {
		let createNewInputPort = true;
		const inputs = props.node.inputs;
		for (let i = 0; i < inputs.length; i++) {
			if (!inputs[i].value) {
				createNewInputPort = false;
			}
		}
		if (createNewInputPort) {
			emit('append-input-port', {
				type: 'modelId',
				label: 'Model'
			});
		}
	},
	{ deep: true }
);
</script>
