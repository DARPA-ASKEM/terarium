import { Operation, WorkflowOperationTypes } from '@/types/workflow';
import type { Initial, ModelParameter } from '@/types/Types';

export const name = 'ModelConfigOperation';

export interface ModelEditCode {
	code: string;
	timestamp: number;
}

export interface ModelConfigOperationState {
	name: string;
	description: string;
	initials: Initial[];
	parameters: ModelParameter[];
	timeseries: { [key: string]: string };
	sources: { [key: string]: string };
	units: { [key: string]: string };
	modelEditCodeHistory: ModelEditCode[];
	hasCodeBeenRun: boolean;
	tempConfigId: string; // This is used for beaker context when there is no output selected. It is a config id that is in TDS and marked as temp
}

export const ModelConfigOperation: Operation = {
	name: WorkflowOperationTypes.MODEL_CONFIG,
	displayName: 'Configure model',
	description: 'Create model configurations.',
	isRunnable: true,
	inputs: [
		{ type: 'modelId' },
		{ type: 'documentId', label: 'Document', isOptional: true },
		{ type: 'datasetId', label: 'Dataset', isOptional: true }
	],
	outputs: [{ type: 'modelConfigId' }],
	action: async () => ({}),

	initState: () => {
		const init: ModelConfigOperationState = {
			name: '',
			description: '',
			modelEditCodeHistory: [],
			hasCodeBeenRun: false,
			tempConfigId: '',
			initials: [],
			parameters: [],
			timeseries: {},
			sources: {},
			units: {}
		};
		return init;
	}
};
