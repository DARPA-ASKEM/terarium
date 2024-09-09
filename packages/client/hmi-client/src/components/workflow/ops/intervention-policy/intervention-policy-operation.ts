import { InterventionPolicy } from '@/types/Types';
import { WorkflowOperationTypes } from '@/types/workflow';
import type { Operation, BaseState } from '@/types/workflow';
import { isEqual, omit } from 'lodash';

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
		if (intervention.appliedTo !== newPolicy.interventions[index].appliedTo) return true;
		if (intervention.type !== newPolicy.interventions[index].type) return true;
		if (!isEqual(intervention.staticInterventions, newPolicy.interventions[index].staticInterventions)) return true;
		if (!isEqual(intervention.dynamicInterventions, newPolicy.interventions[index].dynamicInterventions)) return true;
		return false;
	});
	return !notEqual;
};
