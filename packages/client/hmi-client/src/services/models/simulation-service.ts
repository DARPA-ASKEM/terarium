import { csvParse } from 'd3';
import { logger } from '@/utils/logger';
import API from '@/api/api';
import {
	CalibrationRequestCiemss,
	CalibrationRequestJulia,
	ClientEvent,
	ClientEventType,
	EnsembleCalibrationCiemssRequest,
	EnsembleSimulationCiemssRequest,
	EventType,
	OptimizeRequestCiemss,
	ProgressState,
	Simulation,
	SimulationRequest
} from '@/types/Types';
import { RunResults } from '@/types/SimulateConfig';
import * as EventService from '@/services/event';
import { WorkflowNode } from '@/types/workflow';
import { useProjects } from '@/composables/project';
import { subscribe, unsubscribe } from '@/services/ClientEventService';

export async function cancelCiemssJob(runId: String) {
	try {
		const resp = await API.get(`simulation-request/ciemss/cancel/${runId}`);
		const output = resp.data;
		return output;
	} catch (err) {
		logger.error(err);
		return null;
	}
}

export async function makeForecastJob(simulationParam: SimulationRequest) {
	try {
		const resp = await API.post('simulation-request/forecast', simulationParam);
		EventService.create(
			EventType.TransformPrompt,
			useProjects().activeProject.value?.id,
			JSON.stringify({
				type: 'julia',
				params: simulationParam
			})
		);
		const output = resp.data;
		return output;
	} catch (err) {
		logger.error(err);
		return null;
	}
}

export async function makeForecastJobCiemss(simulationParam: SimulationRequest) {
	try {
		const resp = await API.post('simulation-request/ciemss/forecast', simulationParam);
		EventService.create(
			EventType.TransformPrompt,
			useProjects().activeProject.value?.id,
			JSON.stringify({
				type: 'ciemss',
				params: simulationParam
			})
		);
		const output = resp.data;
		return output;
	} catch (err) {
		logger.error(err);
		return null;
	}
}

// TODO: Add typing to julia's output: https://github.com/DARPA-ASKEM/Terarium/issues/1655
export async function getRunResultJulia(runId: string, filename = 'result.json') {
	try {
		const resp = await API.get(`simulations/${runId}/result`, {
			params: { filename }
		});
		const output = resp.data;
		const [states, params] = output;

		const columnNames = (states.colindex.names as string[]).join(',');
		let csvData: string = columnNames as string;
		for (let j = 0; j < states.columns[0].length; j++) {
			csvData += '\n';
			for (let i = 0; i < states.columns.length; i++) {
				csvData += `${states.columns[i][j]},`;
			}
		}

		const paramVals = {};
		Object.entries(params.colindex.lookup).forEach(([key, value]) => {
			paramVals[key] = params.columns[(value as number) - 1][0];
		});

		return { csvData, paramVals };
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

// Return the presigned download URL of the calibration output blob
// This is only applicable to CIEMSS functionalities
export async function getCalibrateBlobURL(runId: string) {
	const resp = await API.get(`simulations/${runId}/download-url`, {
		params: { filename: 'parameters.dill' }
	});
	return resp.data.url;
}

export async function getRunResultCiemss(runId: string, filename = 'result.csv') {
	const resultCsv = await getRunResult(runId, filename);
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
			const keyArr = key.split('_');
			const keySuffix = keyArr.pop();
			const keyName = keyArr.join('_');

			if (keySuffix === 'param' || keySuffix === 'state') {
				outputRowRunResults[keyName] = inputRow[key];
				if (!runConfigs[keyName]) {
					runConfigs[keyName] = [];
				}
				runConfigs[keyName].push(Number(inputRow[key]));
			} else if (keySuffix === 'sol') {
				outputRowRunResults[keyName] = inputRow[key];
			} else if (keySuffix === 'obs') {
				outputRowRunResults[keyName] = inputRow[key];
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
		EventService.create(
			EventType.RunCalibrate,
			useProjects().activeProject.value?.id,
			JSON.stringify(calibrationParams)
		);
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

export async function makeOptimizeJobCiemss(optimizeParams: OptimizeRequestCiemss) {
	try {
		const resp = await API.post('simulation-request/ciemss/optimize', optimizeParams);
		const output = resp.data;
		return output;
	} catch (err) {
		logger.error(err);
		return null;
	}
}

export async function makeEnsembleCiemssSimulation(params: EnsembleSimulationCiemssRequest) {
	try {
		const resp = await API.post('simulation-request/ciemss/ensemble-simulate', params);
		const output = resp.data;
		return output;
	} catch (err) {
		logger.error(err);
		return null;
	}
}

export async function makeEnsembleCiemssCalibration(params: EnsembleCalibrationCiemssRequest) {
	try {
		const resp = await API.post('simulation-request/ciemss/ensemble-calibrate', params);
		const output = resp.data;
		return output;
	} catch (err) {
		logger.error(err);
		return null;
	}
}

// This function returns a string array of run ids.
export const querySimulationInProgress = (node: WorkflowNode<any>): string[] => {
	const state = node.state;
	if (state.simulationsInProgress && state.simulationsInProgress.length > 0) {
		// return all run ids on the node
		return state.simulationsInProgress;
	}

	// return an empty array if no run ids are present
	return [];
};

export async function subscribeToUpdateMessages(
	simulationIds: string[],
	eventType: ClientEventType,
	messageHandler: (data: ClientEvent<any>) => void
) {
	await API.get(`/simulations/subscribe?simulation-ids=${simulationIds}`);
	await subscribe(eventType, messageHandler);
}

export async function unsubscribeToUpdateMessages(
	simulationIds: string[],
	eventType: ClientEventType,
	messageHandler: (data: ClientEvent<any>) => void
) {
	await API.get(`/simulations/unsubscribe?simulation-ids=${simulationIds}`);
	await unsubscribe(eventType, messageHandler);
}

export async function pollAction(id: string) {
	const simResponse: Simulation | null = await getSimulation(id);
	if (!simResponse) {
		console.error(`Error occured with simulation ${id}`);
		return { data: null, progress: null, error: `Failed running simulation ${id}` };
	}

	if ([ProgressState.Queued, ProgressState.Running].includes(simResponse.status)) {
		// TODO: untangle progress
		return { data: null, progress: null, error: null };
	}

	if ([ProgressState.Error, ProgressState.Failed].includes(simResponse.status)) {
		const errorMessage: string = simResponse.statusMessage || `Failed running simulation ${id}`;
		return { data: null, progress: null, error: errorMessage };
	}

	if (simResponse.status === ProgressState.Cancelled) {
		return { data: simResponse, progress: null, error: null, cancelled: true };
	}
	return { data: simResponse, progress: null, error: null };
}
