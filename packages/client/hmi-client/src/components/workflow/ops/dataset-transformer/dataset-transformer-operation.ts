import { Operation, WorkflowOperationTypes, BaseState } from '@/types/workflow';

const DOCUMENTATION_URL = 'https://pandas.pydata.org/docs/user_guide/index.html#user-guide';

export interface DatasetTransformerState extends BaseState {
	datasetId: string | null;
	notebookSessionId?: string;
}

export const DatasetTransformerOperation: Operation = {
	name: WorkflowOperationTypes.DATASET_TRANSFORMER,
	description: 'Select a dataset',
	documentationUrl: DOCUMENTATION_URL,
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
