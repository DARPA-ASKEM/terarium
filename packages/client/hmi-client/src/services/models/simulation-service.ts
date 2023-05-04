import { logger } from '@/utils/logger';
import API from '@/api/api';
import { Simulation, SimulationParams, CalibrationParams } from '@/types/Types';

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
		// const response = await API.post('/simulation', simulation);
		const request = {
			simulator_id: 1,
			parameters: Object.entries(simulation).map((entry) => ({
				name: entry[0],
				value: JSON.stringify(entry[1]),
				type: 'float',
				id: 0
			}))
		};
		const response = await API.post('/simulations/runs/', request);
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

export async function makeCalibrateJob(calibrationParams: CalibrationParams) {
	try {
		const resp = await API.post('simulation/calibrate', calibrationParams);
		const output = resp.data;
		return output;
	} catch (err) {
		logger.error(err);
		return null;
	}
}
