import { Operation, WorkflowOperationTypes, BaseState } from '@/types/workflow';
import simulateEnsembleCiemss from '@assets/svg/operator-images/simulate-ensemble-probabilistic.svg';
import { CiemssMethodOptions } from '@/services/models/simulation-service';

const DOCUMENTATION_URL = 'https://github.com/ciemss/pyciemss/blob/main/pyciemss/interfaces.py#L35';

export interface SimulateEnsembleMapEntry {
	modelConfigId: string;
	compartmentName: string; // State or Obs that is being mapped to newName
}

export interface SimulateEnsembleMappingRow {
	id: string; // uuid that can be used as a row key
	newName: string; // This is the new name provided by the user.
	modelConfigurationMappings: SimulateEnsembleMapEntry[];
}

export interface SimulateEnsembleWeight {
	modelConfigurationId: string;
	value: number;
}

export const speedValues = Object.freeze({
	numSamples: 1,
	method: CiemssMethodOptions.euler
});

export const normalValues = Object.freeze({
	numSamples: 100,
	method: CiemssMethodOptions.dopri5
});

export interface SimulateEnsembleCiemssOperationState extends BaseState {
	chartConfigs: string[][];
	mapping: SimulateEnsembleMappingRow[];
	weights: SimulateEnsembleWeight[];
	endTime: number;
	numSamples: number;
	method: CiemssMethodOptions;
	inProgressForecastId: string;
	forecastId: string; // Completed run's Id
	errorMessage: { name: string; value: string; traceback: string };
}

export const SimulateEnsembleCiemssOperation: Operation = {
	name: WorkflowOperationTypes.SIMULATE_ENSEMBLE_CIEMSS,
	displayName: 'Simulate ensemble',
	description: '',
	documentationUrl: DOCUMENTATION_URL,
	imageUrl: simulateEnsembleCiemss,
	inputs: [{ type: 'modelConfigId', label: 'Model configuration' }],
	outputs: [{ type: 'datasetId' }],
	isRunnable: true,

	// TODO: Figure out mapping
	// Calls API, returns results.
	action: async (): Promise<void> => {
		console.log('test');
	},

	initState: () => {
		const init: SimulateEnsembleCiemssOperationState = {
			chartConfigs: [],
			mapping: [],
			weights: [],
			endTime: 100,
			numSamples: normalValues.numSamples,
			method: normalValues.method,
			inProgressForecastId: '',
			forecastId: '',
			errorMessage: { name: '', value: '', traceback: '' }
		};
		return init;
	}
};
