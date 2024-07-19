import API from '@/api/api';
import { InterventionPolicy } from '@/types/Types';

export const getInterventionPolicyById = async (policyId: string): Promise<InterventionPolicy> => {
	const response = await API.get<InterventionPolicy>(`/interventions/${policyId}`);
	return response?.data ?? null;
};

export const createInterventionPolicy = async (policy: InterventionPolicy): Promise<InterventionPolicy> => {
	delete policy.id;
	const response = await API.post<InterventionPolicy>(`/interventions`, policy);
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
