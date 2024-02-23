import { Operation, WorkflowOperationTypes } from '@/types/workflow';

export interface DatasetTransformerState {
	datasetId: string | null;
	notebookSessionId?: string;
}

export const DatasetTransformerOperation: Operation = {
	name: WorkflowOperationTypes.DATASET_TRANSFORMER,
	description: 'Select a dataset',
	displayName: 'Transform dataset',
	isRunnable: true,
	inputs: [{ type: 'datasetId|simulationId', label: 'Dataset or Simulation' }],
	outputs: [],
	action: () => {},

	initState: () => {
		const init: DatasetTransformerState = {
			datasetId: null
		};
		return init;
	}
};
