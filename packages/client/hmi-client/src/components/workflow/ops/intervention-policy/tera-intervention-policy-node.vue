<template>
	<section>
		<tera-operator-placeholder :node="node" />
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
import { blankIntervention } from '@/services/intervention-policy';
import { InterventionPolicyState } from './intervention-policy-operation';

const emit = defineEmits(['open-drilldown', 'update-state']);
const props = defineProps<{
	node: WorkflowNode<InterventionPolicyState>;
}>();

const modelInput = props.node.inputs.find((input) => input.type === 'modelId');
const isModelInputConnected = computed(() => modelInput?.status === WorkflowPortStatus.CONNECTED);

watch(
	() => props.node.inputs,
	(inputs) => {
		const modelId = inputs.find((input) => input.type === 'modelId')?.value?.[0];
		if (!modelId) return;

		const state = cloneDeep(props.node.state);
		// Reset previous model cache
		state.interventionPolicy = {
			modelId,
			interventions: [blankIntervention]
		};
		emit('update-state', state);
	},
	{ deep: true }
);
</script>
