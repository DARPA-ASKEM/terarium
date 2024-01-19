import { WorkflowOperationTypes } from '@/types/Types';
import { Operation } from '@/types/workflow';

export interface DatasetTransformerState {
	datasetId: string | null;
	notebookSessionId?: string;
}

export const DatasetTransformerOperation: Operation = {
	name: WorkflowOperationTypes.DatasetTransformer,
	description: 'Select a dataset',
	displayName: 'Dataset Transformer',
	isRunnable: true,
	inputs: [{ type: 'datasetId', label: 'Dataset' }],
	outputs: [],
	action: () => {},

	initState: () => {
		const init: DatasetTransformerState = {
			datasetId: null
		};
		return init;
	}
};
