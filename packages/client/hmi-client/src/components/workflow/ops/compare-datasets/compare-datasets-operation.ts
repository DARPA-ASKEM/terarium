import { WorkflowOperationTypes } from '@/types/workflow';
import type { Operation, BaseState } from '@/types/workflow';

export interface CompareDatasetsState extends BaseState {}

export const CompareDatasetsOperation: Operation = {
	name: WorkflowOperationTypes.COMPARE_DATASETS,
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
		const init: CompareDatasetsState = {};
		return init;
	}
};
