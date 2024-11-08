import { Operation, WorkflowOperationTypes, BaseState } from '@/types/workflow';
import type { TimeSpan } from '@/types/Types';
import simulateEnsembleCiemss from '@assets/svg/operator-images/simulate-ensemble-probabilistic.svg';

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

export interface SimulateEnsembleCiemssOperationState extends BaseState {
	chartConfigs: string[][];
	mapping: SimulateEnsembleMappingRow[];
	weights: SimulateEnsembleWeight[];
	timeSpan: TimeSpan;
	numSamples: number;
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
			timeSpan: { start: 0, end: 40 },
			numSamples: 40,
			inProgressForecastId: '',
			forecastId: '',
			errorMessage: { name: '', value: '', traceback: '' }
		};
		return init;
	}
};
