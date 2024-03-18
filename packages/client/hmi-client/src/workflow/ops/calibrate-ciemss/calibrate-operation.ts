import { Operation, WorkflowOperationTypes } from '@/types/workflow';
import { ChartConfig } from '@/types/SimulateConfig';
import { CalibrateMap } from '@/services/calibrate-workflow';

export interface CalibrationOperationStateCiemss {
	chartConfigs: ChartConfig[];
	mapping: CalibrateMap[];
	simulationsInProgress: string[];

	inProgressCalibrationId: string;
	inProgressForecastId: string;

	calibrationId: string;
	forecastId: string;
}

export const CalibrationOperationCiemss: Operation = {
	name: WorkflowOperationTypes.CALIBRATION_CIEMSS,
	displayName: 'Calibrate with PyCIEMSS',
	description:
		'given a model id, a dataset id, and optionally a configuration. calibrate the models initial values and rates',
	inputs: [
		{ type: 'modelConfigId', label: 'Model configuration' },
		{ type: 'datasetId', label: 'Dataset' }
	],
	outputs: [{ type: 'simulationId' }],
	isRunnable: true,

	action: async () => {},

	initState: () => {
		const init: CalibrationOperationStateCiemss = {
			chartConfigs: [],
			mapping: [{ modelVariable: '', datasetVariable: '' }],
			simulationsInProgress: [],
			inProgressCalibrationId: '',
			inProgressForecastId: '',
			calibrationId: '',
			forecastId: ''
		};
		return init;
	}
};
