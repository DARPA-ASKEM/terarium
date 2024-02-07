import type { TimeSpan } from '@/types/Types';
import { Operation, WorkflowOperationTypes } from '@/types/workflow';

export interface SimulateCiemssOperationState {
	// state shared across all runs
	chartConfigs: string[][];

	// state specific to individual simulate runs
	currentTimespan: TimeSpan;
	numSamples: number;
	method: string;
	simulationsInProgress: string[];
}

export const SimulateCiemssOperation: Operation = {
	name: WorkflowOperationTypes.SIMULATE_CIEMSS,
	displayName: 'Simulate (probabilistic)',
	description: 'given a model id, and configuration id, run a simulation',
	inputs: [
		{ type: 'modelConfigId', label: 'Model configuration', acceptMultiple: false },
		{ type: 'calibrateSimulationId', label: 'Calibration', acceptMultiple: false }
	],
	outputs: [{ type: 'simOutput' }],
	isRunnable: true,

	initState: () => {
		const init: SimulateCiemssOperationState = {
			chartConfigs: [],
			currentTimespan: { start: 1, end: 100 },
			numSamples: 100,
			method: 'dopri5',
			simulationsInProgress: []
		};
		return init;
	},

	// TODO: Figure out mapping
	// Calls API, returns results.
	action: async (): Promise<void> => {
		console.log('test');
	}
};
