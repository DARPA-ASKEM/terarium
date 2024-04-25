import { Operation, WorkflowOperationTypes, BaseState } from '@/types/workflow';

const DOCUMENTATION_URL =
	'https://github.com/DARPA-ASKEM/beaker-kernel/blob/main/docs/contexts_climate_data_utility.md';

export interface RegriddingOperationState extends BaseState {
	datasetId: string | null;
	notebookSessionId?: string;
}

export const RegriddingOperation: Operation = {
	name: WorkflowOperationTypes.REGRIDDING,
	description: 'Select a dataset',
	displayName: 'Transform gridded dataset',
	documentationUrl: DOCUMENTATION_URL,
	isRunnable: true,
	inputs: [{ type: 'datasetId', label: 'Dataset' }],
	outputs: [],
	action: () => {},

	initState: () => {
		const init: RegriddingOperationState = {
			datasetId: null
		};
		return init;
	}
};
