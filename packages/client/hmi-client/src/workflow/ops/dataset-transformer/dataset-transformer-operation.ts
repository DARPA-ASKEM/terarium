import { Operation, WorkflowOperationTypes } from '@/types/workflow';

export interface DatasetOperationState {
	datasetId: string | null;
}

export const DatasetTransformerOperation: Operation = {
	name: WorkflowOperationTypes.DATASET_TRANSFORMER,
	description: 'Select a dataset',
	displayName: 'Dataset Transformer',
	isRunnable: true,
	inputs: [{ type: 'datasetId', label: 'Dataset' }],
	outputs: [],
	action: () => {},

	initState: () => {
		const init: DatasetOperationState = {
			datasetId: null
		};
		return init;
	}
};
