<template>
	<main>Model coupling</main>
	<Button
		v-if="node.inputs[0].value"
		@click="emit('open-drilldown')"
		label="Configure"
		severity="secondary"
		outlined
	/>
</template>

<script setup lang="ts">
import { watch } from 'vue';
import Button from 'primevue/button';
import { WorkflowNode } from '@/types/workflow';

const props = defineProps<{
	node: WorkflowNode<any>;
}>();
const emit = defineEmits([
	'open-drilldown',
	'append-input-port',
	'append-output-port',
	'update-state'
]);

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
