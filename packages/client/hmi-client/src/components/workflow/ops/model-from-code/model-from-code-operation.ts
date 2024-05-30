import { ProgrammingLanguage } from '@/types/Types';
import { ModelServiceType } from '@/types/common';
import { AssetBlock, Operation, WorkflowOperationTypes } from '@/types/workflow';
import { CodeBlock } from '@/utils/code-asset';

const DOCUMENTATION_URL = 'https://github.com/ml4ai/ASKEM-TA1-DockerVM?tab=readme-ov-file#code2amr';

export interface ModelFromCodeState {
	codeLanguage: ProgrammingLanguage;
	codeBlocks: AssetBlock<CodeBlock>[];
	modelFramework: string;
	modelId: string;
	modelService: ModelServiceType;
}

export const ModelFromCodeOperation: Operation = {
	name: WorkflowOperationTypes.MODEL_FROM_CODE,
	description: 'Create model',
	displayName: 'Create model from code',
	documentationUrl: DOCUMENTATION_URL,
	isRunnable: true,
	inputs: [
		{ type: 'codeAssetId', label: 'Code' },
		{ type: 'documentId', label: 'Document' }
	],
	outputs: [{ type: 'modelId', label: 'Model' }],
	action: () => {},

	initState: () => {
		const init: ModelFromCodeState = {
			codeLanguage: ProgrammingLanguage.Python,
			codeBlocks: [],
			modelFramework: 'Petrinet',
			modelId: '',
			modelService: ModelServiceType.TA1
		};
		return init;
	}
};
