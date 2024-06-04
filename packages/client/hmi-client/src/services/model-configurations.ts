import API from '@/api/api';
import type { ModelConfiguration } from '@/types/Types';

export const getAllModelConfigurations = async (): Promise<ModelConfiguration[]> => {
	const response = await API.get(`/model-configurations`);
	return response?.data ?? null;
};

export const getModelConfigurationById = async (id: string): Promise<ModelConfiguration> => {
	const response = await API.get(`/model-configurations/${id}`);
	return response?.data ?? null;
};

export const createModelConfiguration = async (
	modelConfiguration: ModelConfiguration
): Promise<ModelConfiguration> => {
	modelConfiguration.temporary = modelConfiguration.temporary ?? false;
	const response = await API.post(`/model-configurations`, modelConfiguration);
	return response?.data ?? null;
};

export const updateModelConfiguration = async (modelConfiguration: ModelConfiguration) => {
	const response = await API.put(
		`/model-configurations/${modelConfiguration.id}`,
		modelConfiguration
	);
	return response?.data ?? null;
};
