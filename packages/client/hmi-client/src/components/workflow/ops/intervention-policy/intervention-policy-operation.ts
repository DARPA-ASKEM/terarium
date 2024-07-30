import { InterventionPolicy } from '@/types/Types';
import { WorkflowOperationTypes } from '@/types/workflow';
import type { Operation, BaseState } from '@/types/workflow';

export interface InterventionPolicyState extends BaseState {
	interventionPolicy: InterventionPolicy;
}

export const InterventionPolicyOperation: Operation = {
	name: WorkflowOperationTypes.INTERVENTION_POLICY,
	description: 'Create intervention policy',
	displayName: 'Create intervention policy',
	isRunnable: true,
	inputs: [{ type: 'modelId', label: 'Model' }],
	outputs: [{ type: 'policyInterventionId', label: 'Intervention Policy' }],
	action: () => {},

	initState: () => {
		const init: InterventionPolicyState = {
			interventionPolicy: {
				modelId: '',
				interventions: []
			}
		};
		return init;
	}
};
