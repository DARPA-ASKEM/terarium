import { ChartSetting } from '@/types/common';
import type { TimeSpan } from '@/types/Types';
import { Operation, WorkflowOperationTypes, BaseState } from '@/types/workflow';
import simulateProbabilistic from '@assets/svg/operator-images/simulate-probabilistic.svg';

const DOCUMENTATION_URL = 'https://github.com/ciemss/pyciemss/blob/main/pyciemss/interfaces.py#L323';

export interface SimulateCiemssOperationState extends BaseState {
	// state shared across all runs
	chartSettings: ChartSetting[] | null; // null indicates that the chart settings have not been set yet

	// state specific to individual simulate runs
	currentTimespan: TimeSpan;
	numSamples: number;
	method: string;
	forecastId: string; // Completed run's Id

	// In progress
	inProgressForecastId: string;

	errorMessage: { name: string; value: string; traceback: string };
}

export const SimulateCiemssOperation: Operation = {
	name: WorkflowOperationTypes.SIMULATE_CIEMSS,
	displayName: 'Simulate',
	description: 'given a model id, and configuration id, run a simulation',
	documentationUrl: DOCUMENTATION_URL,
	imageUrl: simulateProbabilistic,
	inputs: [
		{ type: 'modelConfigId', label: 'Model configuration' },
		{
			type: 'policyInterventionId',
			label: 'Interventions',
			isOptional: true
		}
	],
	outputs: [{ type: 'datasetId' }],
	isRunnable: true,

	initState: () => {
		const init: SimulateCiemssOperationState = {
			chartSettings: null,
			currentTimespan: { start: 0, end: 100 },
			numSamples: 100,
			method: 'dopri5',
			forecastId: '',
			inProgressForecastId: '',
			errorMessage: { name: '', value: '', traceback: '' }
		};
		return init;
	}
};
