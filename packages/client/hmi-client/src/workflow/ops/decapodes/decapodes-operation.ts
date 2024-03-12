import { Operation, WorkflowOperationTypes } from '@/types/workflow';

export interface DecapodesOperationState {
	modelId: string | null;
	modelConfigurationIds: string[];
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
			modelId: null,
			modelConfigurationIds: []
		};
		return init;
	}
};
