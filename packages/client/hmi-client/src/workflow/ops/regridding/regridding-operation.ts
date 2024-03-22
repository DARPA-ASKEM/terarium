import { Operation, WorkflowOperationTypes } from '@/types/workflow';

export interface RegriddingState {
	datasetId: string | null;
	notebookSessionId?: string;
}

export const RegriddingOperation: Operation = {
	name: WorkflowOperationTypes.REGRIDDING,
	description: 'Select a dataset',
	displayName: 'Transform gridded dataset',
	isRunnable: true,
	inputs: [{ type: 'datasetId', label: 'Dataset' }],
	outputs: [],
	action: () => {},

	initState: () => {
		const init: RegriddingState = {
			datasetId: null
		};
		return init;
	}
};
