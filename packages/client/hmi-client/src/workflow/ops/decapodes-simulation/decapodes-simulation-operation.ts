import { Operation, WorkflowOperationTypes } from '@/types/workflow';

export interface DecapodesSimulationState {}

export const DecapodesSimulationOperation: Operation = {
	name: WorkflowOperationTypes.DECAPODES_SIMULATION,
	displayName: 'Decapodes Simulation',
	description: 'Decapodes Simulation',
	inputs: [{ type: 'modelId', label: 'Model', acceptMultiple: false }],
	outputs: [],
	isRunnable: false,
	action: () => {},
	initState: () => {
		const init: DecapodesSimulationState = {};
		return init;
	}
};
