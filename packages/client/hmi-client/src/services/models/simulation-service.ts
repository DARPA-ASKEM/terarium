import { cloneDeep, isEmpty, isEqual } from 'lodash';
import { Ref } from 'vue';
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
	ProgressState,
	Simulation,
	SimulationRequest
} from '@/types/Types';
import { RunResults } from '@/types/SimulateConfig';
import * as EventService from '@/services/event';
import { WorkflowNode } from '@/types/workflow';
import { useProjects } from '@/composables/project';
import { subscribe, unsubscribe } from '@/services/ClientEventService';

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

// add a simulation in progress if it does not exist
const addSimulationInProgress = (state: any, runIds: string[]) => {
	if (!state.simulationsInProgress) {
		state.simulationsInProgress = [];
	}
	runIds.forEach((runId) => {
		if (!state.simulationsInProgress.includes(runId)) {
			state.simulationsInProgress.push(runId);
		}
	});
};

// delete a simulation in progress if it exists
const deleteSimulationInProgress = (state: any, runIds: string[]) => {
	if (state.simulationsInProgress) {
		runIds.forEach((runId) => {
			const index = state.simulationsInProgress.indexOf(runId);
			if (index !== -1) {
				state.simulationsInProgress.splice(index, 1);
			}
		});
	}
};

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

export async function simulationPollAction(
	simulationIds: string[],
	node: WorkflowNode<any>,
	progress: Ref<{ status: ProgressState; value: number }>,
	emitFn: (event: 'append-output' | 'update-state', ...args: any[]) => void
) {
	const requestList: Promise<Simulation | null>[] = [];

	simulationIds.forEach((id) => {
		requestList.push(getSimulation(id));
	});

	const response = await Promise.all(requestList).then(
		(list) => list.filter((item) => !!item) as Simulation[]
	);

	const completedSimulationIds = isEmpty(response)
		? ([] as string[])
		: (response
				.filter((simulation) => simulation?.status === ProgressState.Complete)
				.map((simulation) => simulation!.id) as string[]);

	const inProgressSimulationIds = isEmpty(response)
		? ([] as string[])
		: (response
				.filter(
					(simulation) =>
						simulation?.status === ProgressState.Queued ||
						simulation?.status === ProgressState.Running
				)
				.map((simulation) => simulation!.id) as string[]);

	const unhandledStateSimulationIds = response
		.filter(
			(simulation) =>
				simulation?.status !== ProgressState.Queued &&
				simulation?.status !== ProgressState.Running &&
				simulation?.status !== ProgressState.Complete
		)
		.map((simulation) => simulation!.id);

	// there are unhandled states - we will return an error and remove all simulation Ids
	if (unhandledStateSimulationIds.length > 0) {
		const newState = cloneDeep(node.state);
		deleteSimulationInProgress(newState, simulationIds);
		if (!isEqual(node.state, newState)) {
			emitFn('update-state', newState);
		}

		return {
			data: response,
			progress: null,
			error: true
		};
	}

	// all simulations complete
	if (inProgressSimulationIds.length === 0 && completedSimulationIds.length > 0) {
		const newState = cloneDeep(node.state);
		deleteSimulationInProgress(newState, completedSimulationIds);
		// only update state if it is different from the current one
		if (!isEqual(node.state, newState)) {
			emitFn('update-state', newState);
		}
		return {
			data: response,
			progress: null,
			error: null
		};
	}

	// handle any in progress simulations
	if (inProgressSimulationIds.length > 0) {
		const newState = cloneDeep(node.state);
		addSimulationInProgress(newState, inProgressSimulationIds);
		deleteSimulationInProgress(newState, completedSimulationIds);

		// only update state if it is different from the current one
		if (!isEqual(node.state, newState)) {
			emitFn('update-state', newState);
		}
		progress.value.status = ProgressState.Running;
		// keep polling
		return {
			data: null,
			progress: null,
			error: null
		};
	}

	// remove all simulations for now if there is an unhandled state
	const newState = deleteSimulationInProgress(node, simulationIds);
	if (!isEqual(node.state, newState)) {
		emitFn('update-state', newState);
	}

	return {
		data: response,
		progress: null,
		error: true
	};
}
