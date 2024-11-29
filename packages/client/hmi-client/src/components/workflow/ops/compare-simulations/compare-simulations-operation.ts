import { WorkflowOperationTypes } from '@/types/workflow';
import type { Operation, BaseState } from '@/types/workflow';

export interface CompareSimulationsState extends BaseState {}

export const CompareSimulationsOperation: Operation = {
	name: WorkflowOperationTypes.COMPARE_SIMULATIONS,
	displayName: 'Compare simulations',
	description: 'Compare simulations',
	documentationUrl: '',
	inputs: [
		{ type: 'datasetId', label: 'Dataset' },
		{ type: 'datasetId', label: 'Dataset' }
	],
	outputs: [{ type: 'datasetId', label: 'Dataset' }],
	isRunnable: true,
	action: () => {},
	initState: () => {
		const init: CompareSimulationsState = {};
		return init;
	}
};
