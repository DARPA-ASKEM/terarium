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
import { pythonInstance } from '@/python/PyodideController';
import { DistributionType } from '@/services/distribution';

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

export function sanityCheck(config: ModelConfiguration): string[] {
	const errors: string[] = [];
	const modelToCheck = config.configuration;
	if (!modelToCheck) {
		errors.push('no model defined in configuration');
		return errors;
	}

	const parameters: ModelParameter[] = getParameters(config);

	parameters.forEach((p) => {
		const val = p.value;
		const max = parseFloat(p.distribution?.parameters.maximum);
		const min = parseFloat(p.distribution?.parameters.minimum);
		if (val && val > max) {
			errors.push(`${p.id} value ${p.value} > distribution max of ${max}`);
		}
		if (val && val < min) {
			errors.push(`${p.id} value ${p.value} < distribution min of ${min}`);
		}

		// Arbitrary 0.003 here, try to ensure interval is significant w.r.t value
		const interval = Math.abs(max - min);
		if (val !== 0 && val !== undefined && Math.abs(interval / val) < 0.003) {
			errors.push(`${p.id} distribution range [${min}, ${max}] may be too small`);
		}

		// no constant & no/partial distribution
		if (val === undefined || Number.isNaN(val)) {
			if (Number.isNaN(max) || Number.isNaN(min)) {
				errors.push(`${p.id} has no constant value and partial/no distribution`);
			}
		}
	});
	return errors;
}

export function getInitial(config: ModelConfiguration, initialId: string): Initial | undefined {
	return config.configuration.semantics?.ode.initials?.find(
		(initial) => initial.target === initialId
	);
}

export function getInitialSource(config: ModelConfiguration, initialId: string): string {
	return config.configuration.metadata?.initials?.[initialId]?.source ?? '';
}

export function getInitialName(config: ModelConfiguration, initialId: string): string {
	return config.configuration.metadata?.initials?.[initialId]?.name ?? '';
}

export function getInitialUnit(config: ModelConfiguration, initialId: string): string {
	return config.configuration.metadata?.initials?.[initialId]?.unit ?? '';
}

export function getInitialDescription(config: ModelConfiguration, initialId: string): string {
	return config.configuration.metadata?.initials?.[initialId]?.description ?? '';
}

export function getInitialExpression(config: ModelConfiguration, initialId: string): string {
	const initial = getInitial(config, initialId);
	return initial?.expression ?? '';
}

export function setInitialSource(
	config: ModelConfiguration,
	initialId: string,
	source: string
): void {
	const initial = config.configuration.metadata?.initials?.[initialId];
	if (initial) {
		initial.source = source;
	}
}

export function setInitialExpression(
	config: ModelConfiguration,
	initialId: string,
	expression: string
): void {
	const initial = getInitial(config, initialId);
	if (!initial) return;

	pythonInstance
		.parseExpression(expression)
		.then((result) => {
			const mathml = result.mathml;
			initial.expression = expression;
			initial.expression_mathml = mathml;
		})
		.catch((error) => {
			// Handle error appropriately
			console.error('Error parsing expression:', error);
		});
}

export function setInitialName(config: ModelConfiguration, initialId: string, name: string): void {
	const initial = config.configuration.metadata?.initials?.[initialId];
	if (initial) {
		initial.name = name;
	}
}

export function setInitialDescription(
	config: ModelConfiguration,
	initialId: string,
	description: string
): void {
	const initial = config.configuration.metadata?.initials?.[initialId];
	if (initial) {
		initial.description = description;
	}
}

export function getParameter(
	config: ModelConfiguration,
	parameterId: string
): ModelParameter | undefined {
	return config.configuration.semantics?.ode.parameters?.find((param) => param.id === parameterId);
}

export function getParameterName(config: ModelConfiguration, parameterId: string): string {
	const parameter = getParameter(config, parameterId);
	return parameter?.name ?? '';
}

export function getParameterDescription(config: ModelConfiguration, parameterId: string): string {
	const parameter = getParameter(config, parameterId);
	return parameter?.description ?? '';
}

export function getParameterUnit(config: ModelConfiguration, parameterId: string): string {
	const parameter = getParameter(config, parameterId);
	return parameter?.units?.expression ?? '';
}

export function getParameterDistribution(
	config: ModelConfiguration,
	parameterId: string
): ModelDistribution | undefined {
	const parameter = getParameter(config, parameterId);
	return parameter?.distribution;
}

export function getParameterDistributionType(
	config: ModelConfiguration,
	parameterId: string
): DistributionType {
	const parameter = getParameter(config, parameterId);
	// for now just returning uniform distribution for all distributions
	if (parameter?.distribution) return DistributionType.Uniform;
	return DistributionType.Constant;
}

export function getInterventions(config: ModelConfiguration): Intervention[] {
	return config.interventions ?? [];
}

// FIXME: for set and remove interventions, we should not be using the index.  This should be addressed when we move to the new model config data structure.
export function setIntervention(
	config: ModelConfiguration,
	index: number,
	intervention: Intervention
): void {
	const interventions = getInterventions(config);
	interventions[index] = intervention;
}

export function removeIntervention(config: ModelConfiguration, index: number): void {
	const interventions = getInterventions(config);
	interventions.splice(index, 1);
}

export function getParameterSource(config: ModelConfiguration, parameterId: string): string {
	return config.configuration.metadata?.parameters?.[parameterId]?.source ?? '';
}

export function setParameterSource(
	config: ModelConfiguration,
	parameterId: string,
	source: string
): void {
	const parameter = config.configuration.metadata?.parameters?.[parameterId];
	if (parameter) {
		parameter.source = source;
	}
}

export function getParameterConstant(config: ModelConfiguration, parameterId: string): number {
	const parameter = getParameter(config, parameterId);
	return parameter?.value ?? NaN;
}

export function setParameterConstant(
	config: ModelConfiguration,
	parameterId: string,
	value: string
): void {
	const parameter = getParameter(config, parameterId);
	if (parameter) {
		parameter.value = parseFloat(value);
	}
}

// sets the parameter distribution type along with its default respective parameters
export function setParameterDistributionType(
	config: ModelConfiguration,
	parameterId: string,
	distributionType: DistributionType
): void {
	const parameter = getParameter(config, parameterId);
	if (!parameter) return;
	switch (distributionType) {
		case DistributionType.Constant:
			delete parameter.distribution;
			break;
		case DistributionType.Uniform:
			parameter.distribution = {
				type: DistributionType.Uniform,
				parameters: {
					minimum: 0,
					maximum: 0
				}
			};
			break;
		default:
			break;
	}
}

// sets the parameter distribution parameters (i.e. minimum and maximum for uniform distribution)
export function setParameterDistributionParameters(
	config: ModelConfiguration,
	parameterId: string,
	parameters: { [index: string]: any }
): void {
	const parameter = getParameter(config, parameterId);
	if (parameter?.distribution) {
		parameter.distribution.parameters = {
			...parameter.distribution.parameters,
			...parameters
		};
	}
}

export function getParameters(config: ModelConfiguration): ModelParameter[] {
	return config.configuration.semantics?.ode.parameters ?? [];
}

export function getInitials(config: ModelConfiguration): Initial[] {
	return config.configuration.semantics?.ode.initials ?? [];
}
