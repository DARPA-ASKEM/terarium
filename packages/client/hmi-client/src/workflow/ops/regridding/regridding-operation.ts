import { Operation, WorkflowOperationTypes } from '@/types/workflow';

export interface RegridddingState {
	datasetId: string | null;
	notebookSessionId?: string;
}

export const RegriddingOperation: Operation = {
	name: WorkflowOperationTypes.REGRIDDING,
	description: 'Select a dataset',
	displayName: 'Transform dataset',
	isRunnable: true,
	inputs: [{ type: 'datasetId|simulationId', label: 'Dataset or Simulation' }],
	outputs: [],
	action: () => {},

	initState: () => {
		const init: RegridddingState = {
			datasetId: null
		};
		return init;
	}
};
