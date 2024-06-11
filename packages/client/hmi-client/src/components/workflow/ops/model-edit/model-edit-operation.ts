import { Operation, WorkflowOperationTypes, BaseState } from '@/types/workflow';
import { NotebookHistory, createNotebookFromCode } from '@/services/notebook';
import { getModel } from '@/services/model';

const DOCUMENTATION_URL =
	'https://github.com/DARPA-ASKEM/beaker-kernel/blob/main/docs/contexts_mira_model_edit.md';

export interface ModelEditOperationState extends BaseState {
	notebookHistory: NotebookHistory[];
	hasCodeRun: boolean;
}

export const ModelEditOperation: Operation = {
	name: WorkflowOperationTypes.MODEL_EDIT,
	displayName: 'Edit model',
	description: 'Edit a model',
	documentationUrl: DOCUMENTATION_URL,
	isRunnable: false,
	inputs: [
		{ type: 'modelId|modelConfigId', label: 'Model or Model configuration', acceptMultiple: false }
	],
	outputs: [{ type: 'modelId', label: 'Model' }],
	action: async () => ({}),
	initState: () => {
		const init: ModelEditOperationState = {
			notebookHistory: [],
			hasCodeRun: false
		};
		return init;
	},
	createNotebook: async (state: ModelEditOperationState, value?: any[] | null) => {
		const modelIdToLoad = value?.[0];
		const outputModel = await getModel(modelIdToLoad);
		const { code, llmQuery, llmThoughts } = state.notebookHistory?.[0] ?? {};
		const notebook = createNotebookFromCode(
			code ?? '',
			'python3',
			{ 'application/json': outputModel },
			llmQuery,
			llmThoughts
		);
		return notebook;
	}
};
