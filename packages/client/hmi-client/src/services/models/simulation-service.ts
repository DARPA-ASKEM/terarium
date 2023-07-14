import { csvParse } from 'd3';

import { logger } from '@/utils/logger';
import API from '@/api/api';
import {
	Simulation,
	SimulationRequest,
	CalibrationRequestJulia,
	CalibrationRequestCiemss
} from '@/types/Types';
import { RunResults } from '@/types/SimulateConfig';

export async function makeForecastJob(simulationParam: SimulationRequest) {
	try {
		const resp = await API.post('simulation-request/forecast', simulationParam);
		const output = resp.data;
		return output;
	} catch (err) {
		logger.error(err);
		return null;
	}
}

export async function makeForecastJobCiemss(simulationParam: SimulationRequest) {
	try {
		const resp = await API.post('simulation-request/ciemss/forecast/', simulationParam);
		const output = resp.data;
		return output;
	} catch (err) {
		logger.error(err);
		return null;
	}
}

export async function getRunResult(runId: string, filename: string) {
	try {
		const resp = await API.get(`simulations/${runId}/result`, {
			params: { filename }
		});
		const output = resp.data;
		return output;
	} catch (err) {
		logger.error(err);
		return null;
	}
}

export async function getRunResultCiemss(runId: string) {
	const resultCsv = await getRunResult(runId, 'result.csv');
	const csvData = csvParse(resultCsv);

	const output = {
		parsedRawData: csvData,
		runResults: {} as RunResults,
		runConfigs: {} as { [paramKey: string]: number[] }
	};
	const { parsedRawData, runResults, runConfigs } = output;

	const sampleList = new Array(Number(parsedRawData[parsedRawData.length - 1].sample_id) + 1)
		.fill('0')
		.map((_x, i) => i.toString());

	// initialize runResults ds
	for (let i = 0; i < sampleList.length; i++) {
		runResults[i.toString()] = [];
	}

	// populate runResults
	parsedRawData.forEach((inputRow) => {
		const outputRowRunResults = { timestamp: inputRow.timepoint_id };
		Object.keys(inputRow).forEach((key) => {
			if (key.includes('_sol')) {
				outputRowRunResults[key.replace('_sol', '')] = inputRow[key];
			} else if (key.includes('_param')) {
				const paramKey = key.replace('_param', '');
				if (!runConfigs[paramKey]) {
					runConfigs[paramKey] = [];
				}
				runConfigs[paramKey].push(Number(inputRow[key]));
			}
		});
		runResults[inputRow.sample_id as string].push(outputRowRunResults as any);
	});

	Object.keys(runConfigs).forEach((key) => {
		runConfigs[key] = runConfigs[key].sort();
	});

	return output;
}

export async function getSimulation(id: Simulation['id']): Promise<Simulation | null> {
	try {
		const response = await API.get(`/simulations/${id}`);
		return response.data;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

export async function makeCalibrateJobJulia(calibrationParams: CalibrationRequestJulia) {
	try {
		const resp = await API.post('simulation-request/calibrate', calibrationParams);
		const output = resp.data;
		return output;
	} catch (err) {
		logger.error(err);
		return null;
	}
}

export async function makeCalibrateJobCiemss(calibrationParams: CalibrationRequestCiemss) {
	try {
		const resp = await API.post('simulation-request/ciemss/calibrate', calibrationParams);
		const output = resp.data;
		return output;
	} catch (err) {
		logger.error(err);
		return null;
	}
}
