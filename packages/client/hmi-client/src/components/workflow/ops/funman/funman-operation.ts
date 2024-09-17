import { Operation, WorkflowOperationTypes, BaseState } from '@/types/workflow';
import type { FunmanInterval, TimeSpan } from '@/types/Types';

const DOCUMENTATION_URL = 'https://github.com/siftech/funman';

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
	inProgressId: string;
	useCompartmentalConstraint: boolean;
	constraintGroups: ConstraintGroup[];
	requestParameters: RequestParameter[];

	// selected state in ouptut
	trajectoryState?: string;
}

export const FunmanOperation: Operation = {
	name: WorkflowOperationTypes.FUNMAN,
	displayName: 'Validate configuration',
	description: 'Validate configuration',
	documentationUrl: DOCUMENTATION_URL,
	inputs: [{ type: 'modelConfigId', label: 'Model configuration' }],
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
			useCompartmentalConstraint: true,
			inProgressId: ''
		};
		return init;
	}
};
