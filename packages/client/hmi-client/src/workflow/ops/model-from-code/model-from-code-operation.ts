import { ProgrammingLanguage, WorkflowOperationTypes } from '@/types/Types';
import { AssetBlock, Operation } from '@/types/workflow';
import { CodeBlock } from '@/utils/code-asset';

export interface ModelFromCodeState {
	codeLanguage: ProgrammingLanguage;
	codeBlocks: AssetBlock<CodeBlock>[];
	modelFramework: string;
	modelId: string;
}

export const ModelFromCodeOperation: Operation = {
	name: WorkflowOperationTypes.ModelFromCode,
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
