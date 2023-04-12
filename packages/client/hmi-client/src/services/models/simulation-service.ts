import { logger } from '@/utils/logger';
import API from '@/api/api';
import { Simulation, SimulationParams } from '@/types/Types';

export async function makeForecast(simulationParam: SimulationParams) {
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

export async function getSimulation(id: Simulation['id']): Promise<Simulation | null> {
	try {
		const response = await API.get(`/simulation/${id}`);
		return response.data;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

export async function createSimulation(simulation: Simulation): Promise<Simulation | null> {
	try {
		const response = await API.post('/simulation', simulation);
		return response.data;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

export async function updateSimulation(
	id: Simulation['id'],
	name: Simulation['name'],
	description: Simulation['description']
): Promise<Simulation | null> {
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

export async function deleteAnnotation(id: Simulation['id']) {
	try {
		const response = await API.delete(`/simulation/${id}`);
		return response.data;
	} catch (error) {
		logger.error(error);
		return null;
	}
}
