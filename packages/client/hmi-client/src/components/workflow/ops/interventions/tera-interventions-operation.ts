import { InterventionPolicy } from '@/types/Types';
import { WorkflowOperationTypes } from '@/types/workflow';
import type { Operation, BaseState } from '@/types/workflow';

export interface InterventionsState extends BaseState {
	transientInterventionPolicy: InterventionPolicy;
}

export const InterventionsOperation: Operation = {
	name: WorkflowOperationTypes.INTERVENTIONS,
	description: 'Create interventions',
	displayName: 'Create intervention policy',
	isRunnable: true,
	inputs: [{ type: 'modelId', label: 'Model' }],
	outputs: [{ type: 'policyId', label: 'Interventions' }],
	action: () => {},

	initState: () => {
		const init: InterventionsState = {
			transientInterventionPolicy: {
				modelId: '',
				interventions: []
			}
		};
		return init;
	}
};
