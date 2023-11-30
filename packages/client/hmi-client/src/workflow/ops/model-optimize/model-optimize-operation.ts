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
	startTime: number;
	endTime: number;
	numTimePoints: number;
	timeUnit: string;
	numStochasticSamples: number;
	solverMethod: string;
	interventionPolicyGroups: InterventionPolicyGroup[];
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
			interventionPolicyGroups: [blankInterventionPolicyGroup]
		};
		return init;
	}
};
