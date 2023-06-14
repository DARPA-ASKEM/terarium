import API from '@/api/api';
import { ModelConfiguration } from '@/types/Types';

export const getModelConfigurations = async () => {
	const response = await API.get(`/model_configurations`);
	return (response?.data as ModelConfiguration[]) ?? null;
};

export const getModelConfigurationById = async (id: string) => {
	const response = await API.get(`/model_configurations/${id}`);
	return (response?.data as ModelConfiguration) ?? null;
};

export const createModelConfiguration = async (config: ModelConfiguration) => {
	const response = await API.post(`/model_configurations`, config);
	return response?.data ?? null;
};

export const updateModelConfiguration = async (config: ModelConfiguration) => {
	const response = await API.put(`/model_configurations/${config.id}`, config);
	return response?.data ?? null;
};
