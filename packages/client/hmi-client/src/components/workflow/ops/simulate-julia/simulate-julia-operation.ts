import type { TimeSpan } from '@/types/Types';
import { Operation, WorkflowOperationTypes, BaseState } from '@/types/workflow';

export interface SimulateJuliaOperationState extends BaseState {
	// state shared across all runs
	chartConfigs: string[][];

	// state specific to individual simulate runs
	currentTimespan: TimeSpan;

	// In progress
	inProgressSimulationId: string;
}

export const SimulateJuliaOperation: Operation = {
	name: WorkflowOperationTypes.SIMULATE_JULIA,
	displayName: 'Simulate with SciML',
	description: 'given a model id, and configuration id, run a simulation',
	inputs: [{ type: 'modelConfigId', label: 'Model configuration', acceptMultiple: false }],
	outputs: [{ type: 'simulationId' }],
	isRunnable: true,

	initState: () => {
		const init: SimulateJuliaOperationState = {
			chartConfigs: [],
			currentTimespan: { start: 1, end: 100 },
			inProgressSimulationId: ''
		};
		return init;
	},

	// TODO: Figure out mapping
	// Calls API, returns results.
	action: async (): Promise<void> => {
		console.log('test');
	}
};
