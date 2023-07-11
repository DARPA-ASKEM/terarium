import { TimeSpan } from '@/types/Types';
import { Operation, WorkflowOperationTypes } from '@/types/workflow';
import { ChartConfig } from '@/types/SimulateConfig';

export interface SimulateOperationState {
	chartConfigs: ChartConfig[];
	currentTimespan: TimeSpan;
}

export const SimulateJuliaOperation: Operation = {
	name: WorkflowOperationTypes.SIMULATE_JULIA,
	description: 'given a model id, and configuration id, run a simulation',
	inputs: [{ type: 'modelConfigId', acceptMultiple: true }],
	outputs: [{ type: 'simOutput' }],
	isRunnable: true,

	initState: () => {
		const init: SimulateOperationState = {
			chartConfigs: [],
			currentTimespan: { start: 1, end: 100 }
		};
		return init;
	},

	// TODO: Figure out mapping
	// Calls API, returns results.
	action: async (): Promise<void> => {
		console.log('test');
	}
};
