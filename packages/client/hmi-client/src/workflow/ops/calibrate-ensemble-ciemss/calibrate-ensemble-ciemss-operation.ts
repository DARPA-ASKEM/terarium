import { Operation, WorkflowOperationTypes } from '@/types/workflow';
// import type { EnsembleRequest } from '@/types/Types';
// import { makeEnsembleJob } from '@/services/models/simulation-service';
import { ChartConfig } from '@/types/SimulateConfig';
import type { EnsembleModelConfigs } from '@/types/Types';

export interface EnsembleCalibrateExtraCiemss {
	numSamples: number;
	totalPopulation: number;
	numIterations: number;
}

export interface CalibrateEnsembleCiemssOperationState {
	chartConfigs: ChartConfig[];
	mapping: EnsembleModelConfigs[];
	extra: EnsembleCalibrateExtraCiemss;
	simulationsInProgress: string[];
}

export const CalibrateEnsembleCiemssOperation: Operation = {
	name: WorkflowOperationTypes.CALIBRATE_ENSEMBLE_CIEMSS,
	displayName: 'Calibrate ensemble',
	description: '',
	inputs: [
		{ type: 'modelConfigId', label: 'Model configuration', acceptMultiple: true },
		{ type: 'datasetId', label: 'Dataset' }
	],
	outputs: [{ type: 'simulationId' }],
	isRunnable: true,

	// TODO: Figure out mapping
	// Calls API, returns results.
	action: async (): Promise<void> => {
		console.log('test');
	},

	initState: () => {
		const init: CalibrateEnsembleCiemssOperationState = {
			chartConfigs: [],
			mapping: [],
			extra: {
				numSamples: 50,
				totalPopulation: 1000,
				numIterations: 10
			},
			simulationsInProgress: []
		};
		return init;
	}
};
