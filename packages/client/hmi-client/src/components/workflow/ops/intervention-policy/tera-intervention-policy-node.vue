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
import { blankIntervention, createInterventionPolicy } from '@/services/intervention-policy';
import { getInterventionPoliciesForModel } from '@/services/model';
import { InterventionPolicy } from '@/types/Types';
import { InterventionPolicyOperation, InterventionPolicyState } from './intervention-policy-operation';

const emit = defineEmits(['open-drilldown', 'update-state', 'append-output']);
const props = defineProps<{
	node: WorkflowNode<InterventionPolicyState>;
}>();

const modelInput = props.node.inputs.find((input) => input.type === 'modelId');
const isModelInputConnected = computed(() => modelInput?.status === WorkflowPortStatus.CONNECTED);

watch(
	() => props.node.inputs,
	async (inputs) => {
		const state = cloneDeep(props.node.state);
		const modelId = inputs.find((input) => input.type === 'modelId')?.value?.[0];
		if (!modelId) return;

		let policies = await getInterventionPoliciesForModel(modelId);
		if (!policies[0]?.id) {
			const interventionPolicy: InterventionPolicy = {
				modelId,
				name: 'New Intervention Policy',
				interventions: [blankIntervention]
			};

			await createInterventionPolicy(interventionPolicy);
			policies = await getInterventionPoliciesForModel(modelId);
		}

		if (policies[0]?.id) {
			const policy = policies[0];
			state.interventionPolicy = policy;
			emit('update-state', state);
			emit('append-output', {
				type: InterventionPolicyOperation.outputs[0].type,
				label: policy.name,
				value: policy.id,
				isSelected: false,
				state
			});
		} else {
			state.interventionPolicy = {
				modelId,
				interventions: [blankIntervention]
			};
			emit('update-state', state);
		}
	},
	{ deep: true }
);
</script>
