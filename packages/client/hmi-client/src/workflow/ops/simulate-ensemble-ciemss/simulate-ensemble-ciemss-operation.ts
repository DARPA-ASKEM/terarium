import { Operation, WorkflowOperationTypes } from '@/types/workflow';
import { ChartConfig } from '@/types/SimulateConfig';
import type { EnsembleModelConfigs, TimeSpan } from '@/types/Types';

export interface SimulateEnsembleCiemssOperationState {
	modelConfigIds: string[];
	chartConfigs: ChartConfig[];
	mapping: EnsembleModelConfigs[];
	timeSpan: TimeSpan;
	numSamples: number;
	inProgressSimulationId: string;
}

export const SimulateEnsembleCiemssOperation: Operation = {
	name: WorkflowOperationTypes.SIMULATE_ENSEMBLE_CIEMSS,
	displayName: 'Simulate ensemble',
	description: '',
	inputs: [{ type: 'modelConfigId', label: 'Model configuration', acceptMultiple: false }],
	outputs: [{ type: 'simulationId' }],
	isRunnable: true,

	// TODO: Figure out mapping
	// Calls API, returns results.
	action: async (): Promise<void> => {
		console.log('test');
	},

	initState: () => {
		const init: SimulateEnsembleCiemssOperationState = {
			modelConfigIds: [],
			chartConfigs: [],
			mapping: [],
			timeSpan: { start: 0, end: 40 },
			numSamples: 40,
			inProgressSimulationId: ''
		};
		return init;
	}
};
