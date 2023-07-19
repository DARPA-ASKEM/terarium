import { Operation, WorkflowOperationTypes } from '@/types/workflow';
// import { EnsembleRequest } from '@/types/Types';
// import { makeEnsembleJob } from '@/services/models/simulation-service';
import { ChartConfig } from '@/types/SimulateConfig';
import { EnsembleModelConfigs, TimeSpan } from '@/types/Types';

export interface EnsembleCalibrateExtraCiemss {
	numSamples: number;
	totalPopulation: number;
	numIterations: number;
}

export interface CalibrateEnsembleCiemssOperationState {
	modelConfigIds: string[];
	chartConfigs: ChartConfig[];
	mapping: EnsembleModelConfigs[];
	timeSpan: TimeSpan;
	extra: EnsembleCalibrateExtraCiemss;
}

export const CalibrateEnsembleCiemssOperation: Operation = {
	name: WorkflowOperationTypes.CALIBRATE_ENSEMBLE_CIEMSS,
	displayName: 'Simulate ensemble (probabilistic)',
	description: '',
	inputs: [
		{ type: 'modelConfigId', label: 'Model configuration', acceptMultiple: true },
		{ type: 'datasetId', label: 'Dataset' }
	],
	outputs: [{ type: 'number' }],
	isRunnable: true,

	// TODO: Figure out mapping
	// Calls API, returns results.
	action: async (): Promise<void> => {
		console.log('test');
	},

	initState: () => {
		const init: CalibrateEnsembleCiemssOperationState = {
			modelConfigIds: [],
			chartConfigs: [],
			mapping: [],
			timeSpan: { start: 0, end: 40 },
			extra: {
				numSamples: 50,
				totalPopulation: 1000,
				numIterations: 10
			}
		};
		return init;
	}
};
