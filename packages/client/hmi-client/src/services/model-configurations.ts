import { API, getProjectIdFromUrl } from '@/api/api';
import type {
	InitialSemantic,
	Model,
	ModelConfiguration,
	ModelDistribution,
	ObservableSemantic,
	ParameterSemantic
} from '@/types/Types';
import { isEmpty } from 'lodash';
import { pythonInstance } from '@/python/PyodideController';
import { activeProjectId } from '@/composables/activeProject';
import { DistributionType } from './distribution';

export interface SemanticOtherValues {
	name: string;
	target?: string;
	expression?: string;
	expressionMathml?: string;
	referenceId?: string;
	distribution?: ModelDistribution;
	default?: boolean;
}

export const getAllModelConfigurations = async (): Promise<ModelConfiguration[]> => {
	const response = await API.get(`/model-configurations`);
	return response?.data ?? null;
};

export const getModelConfigurationById = async (id: string): Promise<ModelConfiguration> => {
	const response = await API.get(`/model-configurations/${id}`);
	return response?.data ?? null;
};

export const createModelConfiguration = async (modelConfiguration: ModelConfiguration): Promise<ModelConfiguration> => {
	const projectId = activeProjectId.value || getProjectIdFromUrl();
	delete modelConfiguration.id;
	modelConfiguration.temporary = modelConfiguration.temporary ?? false;
	const response = await API.post(`/model-configurations?project-id=${projectId}`, modelConfiguration);
	return response?.data ?? null;
};

export const updateModelConfiguration = async (modelConfiguration: ModelConfiguration) => {
	const response = await API.put(`/model-configurations/${modelConfiguration.id}`, modelConfiguration);
	return response?.data ?? null;
};

export const deleteModelConfiguration = async (id: string) => {
	const response = await API.delete(`/model-configurations/${id}`);
	return response?.data ?? null;
};

export const getAsConfiguredModel = async (modelConfiguration: ModelConfiguration): Promise<Model> => {
	const response = await API.get<Model>(`model-configurations/as-configured-model/${modelConfiguration.id}`);
	return response?.data ?? null;
};

export const postAsConfiguredModel = async (model: Model): Promise<ModelConfiguration> => {
	const projectId = activeProjectId.value || getProjectIdFromUrl();
	const response = await API.post<ModelConfiguration>(
		`model-configurations/as-configured-model/?project-id=${projectId}`,
		model
	);
	return response?.data ?? null;
};

export const amrToModelConfiguration = async (model: Model): Promise<ModelConfiguration> => {
	const projectId = activeProjectId.value || getProjectIdFromUrl();
	const response = await API.post<ModelConfiguration>(
		`/models/amr-to-model-configuration?project-id=${projectId}`,
		model
	);
	return response?.data ?? null;
};

export const getModelIdFromModelConfigurationId = async (id: string) => {
	const modelConfiguration = await getModelConfigurationById(id);
	return modelConfiguration?.modelId ?? null;
};

export function getParameters(config: ModelConfiguration): ParameterSemantic[] {
	return config.parameterSemanticList ?? [];
}

export function getInitials(config: ModelConfiguration): InitialSemantic[] {
	return config.initialSemanticList ?? [];
}

export function setParameterDistributions(
	config: ModelConfiguration,
	distributionParameterMappings: { id: string; distribution: ModelDistribution }[]
): void {
	distributionParameterMappings.forEach((mapping) => {
		setParameterDistribution(config, mapping.id, mapping.distribution);
	});
}

export function getParameter(config: ModelConfiguration, parameterId: string): ParameterSemantic | undefined {
	return config.parameterSemanticList?.find((param) => param.referenceId === parameterId);
}

export function getParameterDistribution(config: ModelConfiguration, parameterId: string): ModelDistribution {
	const parameter = getParameter(config, parameterId);
	if (!parameter) return { type: DistributionType.Constant, parameters: { value: 0 } };
	return parameter.distribution;
}

export function setParameterDistribution(
	config: ModelConfiguration,
	parameterId: string,
	distribution: ModelDistribution
): void {
	const parameter = getParameter(config, parameterId);
	if (!parameter) return;

	const type = distribution.type;

	if (type === DistributionType.Constant) {
		parameter.distribution = convertToConstantDistribution(distribution);
	} else if (type === DistributionType.Uniform) {
		parameter.distribution = convertToUniformDistribution(distribution);
	}
}

function convertToConstantDistribution(distribution: ModelDistribution): ModelDistribution {
	// if no parameters, initialize
	if (isEmpty(distribution.parameters)) distribution.parameters = { value: 0 };

	return {
		type: DistributionType.Constant,
		parameters: {
			value: distribution.parameters.value
		}
	};
}

function convertToUniformDistribution(distribution: ModelDistribution): ModelDistribution {
	// if no parameters, initialize
	if (isEmpty(distribution.parameters)) distribution.parameters = { minimum: 0, maximum: 0 };

	return {
		type: DistributionType.Uniform,
		parameters: {
			minimum: distribution.parameters.minimum,
			maximum: distribution.parameters.maximum
		}
	};
}

export function getParameterSource(config: ModelConfiguration, parameterId: string): string {
	return getParameter(config, parameterId)?.source ?? '';
}

export function setParameterSource(config: ModelConfiguration, parameterId: string, source: string): void {
	const parameter = getParameter(config, parameterId);
	if (parameter) {
		parameter.source = source;
	}
}

export function getInitial(config: ModelConfiguration, initialId: string): InitialSemantic | undefined {
	return getInitials(config).find((initial) => initial.target === initialId);
}

export function setInitialExpression(config: ModelConfiguration, initialId: string, expression: string): void {
	const initial = getInitial(config, initialId);
	if (!initial) return;

	pythonInstance
		.parseExpression(expression)
		.then((result) => {
			const mathml = result.mathml;
			initial.expression = expression;
			initial.expressionMathml = mathml;
		})
		.catch((error) => {
			// Handle error appropriately
			console.error('Error parsing expression:', error);
		});
}

export function setInitialSource(config: ModelConfiguration, initialId: string, source: string): void {
	const initial = getInitial(config, initialId);
	if (initial) {
		initial.source = source;
	}
}

export function getInitialSource(config: ModelConfiguration, initialId: string): string {
	return getInitial(config, initialId)?.source ?? '';
}

export function getInitialExpression(config: ModelConfiguration, initialId: string): string {
	return getInitial(config, initialId)?.expression ?? '';
}

export function getObservables(config: ModelConfiguration): ObservableSemantic[] {
	return config.observableSemanticList ?? [];
}

export function getOtherValues(configs: ModelConfiguration[], id: string, key: string, otherValueList: string) {
	let otherValues: SemanticOtherValues[] = [];

	const modelConfigTableData = configs.map((modelConfig) => ({
		name: modelConfig.name ?? '',
		list: modelConfig[otherValueList]
	}));

	modelConfigTableData.forEach((modelConfig) => {
		const config: ParameterSemantic[] | InitialSemantic[] = modelConfig.list.filter((item) => item[key] === id)[0];
		if (config && modelConfig.name) {
			const data: SemanticOtherValues = { name: modelConfig.name, ...config };
			otherValues = [...otherValues, data];
		}
	});
	return otherValues;
}
