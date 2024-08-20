import { Operation, WorkflowOperationTypes, BaseState } from '@/types/workflow';
import { NotebookHistory } from '@/services/notebook';

const DOCUMENTATION_URL =
	'https://githubicom/gyorilab/mira/blob/7314765ab409ddc9647269ad2381055f1cd67706/notebooks/hackathon_2023.10/dkg_grounding_model_comparison.ipynb#L307';

export interface ModelComparisonOperationState extends BaseState {
	overview: string;
	notebookHistory: NotebookHistory[];
	hasCodeRun: boolean;
	comparisonImageIds: string[];
}

export const ModelComparisonOperation: Operation = {
	name: WorkflowOperationTypes.MODEL_COMPARISON,
	displayName: 'Compare models',
	description: '',
	documentationUrl: DOCUMENTATION_URL,
	inputs: [
		{ type: 'modelId', label: 'Model' },
		{ type: 'modelId', label: 'Model' }
	],
	outputs: [],
	isRunnable: true,
	action: () => {},
	initState: () => {
		const init: ModelComparisonOperationState = {
			overview: '',
			notebookHistory: [],
			hasCodeRun: false,
			comparisonImageIds: []
		};
		return init;
	}
};
