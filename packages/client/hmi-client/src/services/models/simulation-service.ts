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
		const resp = await API.post('simulation-service/forecast', simulationParam);
		const output = resp.data;
		return output;
	} catch (err) {
		logger.error(err);
		return null;
	}
}

export async function getRunStatus(runId: number) {
	try {
		const resp = await API.get(`simulation-service/${runId}/status`);
		const output = resp.data;
		return output;
	} catch (err) {
		logger.error(err);
		return null;
	}
}

export async function getRunResult(runId: number) {
	try {
		const resp = await API.get(`simulation-service/${runId}/result`);
		const output = resp.data;
		return output;
	} catch (err) {
		logger.error(err);
		return null;
	}
}
