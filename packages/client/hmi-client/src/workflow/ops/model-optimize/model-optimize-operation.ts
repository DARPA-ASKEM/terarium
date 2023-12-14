import { Operation, WorkflowOperationTypes } from '@/types/workflow';

export interface InterventionPolicyGroup {
	borderColour: string;
	name: string;
	parameter: string;
	goal: string;
	costBenefitFn: string;
	startTime: number;
	lowerBound: number;
	upperBound: number;
	isActive: boolean;
}

export interface ModelOptimizeOperationState {
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
	targetVariable: string;
	statistic: string;
	numDays: number;
	riskTolerance: number;
	aboveOrBelow: string;
	threshold: number;
}

export const blankInterventionPolicyGroup: InterventionPolicyGroup = {
	borderColour: '#cee2a4',
	name: 'Policy bounds',
	parameter: '',
	goal: '',
	costBenefitFn: '',
	startTime: 0,
	lowerBound: 0,
	upperBound: 0,
	isActive: true
};

export const ModelOptimizeOperation: Operation = {
	name: WorkflowOperationTypes.MODEL_OPTIMIZE,
	description: 'Optimize a model',
	displayName: 'Optimize model',
	inputs: [{ type: 'modelConfigId', label: 'Model configuration', acceptMultiple: false }],
	outputs: [{ type: 'modelConfigId' }],
	isRunnable: true,

	initState: () => {
		const init: ModelOptimizeOperationState = {
			startTime: 0,
			endTime: 0,
			numTimePoints: 0,
			timeUnit: '',
			numStochasticSamples: 0,
			solverMethod: '',
			interventionPolicyGroups: [blankInterventionPolicyGroup],
			targetVariable: '',
			statistic: '',
			numDays: 0,
			riskTolerance: 0,
			aboveOrBelow: '',
			threshold: 0
		};
		return init;
	}
};
