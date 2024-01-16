import { ProgrammingLanguage } from '@/types/Types';
import { AssetBlock, Operation, WorkflowOperationTypes } from '@/types/workflow';
import { CodeBlock } from '@/utils/code-asset';

export interface ModelFromCodeState {
	codeLanguage: ProgrammingLanguage;
	codeBlocks: AssetBlock<CodeBlock>[];
	modelFramework: string;
	modelId: string;
}

export const ModelFromCodeOperation: Operation = {
	name: WorkflowOperationTypes.MODEL_FROM_CODE,
	description: 'Create model',
	displayName: 'Create model from code',
	isRunnable: true,
	inputs: [{ type: 'codeAssetId', label: 'Code' }],
	outputs: [],
	action: () => {},

	initState: () => {
		const init: ModelFromCodeState = {
			codeLanguage: ProgrammingLanguage.Python,
			codeBlocks: [],
			modelFramework: 'Petrinet',
			modelId: ''
		};
		return init;
	}
};
