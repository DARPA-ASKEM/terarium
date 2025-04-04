import API from '@/api/api';
import type { DynamicIntervention, Intervention, InterventionPolicy, StaticIntervention } from '@/types/Types';
import { InterventionSemanticType, InterventionValueType } from '@/types/Types';
import { logger } from '@/utils/logger';
import { v4 as uuidv4 } from 'uuid';

export const createBlankIntervention = (): Intervention => ({
	id: uuidv4(),
	name: 'New intervention',
	staticInterventions: [
		{
			timestep: Number.NaN,
			value: Number.NaN,
			type: InterventionSemanticType.Parameter,
			appliedTo: '',
			valueType: InterventionValueType.Value
		}
	],
	dynamicInterventions: []
});

export const getInterventionPolicyById = async (policyId: string): Promise<InterventionPolicy | null> => {
	const response = await API.get<InterventionPolicy>(`/interventions/${policyId}`);
	return response?.data ?? null;
};

export const createInterventionPolicy = async (
	policy: InterventionPolicy,
	skipCheck: boolean = false
): Promise<InterventionPolicy | null> => {
	try {
		delete policy.id;
		delete policy.createdOn;
		delete policy.updatedOn;
		const response = await API.post<InterventionPolicy>(`/interventions?skip-check=${skipCheck}`, policy);
		if (response.status !== 201) {
			return null;
		}
		return response.data ?? null;
	} catch (error) {
		logger.error(error);
		return null;
	}
};

export const deleteInterventionPolicy = async (policyId: string) => {
	const response = await API.delete(`/interventions/${policyId}`);
	return response?.data ?? null;
};

export const updateInterventionPolicy = async (policy: InterventionPolicy): Promise<InterventionPolicy> => {
	const response = await API.put(`/interventions/${policy.id}`, policy);
	return response?.data ?? null;
};

export function isInterventionStatic(individualIntervention: StaticIntervention | DynamicIntervention) {
	if ((individualIntervention as StaticIntervention).timestep) return true;
	return false;
}

// Flatten the intervention data for display in a graph
export const flattenInterventionData = (
	interventions: Intervention[]
): { name: string; value: number; time: number; appliedTo: string }[] =>
	interventions?.flatMap((intervention) =>
		intervention.staticInterventions.map((staticIntervention) => ({
			name: intervention.name,
			value: staticIntervention.value,
			time: staticIntervention.timestep,
			appliedTo: staticIntervention.appliedTo
		}))
	);
