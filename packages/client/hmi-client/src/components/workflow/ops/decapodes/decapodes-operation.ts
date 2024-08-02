import { Operation, BaseState } from '@/types/workflow';

export interface CodeHistory {
	code: string;
	timestamp: number;
}
export interface DecapodesOperationState extends BaseState {
	codeHistory: CodeHistory[];
}

export const DecapodesOperation: Operation = {
	name: 'decapodes',
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
