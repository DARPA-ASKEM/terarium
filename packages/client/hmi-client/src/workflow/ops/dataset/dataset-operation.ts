import { Operation, WorkflowOperationTypes } from '@/types/workflow';

export interface DatasetOperationState {
	datasetId: string | null;
}

export const DatasetOperation: Operation = {
	name: WorkflowOperationTypes.DATASET,
	description: 'Select a dataset',
	displayName: 'Dataset',
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
