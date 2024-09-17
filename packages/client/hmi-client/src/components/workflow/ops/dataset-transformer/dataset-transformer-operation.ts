import { ProgrammingLanguage } from '@/types/Types';
import { ProgrammingLanguageVersion } from '@/types/common';
import { Operation, WorkflowOperationTypes, BaseState } from '@/types/workflow';
import transformDataset from '@assets/svg/operator-images/transform-dataset.svg';

const DOCUMENTATION_URL = 'https://pandas.pydata.org/docs/user_guide/index.html#user-guide';

export interface DatasetTransformerState extends BaseState {
	datasetId: string | null;
	notebookSessionId?: string;
	programmingLanguage: string;
}

export const DatasetTransformerOperation: Operation = {
	name: WorkflowOperationTypes.DATASET_TRANSFORMER,
	description: 'Select a dataset',
	documentationUrl: DOCUMENTATION_URL,
	imageUrl: transformDataset,
	displayName: 'Transform dataset',
	isRunnable: true,
	inputs: [{ type: 'datasetId|simulationId', label: 'Dataset or Simulation' }],
	outputs: [{ type: 'datasetId', label: 'Dataset' }],
	action: () => {},

	initState: () => {
		const init: DatasetTransformerState = {
			datasetId: null,
			programmingLanguage: ProgrammingLanguageVersion[ProgrammingLanguage.Python]
		};
		return init;
	}
};
