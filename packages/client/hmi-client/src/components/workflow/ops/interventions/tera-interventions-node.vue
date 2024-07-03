<template>
	<section>
		<tera-operator-placeholder :operation-type="node.operationType" />
		<Button
			:label="isModelInputConnected ? 'Open' : 'Attach a model'"
			@click="emit('open-drilldown')"
			severity="secondary"
			outlined
			:disabled="!isModelInputConnected"
		/>
	</section>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue';
import { WorkflowNode, WorkflowPortStatus } from '@/types/workflow';
import Button from 'primevue/button';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import { cloneDeep } from 'lodash';
import { InterventionsState } from './tera-interventions-operation';

const emit = defineEmits(['open-drilldown', 'update-state']);
const props = defineProps<{
	node: WorkflowNode<InterventionsState>;
}>();

const modelInput = props.node.inputs.find((input) => input.type === 'modelId');
const isModelInputConnected = computed(() => modelInput?.status === WorkflowPortStatus.CONNECTED);

watch(
	() => props.node.inputs,
	(inputs) => {
		const modelId = inputs.find((input) => input.type === 'modelId')?.value?.[0];
		const state = cloneDeep(props.node.state);

		if (!modelId) {
			// Reset previous model cache
			state.interventionPolicy = {
				modelId: '',
				interventions: []
			};
		} else {
			state.interventionPolicy = {
				modelId,
				interventions: []
			};
		}
		emit('update-state', state);
	},
	{ deep: true }
);
</script>

<style scoped></style>
