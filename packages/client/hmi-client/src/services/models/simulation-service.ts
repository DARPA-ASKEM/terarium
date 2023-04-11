import { logger } from '@/utils/logger';
import API from '@/api/api';

export interface ForecastParametersType {
	petri: string;
	initials: { [key: string]: number };
	tspan: number[];
	params: { [key: string]: number };
}

export async function makeForecast(simulationParam: ForecastParametersType) {
	try {
		const resp = await API.post('simulation/forecast', simulationParam);
		const output = resp.data;
		return output;
	} catch (err) {
		logger.error(err);
		return null;
	}
}

export async function getRunStatus(runId: number) {
	try {
		const resp = await API.get(`simulation/${runId}/status`);
		const output = resp.data;
		return output;
	} catch (err) {
		logger.error(err);
		return null;
	}
}

export async function getRunResult(runId: number) {
	try {
		const resp = await API.get(`simulation/${runId}/result`);
		const output = resp.data;
		return output;
	} catch (err) {
		logger.error(err);
		return null;
	}
}

export async function getSimulation(id) {
	try {
		const response = await API.get(`/simulation/${id}`);
		return response.data;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

export async function createSimulation(name, description, simulationParams, result) {
	try {
		const response = await API.post('/simulation', {
			name,
			description,
			simulationParams,
			result
		});
		return response.data;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

export async function updateSimulation(id, name, description) {
	try {
		const response = await API.patch(`/simulation/${id}`, {
			name,
			description
		});
		return response.data;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

export async function deleteAnnotation(id) {
	try {
		const response = await API.delete(`/simulation/${id}`);
		return response.data;
	} catch (error) {
		logger.error(error);
		return null;
	}
}
