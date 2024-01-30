import { Operation, WorkflowOperationTypes } from '@/types/workflow';

export interface SubsetDataOperationState {
	datasetId: string | null;
	notebookSessionId?: string;
}

export const SubsetDataOperation: Operation = {
	name: WorkflowOperationTypes.SUBSET_DATA,
	displayName: 'Subset data',
	description: '',
	inputs: [{ type: 'datasetId', label: 'Dataset' }],
	outputs: [{ type: 'datasetId' }],
	isRunnable: true,
	action: () => {},
	initState: () => {
		const init: SubsetDataOperationState = {
			datasetId: null
		};
		return init;
	}
};
