import _ from 'lodash';
import API from '@/api/api';
import { ModelConfiguration, Model } from '@/types/Types';

export const getAllModelConfigurations = async () => {
	const response = await API.get(`/model_configurations`);
	return (response?.data as ModelConfiguration[]) ?? null;
};

export const getModelConfigurationById = async (id: string) => {
	const response = await API.get(`/model_configurations/${id}`);
	return (response?.data as ModelConfiguration) ?? null;
};

export const createModelConfiguration = async (
	model_id: string,
	name: string,
	description: string,
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
	// Do a sanity pass to ensure type-safety
	const model: Model = config.configuration as Model;
	const parameters = model.semantics?.ode.parameters;
	if (parameters) {
		parameters.forEach((param) => {
			if (param.value && typeof param.value === 'string' && _.isNumber(+param.value)) {
				param.value = +param.value;
				console.debug(`corerce ${param.id} ${param.value} to number`);
			}
		});
	}

	const response = await API.put(`/model_configurations/${config.id}`, config);
	return response?.data ?? null;
};
