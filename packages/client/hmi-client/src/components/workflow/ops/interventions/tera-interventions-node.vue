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
import { getInterventionPoliciesForModel } from '@/services/model';
import { cloneDeep, isEmpty } from 'lodash';
import { createInterventionPolicy } from '@/services/intervention-policy';
import { InterventionPolicy } from '@/types/Types';
import { InterventionsState } from './tera-interventions-operation';

const emit = defineEmits(['open-drilldown', 'update-state']);
const props = defineProps<{
	node: WorkflowNode<InterventionsState>;
}>();

const modelInput = props.node.inputs.find((input) => input.type === 'modelId');
const isModelInputConnected = computed(() => modelInput?.status === WorkflowPortStatus.CONNECTED);

watch(
	() => props.node.inputs,
	() => {
		const inputs = props.node.inputs;
		const modelInputs = inputs.filter((input) => input.type === 'modelId');

		if (!modelInputs[0].value) {
			// Reset previous model cache
			const state = cloneDeep(props.node.state);
			state.transientInterventionPolicy = {
				modelId: '',
				interventions: []
			};
			emit('update-state', state);
		}

		if (modelInputs?.[0]?.value?.[0]) {
			const modelId = modelInputs?.[0]?.value?.[0];
			getInterventionPoliciesForModel(modelId).then((policies) => {
				if (isEmpty(policies)) {
					const policy: InterventionPolicy = {
						modelId,
						name: 'New Policy',
						description: 'This is a new intervention policy.',
						interventions: []
					};
					createInterventionPolicy(policy);
				}
			});
		}
	},
	{ deep: true }
);
</script>

<style scoped></style>
