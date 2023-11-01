import { Operation, WorkflowOperationTypes } from '@/types/workflow';
import { TimeSpan } from '@/types/Types';

export interface ConstraintGroup {
	borderColour: string;
	name: string;
	target: string;
	currentTimespan: TimeSpan;
	lowerBound?: number;
	upperBound?: number;
}

export interface FunmanOperationState {
	currentTimespan: TimeSpan;
	numSteps: number;
	tolerance: number;
	constraintGroups: ConstraintGroup[];
}

export const FunmanOperation: Operation = {
	name: WorkflowOperationTypes.FUNMAN,
	displayName: 'Validate model configuration',
	description: 'Validate model configuration',
	inputs: [{ type: 'modelConfigId', label: 'Model configuration', acceptMultiple: false }],
	outputs: [],
	isRunnable: true,
	action: () => {},
	initState: () => {
		const init: FunmanOperationState = {
			currentTimespan: { start: 0, end: 100 },
			numSteps: 10,
			tolerance: 0.89,
			constraintGroups: [
				{
					borderColour: '#c300a6',
					name: '',
					currentTimespan: { start: 0, end: 100 },
					target: 'All variables',
					lowerBound: 0
				}
			]
		};
		return init;
	}
};
