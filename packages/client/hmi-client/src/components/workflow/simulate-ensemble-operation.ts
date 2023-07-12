import { Operation, WorkflowOperationTypes } from '@/types/workflow';
// import { EnsembleRequest } from '@/types/Types';
// import { makeEnsembleJob } from '@/services/models/simulation-service';
import { ChartConfig } from '@/types/SimulateConfig';
import { EnsembleModelConfigs } from '@/types/Types';

export interface EnsembleOperationState {
	modelConfigIds: string[];
	chartConfigs: ChartConfig[];
	mapping: EnsembleModelConfigs[];
}

export const EnsembleOperation: Operation = {
	name: WorkflowOperationTypes.SIMULATEENSEMBLE,
	description: '',
	inputs: [{ type: 'modelConfigId', acceptMultiple: true }],
	outputs: [{ type: 'number' }],
	isRunnable: true,

	// TODO: Figure out mapping
	// Calls API, returns results.
	action: async (): Promise<void> => {
		console.log('test');
	},

	initState: () => {
		const init: EnsembleOperationState = {
			modelConfigIds: [],
			chartConfigs: [],
			mapping: []
		};
		return init;
	}
};
