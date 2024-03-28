import { Operation, WorkflowOperationTypes } from '@/types/workflow';

export enum InterventionTypes {
	paramValue = 'param_value',
	startTime = 'start_time'
}

export interface InterventionPolicyGroup {
	borderColour: string;
	name: string;
	parameter: string;
	startTime: number;
	lowerBound: number;
	upperBound: number;
	initialGuess: number;
	isActive: boolean;
	paramValue: number;
}

export interface OptimizeCiemssOperationState {
	// Settings
	endTime: number;
	numSamples: number;
	solverMethod: string;
	maxiter: number;
	maxfeval: number;
	// Intervention policies
	interventionType: InterventionTypes;
	interventionPolicyGroups: InterventionPolicyGroup[];
	// Constraints
	targetVariables: string[];
	riskTolerance: number;
	threshold: number;
	isMinimized: boolean;
	chartConfigs: string[][];
	inProgressOptimizeId: string;
	inProgressForecastId: string;
	forecastRunId: string;
	optimzationRunId: string;
	modelConfigName: string;
	modelConfigDesc: string;
	optimizeErrorMessage: { name: string; value: string; traceback: string };
	simulateErrorMessage: { name: string; value: string; traceback: string };
}

export const blankInterventionPolicyGroup: InterventionPolicyGroup = {
	borderColour: '#cee2a4',
	name: 'Policy bounds',
	parameter: '',
	startTime: 0,
	lowerBound: 0,
	upperBound: 0,
	initialGuess: 0,
	isActive: true,
	paramValue: 0
};

export const OptimizeCiemssOperation: Operation = {
	name: WorkflowOperationTypes.OPTIMIZE_CIEMSS,
	displayName: 'Optimize with PyCIEMSS',
	description: 'Optimize with PyCIEMSS',
	inputs: [
		{ type: 'modelConfigId', label: 'Model configuration', acceptMultiple: false },
		{ type: 'calibrateSimulationId', label: 'Calibration', acceptMultiple: false, isOptional: true }
	],
	outputs: [{ type: 'simulationId' }],
	isRunnable: true,

	initState: () => {
		const init: OptimizeCiemssOperationState = {
			endTime: 90,
			numSamples: 1000,
			solverMethod: 'dopri5',
			maxiter: 5,
			maxfeval: 25,
			interventionType: InterventionTypes.paramValue,
			interventionPolicyGroups: [blankInterventionPolicyGroup],
			targetVariables: [],
			riskTolerance: 5,
			threshold: 1,
			isMinimized: true,
			chartConfigs: [],
			inProgressOptimizeId: '',
			inProgressForecastId: '',
			forecastRunId: '',
			optimzationRunId: '',
			modelConfigName: '',
			modelConfigDesc: '',
			optimizeErrorMessage: { name: '', value: '', traceback: '' },
			simulateErrorMessage: { name: '', value: '', traceback: '' }
		};
		return init;
	}
};
