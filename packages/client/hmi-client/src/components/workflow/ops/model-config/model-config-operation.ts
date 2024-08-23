import { WorkflowOperationTypes } from '@/types/workflow';
import type { Operation, BaseState } from '@/types/workflow';
import type { ModelConfiguration } from '@/types/Types';
import { NotebookHistory } from '@/services/notebook';
import configureModel from '@assets/svg/operator-images/configure-model.svg';

export const name = 'ModelConfigOperation';

export interface ModelConfigOperationState extends BaseState {
	transientModelConfig: ModelConfiguration;
	notebookHistory: NotebookHistory[];
	hasCodeRun: boolean;
	datasetModelConfigTaskId: string;
	documentModelConfigTaskId: string;
}

export const blankModelConfig: ModelConfiguration = {
	id: '',
	modelId: '',
	name: '',
	description: '',
	simulationId: '',
	observableSemanticList: [],
	parameterSemanticList: [],
	initialSemanticList: [],
	inferredParameterList: []
};

export const ModelConfigOperation: Operation = {
	name: WorkflowOperationTypes.MODEL_CONFIG,
	displayName: 'Configure model',
	description: 'Create model configurations.',
	imageUrl: configureModel,
	isRunnable: true,
	inputs: [
		{ type: 'modelId', label: 'Model' },
		{ type: 'documentId', label: 'Document', isOptional: true },
		{ type: 'datasetId', label: 'Dataset', isOptional: true }
	],
	outputs: [{ type: 'modelConfigId', label: 'Model configuration' }],
	action: async () => ({}),
	initState: () => {
		const init: ModelConfigOperationState = {
			transientModelConfig: blankModelConfig,
			notebookHistory: [],
			hasCodeRun: false,
			datasetModelConfigTaskId: '',
			documentModelConfigTaskId: ''
		};
		return init;
	}
};
