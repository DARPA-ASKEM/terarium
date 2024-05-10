import _ from 'lodash';
import API from '@/api/api';
import type {
	ModelConfiguration,
	Model,
	Intervention,
	ModelParameter,
	ModelDistribution,
	Initial
} from '@/types/Types';

export const getAllModelConfigurations = async () => {
	const response = await API.get(`/model-configurations`);
	return (response?.data as ModelConfiguration[]) ?? null;
};

export const getModelConfigurationById = async (id: string) => {
	const response = await API.get(`/model-configurations/${id}`);
	return (response?.data as ModelConfiguration) ?? null;
};

export const getModelIdFromModelConfigurationId = async (id: string) => {
	const modelConfiguration = await getModelConfigurationById(id);
	return modelConfiguration?.model_id ?? null;
};

export const createModelConfiguration = async (
	model_id: string | undefined,
	name: string,
	description: string,
	configuration: Model,
	isTemporary?: boolean,
	givenInterventions?: Intervention[]
) => {
	if (!model_id) {
		return null;
	}
	const temporary = isTemporary ?? false;
	const interventions = givenInterventions ?? [];
	const response = await API.post(`/model-configurations`, {
		model_id,
		temporary,
		name,
		description,
		configuration,
		interventions
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

export const getInitial = (config: ModelConfiguration, initialId: string): Initial | undefined =>
	config.configuration.semantics?.ode.initials?.find((initial) => initial.target === initialId);

export const getInitialSource = (config: ModelConfiguration, initialId: string): string =>
	config.configuration.metadata?.initials?.[initialId].source ?? '';

export const setInitialSource = (
	config: ModelConfiguration,
	initialId: string,
	source: string
): void => {
	const initial = config.configuration.metadata?.initials?.[initialId];
	if (initial) {
		initial.source = source;
	}
};

export const getParameter = (
	config: ModelConfiguration,
	parameterId: string
): ModelParameter | undefined =>
	config.configuration.semantics?.ode.parameters?.find((param) => param.id === parameterId);

export const setDistribution = (
	config: ModelConfiguration,
	distribution: ModelDistribution,
	parameterId: string
): void => {
	const parameter = getParameter(config, parameterId);
	if (parameter) {
		parameter.distribution = distribution;
	}
};

export const removeDistribution = (config: ModelConfiguration, parameterId: string): void => {
	const parameter = getParameter(config, parameterId);
	if (parameter?.distribution) {
		delete parameter.distribution;
	}
};

export const getInterventions = (config: ModelConfiguration): Intervention[] =>
	config.interventions ?? [];

export const setIntervention = (
	config: ModelConfiguration,
	intervention: Intervention,
	index: number
): void => {
	const interventions = getInterventions(config);
	interventions[index] = intervention;
};

export const removeIntervention = (config: ModelConfiguration, index: number): void => {
	const interventions = getInterventions(config);
	interventions.splice(index, 1);
};

export const getParameterSource = (config: ModelConfiguration, parameterId: string): string =>
	config.configuration.metadata?.parameters?.[parameterId]?.source ?? '';

export const setParameterSource = (
	config: ModelConfiguration,
	parameterId: string,
	source: string
): void => {
	const parameter = config.configuration.metadata?.parameters?.[parameterId];
	if (parameter) {
		parameter.source = source;
	}
};
