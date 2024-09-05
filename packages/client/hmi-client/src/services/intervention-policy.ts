import API, { getProjectIdFromUrl } from '@/api/api';
import type { Intervention, InterventionPolicy } from '@/types/Types';
import { InterventionSemanticType } from '@/types/Types';
import { activeProjectId } from '@/composables/activeProject';

export const blankIntervention: Intervention = {
	name: 'New Intervention',
	appliedTo: '',
	type: InterventionSemanticType.Parameter,
	staticInterventions: [{ timestep: Number.NaN, value: Number.NaN }],
	dynamicInterventions: []
};

export const getInterventionPolicyById = async (policyId: string): Promise<InterventionPolicy> => {
	const response = await API.get<InterventionPolicy>(`/interventions/${policyId}`);
	return response?.data ?? null;
};

export const createInterventionPolicy = async (policy: InterventionPolicy): Promise<InterventionPolicy> => {
	const projectId = activeProjectId.value || getProjectIdFromUrl();
	delete policy.id;
	delete policy.createdOn;
	delete policy.updatedOn;
	const response = await API.post<InterventionPolicy>(`/interventions?project-id=${projectId}`, policy);
	return response?.data ?? null;
};

export const deleteInterventionPolicy = async (policyId: string) => {
	const response = await API.delete(`/interventions/${policyId}`);
	return response?.data ?? null;
};

export const updateInterventionPolicy = async (policy: InterventionPolicy): Promise<InterventionPolicy> => {
	const response = await API.put(`/interventions/${policy.id}`, policy);
	return response?.data ?? null;
};
