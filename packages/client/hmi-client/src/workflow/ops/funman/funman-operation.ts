import { Operation, WorkflowOperationTypes, BaseState } from '@/types/workflow';
import type { FunmanInterval, TimeSpan } from '@/types/Types';

export interface ConstraintGroup {
	borderColour: string;
	name: string;

	// One of
	// - monotonicityConstraint
	// - stateConstraint
	constraintType: string;

	variables: string[]; // If len = 1, need to rename to "variable" for request formatting
	weights?: number[]; // 1 to 1 mapping with variables
	timepoints?: FunmanInterval;
	interval?: FunmanInterval;

	derivativeType?: string;
}

export interface RequestParameter {
	name: string;
	interval?: FunmanInterval;
	label: string;
}

export interface FunmanOperationState extends BaseState {
	currentTimespan: TimeSpan;
	numSteps: number;
	tolerance: number;
	useCompartmentalConstraint: boolean;
	constraintGroups: ConstraintGroup[];
	requestParameters: RequestParameter[];
}

export const FunmanOperation: Operation = {
	name: WorkflowOperationTypes.FUNMAN,
	displayName: 'Validate configuration',
	description: 'Validate configuration',
	inputs: [{ type: 'modelConfigId', label: 'Model configuration', acceptMultiple: false }],
	outputs: [{ type: 'funmanQueryId' }],
	isRunnable: true,
	action: () => {},
	initState: () => {
		const init: FunmanOperationState = {
			currentTimespan: { start: 0, end: 100 },
			numSteps: 10,
			tolerance: 0.2,
			constraintGroups: [],
			requestParameters: [],
			useCompartmentalConstraint: true
		};
		return init;
	}
};
