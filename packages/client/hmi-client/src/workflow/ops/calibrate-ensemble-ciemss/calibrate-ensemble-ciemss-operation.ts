import { Operation, WorkflowOperationTypes } from '@/types/workflow';
import type { EnsembleModelConfigs } from '@/types/Types';

export interface EnsembleCalibrateExtraCiemss {
	solverMethod: string;
	numParticles: number; // The number of particles to use for the inference algorithm. https://github.com/ciemss/pyciemss/blob/1fc62b0d4b0870ca992514ad7a9b7a09a175ce44/pyciemss/interfaces.py#L225
	numIterations: number;
}

export interface CalibrateEnsembleCiemssOperationState {
	chartConfigs: string[][];
	ensembleConfigs: EnsembleModelConfigs[];
	timestampColName: string;
	extra: EnsembleCalibrateExtraCiemss;
	inProgressCalibrationId: string;
	inProgressForecastId: string;
	calibrationId: string;
	forecastRunId: string;
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
			ensembleConfigs: [],
			timestampColName: '',
			extra: {
				solverMethod: 'dopri5',
				numParticles: 10,
				numIterations: 100
			},
			inProgressCalibrationId: '',
			inProgressForecastId: '',
			calibrationId: '',
			forecastRunId: ''
		};
		return init;
	}
};
