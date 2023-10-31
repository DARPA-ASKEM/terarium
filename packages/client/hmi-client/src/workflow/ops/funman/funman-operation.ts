import { Operation, WorkflowOperationTypes } from '@/types/workflow';
import { TimeSpan } from '@/types/Types';

export interface FunmanOperationState {
	currentTimespan: TimeSpan;
	dummy: String;
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
			dummy: 'Temp',
			currentTimespan: { start: 0, end: 100 }
		};
		return init;
	}
};
