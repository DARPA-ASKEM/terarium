import API from '@/api/api';
import { ModelConfiguration, Model } from '@/types/Types';
import * as AMRService from '@/model-representation/petrinet/petrinet-service';

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
	// FIXME: We need to hack this into a PetriNet acset until TA3 can consume AMRs. June 21, 2023
	const hackConfig: any = AMRService.convertAMRToACSet(configuration.model as any);
	const response = await API.post(`/model_configurations`, {
		model_id,
		name,
		description,
		configuration: hackConfig,
		amr_configuration: configuration
	});
	return response?.data ?? null;
};

export const updateModelConfiguration = async (config: ModelConfiguration) => {
	const response = await API.put(`/model_configurations/${config.id}`, config);
	return response?.data ?? null;
};
