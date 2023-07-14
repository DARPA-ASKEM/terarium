import { Operation, WorkflowOperationTypes } from '@/types/workflow';

export interface DatasetOperationState {
	datasetId: string | null;
}

export const DatasetOperation: Operation = {
	name: WorkflowOperationTypes.DATASET,
	displayName: 'Dataset',
	description: 'Select a dataset',
	isRunnable: true,
	inputs: [],
	outputs: [{ type: 'datasetId' }],
	action: () => {},

	initState: () => {
		const init: DatasetOperationState = {
			datasetId: null
		};
		return init;
	}
};
