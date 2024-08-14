import { Operation, WorkflowOperationTypes, BaseState } from '@/types/workflow';
import { CalibrateMap } from '@/services/calibrate-workflow';
import calibrateSimulateCiemss from '@assets/svg/operator-images/calibrate-simulate-probabilistic.svg';

const DOCUMENTATION_URL = 'https://github.com/ciemss/pyciemss/blob/main/pyciemss/interfaces.py#L529';

export interface CalibrationOperationStateCiemss extends BaseState {
	method: string;
	selectedParameters: string[]; // Output display setting
	selectedInterventionVariables: string[]; // Output display setting
	selectedSimulationVariables: string[]; // Output display setting

	mapping: CalibrateMap[];
	simulationsInProgress: string[];

	currentProgress: number;
	inProgressCalibrationId: string;
	inProgressPreForecastId: string;
	inProgressForecastId: string;
	errorMessage: { name: string; value: string; traceback: string };

	calibrationId: string;
	preForecastId: string;
	forecastId: string;
	numIterations: number;
	numSamples: number;
	endTime: number;
	stepSize: number;
	learningRate: number;
}

export const CalibrationOperationCiemss: Operation = {
	name: WorkflowOperationTypes.CALIBRATION_CIEMSS,
	displayName: 'Calibrate',
	description:
		'given a model id, a dataset id, and optionally a configuration. calibrate the models initial values and rates',
	documentationUrl: DOCUMENTATION_URL,
	imageUrl: calibrateSimulateCiemss,
	inputs: [
		{ type: 'modelConfigId', label: 'Model configuration' },
		{ type: 'datasetId', label: 'Dataset' },
		{
			type: 'policyInterventionId',
			label: 'Interventions',
			isOptional: true
		}
	],
	outputs: [{ type: 'modelConfigId|datasetId' }],
	isRunnable: true,

	action: async () => {},

	initState: () => {
		const init: CalibrationOperationStateCiemss = {
			method: 'dopri5',
			selectedParameters: [],
			selectedInterventionVariables: [],
			selectedSimulationVariables: [],
			mapping: [{ modelVariable: '', datasetVariable: '' }],
			simulationsInProgress: [],
			currentProgress: 0,
			inProgressPreForecastId: '',
			inProgressCalibrationId: '',
			inProgressForecastId: '',
			calibrationId: '',
			preForecastId: '',
			forecastId: '',
			errorMessage: { name: '', value: '', traceback: '' },
			numIterations: 100,
			numSamples: 100,
			endTime: 100,
			stepSize: 1,
			learningRate: 0.03
		};
		return init;
	}
};
