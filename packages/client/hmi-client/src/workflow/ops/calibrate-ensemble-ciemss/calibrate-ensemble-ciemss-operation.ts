import { Operation, WorkflowOperationTypes } from '@/types/workflow';
// import type { EnsembleRequest } from '@/types/Types';
// import { makeEnsembleJob } from '@/services/models/simulation-service';
import { ChartConfig } from '@/types/SimulateConfig';
import type { EnsembleModelConfigs } from '@/types/Types';

export interface EnsembleCalibrateExtraCiemss {
	solverMethod: string;
	numParticles: number; // The number of particles to use for the inference algorithm. https://github.com/ciemss/pyciemss/blob/1fc62b0d4b0870ca992514ad7a9b7a09a175ce44/pyciemss/interfaces.py#L225
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
		{ type: 'datasetId', label: 'Dataset' },
		{ type: 'modelConfigId', label: 'Model configuration', acceptMultiple: false }
	],
	outputs: [{ type: 'simulationId' }],
	isRunnable: true,

	initState: () => {
		const init: CalibrateEnsembleCiemssOperationState = {
			chartConfigs: [],
			mapping: [],
			extra: {
				solverMethod: 'dopri5',
				numParticles: 10,
				numIterations: 100
			},
			simulationsInProgress: []
		};
		return init;
	}
};
