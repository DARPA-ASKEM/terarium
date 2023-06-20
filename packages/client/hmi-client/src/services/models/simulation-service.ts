import { logger } from '@/utils/logger';
import API from '@/api/api';
import { Simulation, SimulationRequest, CalibrationRequest } from '@/types/Types';

export async function makeForecastJob(simulationParam: SimulationRequest) {
	try {
		const resp = await API.post('simulation/forecast', simulationParam);
		const output = resp.data;
		return output;
	} catch (err) {
		logger.error(err);
		return null;
	}
}

// FIXME: TS1225 Consolidated to getSimuation
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

// FIXME: TS1225 Consolidate to getSimulation
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

export async function makeCalibrateJob(calibrationParams: CalibrationRequest) {
	try {
		const resp = await API.post('simulation/calibrate', calibrationParams);
		const output = resp.data;
		return output;
	} catch (err) {
		logger.error(err);
		return null;
	}
}
