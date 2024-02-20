import _ from 'lodash';
import API from '@/api/api';
import type { ModelConfiguration, Model } from '@/types/Types';

export const getAllModelConfigurations = async () => {
	const response = await API.get(`/model-configurations`);
	return (response?.data as ModelConfiguration[]) ?? null;
};

export const getModelConfigurationById = async (id: string) => {
	const response = await API.get(`/model-configurations/${id}`);
	return (response?.data as ModelConfiguration) ?? null;
};

export const createModelConfiguration = async (
	model_id: string | undefined,
	name: string,
	description: string,
	configuration: Model,
	isTemporary?: boolean
) => {
	if (!model_id) {
		return null;
	}
	const temporary = isTemporary ?? false;
	const response = await API.post(`/model-configurations`, {
		model_id,
		temporary,
		name,
		description,
		configuration
	});
	return response?.data ?? null;
};

export const addDefaultConfiguration = async (model: Model): Promise<void> => {
	await createModelConfiguration(model.id, 'Default config', 'Default config', model);
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

	// If the "default" config is updated we want to update the model as well
	// because the model as a copy of the data
	if (config.name === 'Default config') {
		API.put(`/models/${config.configuration.id}`, config.configuration);
	}

	const response = await API.put(`/model-configurations/${config.id}`, config);
	return response?.data ?? null;
};
