import { ProgrammingLanguage } from '@/types/Types';
import { ProgrammingLanguageVersion } from '@/types/common';
import { Operation, WorkflowOperationTypes, BaseState } from '@/types/workflow';

export interface DatasetTransformerState extends BaseState {
	datasetId: string | null;
	notebookSessionId?: string;
	programmingLanguage: string;
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
			datasetId: null,
			programmingLanguage: ProgrammingLanguageVersion[ProgrammingLanguage.Python]
		};
		return init;
	}
};
