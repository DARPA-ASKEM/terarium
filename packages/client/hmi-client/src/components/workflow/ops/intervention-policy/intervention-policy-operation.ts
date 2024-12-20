import { InterventionPolicy } from '@/types/Types';
import type { BaseState, Operation } from '@/types/workflow';
import { WorkflowOperationTypes } from '@/types/workflow';
import { isEqual, omit } from 'lodash';

export interface InterventionPolicyState extends BaseState {
	interventionPolicy: InterventionPolicy;
	taskIds: string[];
	selectedCharts?: string[];
}

export const InterventionPolicyOperation: Operation = {
	name: WorkflowOperationTypes.INTERVENTION_POLICY,
	description: 'Create intervention policy',
	displayName: 'Create intervention policy',
	isRunnable: true,
	inputs: [
		{ type: 'modelId', label: 'Model' },
		{ type: 'documentId', label: 'Document', isOptional: true }
	],
	outputs: [{ type: 'policyInterventionId', label: 'Intervention Policy' }],
	action: () => {},

	initState: () => {
		const init: InterventionPolicyState = {
			interventionPolicy: {
				modelId: '',
				interventions: []
			},
			taskIds: []
		};
		return init;
	}
};

export const isInterventionPoliciesEqual = (
	originalPolicy: InterventionPolicy | null,
	newPolicy: InterventionPolicy | null
): boolean => {
	if (!originalPolicy || !newPolicy) return false;
	const keysToOmit = ['id', 'createdOn', 'updatedOn'];
	return isEqual(omit(originalPolicy, keysToOmit), omit(newPolicy, keysToOmit));
};

// check that the intervention policy values are equal
export const isInterventionPoliciesValuesEqual = (
	originalPolicy: InterventionPolicy | null,
	newPolicy: InterventionPolicy | null
): boolean => {
	if (!originalPolicy || !newPolicy) return false;
	if (originalPolicy.interventions.length !== newPolicy.interventions.length) return false;

	const notEqual = originalPolicy.interventions.some((intervention, index) => {
		if (!isEqual(intervention.staticInterventions, newPolicy.interventions[index].staticInterventions)) return true;
		if (!isEqual(intervention.dynamicInterventions, newPolicy.interventions[index].dynamicInterventions)) return true;
		return false;
	});
	return !notEqual;
};
