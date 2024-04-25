import { Operation, WorkflowOperationTypes, BaseState } from '@/types/workflow';

const DOCUMENTATION_URL =
	'https://github.com/DARPA-ASKEM/sciml-service/blob/main/src/operations.jl#L245';

export interface CalibrateMap {
	modelVariable: string;
	datasetVariable: string;
}

export interface CalibrateExtraJulia {
	numChains: number;
	numIterations: number;
	odeMethod: string;
	calibrateMethod: string;
}

export enum CalibrateMethodOptions {
	BAYESIAN = 'bayesian',
	LOCAL = 'local',
	GLOBAL = 'global'
}

export interface CalibrationOperationStateJulia extends BaseState {
	// state shared across all runs
	chartConfigs: string[][];
	mapping: CalibrateMap[];

	// state specific to individual calibrate runs
	extra: CalibrateExtraJulia;
	intermediateLoss?: Record<string, number>[];

	inProgressSimulationId: string;
}

export const CalibrationOperationJulia: Operation = {
	name: WorkflowOperationTypes.CALIBRATION_JULIA,
	displayName: 'Calibrate with SciML',
	description:
		'given a model id, a dataset id, and optionally a configuration. calibrate the models initial values and rates',
	documentationUrl: DOCUMENTATION_URL,
	inputs: [
		{ type: 'modelConfigId', label: 'Model configuration' },
		{ type: 'datasetId', label: 'Dataset' }
	],
	outputs: [{ type: 'simulationId' }],
	isRunnable: true,

	action: async () => null,

	initState: () => {
		const init: CalibrationOperationStateJulia = {
			chartConfigs: [],
			inProgressSimulationId: '',
			mapping: [{ modelVariable: '', datasetVariable: '' }],
			extra: {
				numChains: 4,
				numIterations: 50,
				odeMethod: 'default',
				calibrateMethod: CalibrateMethodOptions.GLOBAL
			}
		};
		return init;
	}
};
