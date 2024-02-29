import { Operation, WorkflowOperationTypes } from '@/types/workflow';

export interface InterventionPolicyGroup {
	borderColour: string;
	name: string;
	parameter: string;
	costBenefitFn: string;
	startTime: number;
	lowerBound: number;
	upperBound: number;
	initialGuess: number;
	isActive: boolean;
}

export interface OptimizeCiemssOperationState {
	// Settings
	startTime: number;
	endTime: number;
	numTimePoints: number;
	timeUnit: string;
	numStochasticSamples: number;
	solverMethod: string;
	// Intervention policies
	interventionPolicyGroups: InterventionPolicyGroup[];
	// Constraints
	targetVariables: string[];
	statistic: string;
	numSamples: number;
	riskTolerance: number;
	aboveOrBelow: string;
	threshold: number;
	isMinimized: boolean;
	chartConfigs: string[][];
	simulationsInProgress: string[];
	simulationRunId: string;
	modelConfigName: string;
	modelConfigDesc: string;
}

export const blankInterventionPolicyGroup: InterventionPolicyGroup = {
	borderColour: '#cee2a4',
	name: 'Policy bounds',
	parameter: '',
	costBenefitFn: '',
	startTime: 0,
	lowerBound: 0,
	upperBound: 0,
	initialGuess: 0,
	isActive: true
};

export const OptimizeCiemssOperation: Operation = {
	name: WorkflowOperationTypes.OPTIMIZE_CIEMSS,
	description: 'Optimize a model',
	displayName: 'Optimize model',
	inputs: [{ type: 'modelConfigId', label: 'Model configuration', acceptMultiple: false }],
	outputs: [{ type: 'modelConfigId' }],
	isRunnable: true,

	initState: () => {
		const init: OptimizeCiemssOperationState = {
			startTime: 0,
			endTime: 10,
			numTimePoints: 10,
			timeUnit: '',
			numStochasticSamples: 5,
			solverMethod: 'euler',
			interventionPolicyGroups: [blankInterventionPolicyGroup],
			targetVariables: [],
			statistic: 'mean',
			numSamples: 10,
			riskTolerance: 95,
			aboveOrBelow: 'Below',
			threshold: 0,
			isMinimized: true,
			chartConfigs: [],
			simulationsInProgress: [],
			simulationRunId: '',
			modelConfigName: '',
			modelConfigDesc: ''
		};
		return init;
	}
};
