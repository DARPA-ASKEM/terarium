import { Operation, WorkflowOperationTypes } from '@/types/workflow';

export interface CodeHistory {
	code: string;
	timestamp: number;
}
export interface DecapodesOperationState {
	codeHistory: CodeHistory[];
}

export const DecapodesOperation: Operation = {
	name: WorkflowOperationTypes.DECAPODES,
	displayName: 'Decapodes',
	description: 'Decapodes notebook.',
	isRunnable: true,
	inputs: [{ type: 'modelId' }],
	outputs: [],
	action: async () => ({}),

	initState: () => {
		const init: DecapodesOperationState = {
			codeHistory: []
		};
		return init;
	}
};
