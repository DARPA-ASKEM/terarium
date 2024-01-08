import { Operation, WorkflowOperationTypes } from '@/types/workflow';
import { TimeSpan, FunmanInterval } from '@/types/Types';

export interface ConstraintGroup {
	borderColour: string;
	name: string;
	variables: string[]; // If len = 1, need to rename to "variable" for request formatting
	weights?: number[]; // 1 to 1 mapping with variables
	timepoints?: FunmanInterval;
	interval?: FunmanInterval;
}

export interface RequestParameter {
	name: string;
	interval?: FunmanInterval;
	label: string;
}

export interface FunmanOperationState {
	currentTimespan: TimeSpan;
	numSteps: number;
	tolerance: number;
	constraintGroups: ConstraintGroup[];
	requestParameters: RequestParameter[];
}

export const FunmanOperation: Operation = {
	name: WorkflowOperationTypes.FUNMAN,
	displayName: 'Validate model configuration',
	description: 'Validate model configuration',
	inputs: [{ type: 'modelConfigId', label: 'Model configuration', acceptMultiple: false }],
	outputs: [{ type: 'funmanQueryId' }],
	isRunnable: true,
	action: () => {},
	initState: () => {
		const init: FunmanOperationState = {
			currentTimespan: { start: 0, end: 100 },
			numSteps: 10,
			tolerance: 0.89,
			constraintGroups: [],
			requestParameters: []
		};
		return init;
	}
};
