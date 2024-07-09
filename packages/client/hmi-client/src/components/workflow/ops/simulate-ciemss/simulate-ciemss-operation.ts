import type { TimeSpan } from '@/types/Types';
import { Operation, WorkflowOperationTypes, BaseState } from '@/types/workflow';

const DOCUMENTATION_URL =
	'https://github.com/ciemss/pyciemss/blob/main/pyciemss/interfaces.py#L323';

export interface SimulateCiemssOperationState extends BaseState {
	// state shared across all runs
	chartConfigs: string[][];

	// state specific to individual simulate runs
	currentTimespan: TimeSpan;
	numSamples: number;
	method: string;

	// In progress
	inProgressSimulationId: string;

	errorMessage: { name: string; value: string; traceback: string };
}

export const SimulateCiemssOperation: Operation = {
	name: WorkflowOperationTypes.SIMULATE_CIEMSS,
	displayName: 'Simulate with PyCIEMSS',
	description: 'given a model id, and configuration id, run a simulation',
	documentationUrl: DOCUMENTATION_URL,
	inputs: [
		{ type: 'modelConfigId', label: 'Model configuration', acceptMultiple: false },
		{
			type: 'calibrateSimulationId',
			label: 'Calibration',
			acceptMultiple: false,
			isOptional: true
		},
		{
			type: 'policyInterventionId',
			label: 'Interventions',
			acceptMultiple: false,
			isOptional: true
		}
	],
	outputs: [{ type: 'simulationId' }],
	isRunnable: true,

	initState: () => {
		const init: SimulateCiemssOperationState = {
			chartConfigs: [],
			currentTimespan: { start: 0, end: 100 },
			numSamples: 100,
			method: 'dopri5',
			inProgressSimulationId: '',
			errorMessage: { name: '', value: '', traceback: '' }
		};
		return init;
	}
};
