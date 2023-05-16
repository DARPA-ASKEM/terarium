import { Operation } from '@/types/workflow';

export const SimulateOperation: Operation = {
	name: 'SimulateOperation',
	description: 'given a model id, and configuration id, run a simulation',
	inputs: [{ type: 'modelConfig' }],
	outputs: [],
	isRunnable: true,

	// TODO: Figure out mapping
	// Calls API, returns results.
	action: async (): Promise<void> => {
		console.log('test');
	}
};
