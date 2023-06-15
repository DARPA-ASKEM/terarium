import API from '@/api/api';
import { ModelConfiguration, Model } from '@/types/Types';

export const getModelConfigurations = async () => {
	const response = await API.get(`/model_configurations`);
	return (response?.data as ModelConfiguration[]) ?? null;
};

export const getModelConfigurationById = async (id: string) => {
	const response = await API.get(`/model_configurations/${id}`);
	return (response?.data as ModelConfiguration) ?? null;
};

export const createModelConfiguration = async (
	name: string,
	description: string,
	model_id: string,
	configuration: Model
) => {
	const response = await API.post(`/model_configurations`, {
		model_id,
		name,
		description,
		configuration
	});
	return response?.data ?? null;
};

export const updateModelConfiguration = async (config: ModelConfiguration) => {
	const response = await API.put(`/model_configurations/${config.id}`, config);
	return response?.data ?? null;
};
