import type { TimeSpan } from '@/types/Types';
import { Operation, BaseState } from '@/types/workflow';

const DOCUMENTATION_URL = 'https://github.com/DARPA-ASKEM/sciml-service/blob/main/src/operations.jl#L222';

export interface SimulateJuliaOperationState extends BaseState {
	// state shared across all runs
	chartConfigs: string[][];

	// state specific to individual simulate runs
	currentTimespan: TimeSpan;

	// In progress
	inProgressSimulationId: string;
}

export const SimulateJuliaOperation: Operation = {
	name: 'simulateJulia',
	displayName: 'Simulate with SciML',
	description: 'given a model id, and configuration id, run a simulation',
	documentationUrl: DOCUMENTATION_URL,
	inputs: [{ type: 'modelConfigId', label: 'Model configuration' }],
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
