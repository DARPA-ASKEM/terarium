import type { DocumentExtraction } from '@/types/Types';
import { WorkflowOperationTypes } from '@/types/Types';
import { AssetBlock, Operation } from '@/types/workflow';

export interface EquationBlock {
	text: string;
	extractionError?: boolean;
}
export interface EquationFromImageBlock extends DocumentExtraction {
	text: string;
	extractionError?: boolean;
}

export function instanceOfEquationFromImageBlock(
	object: EquationBlock | EquationFromImageBlock
): object is EquationFromImageBlock {
	return 'fileName' in object;
}

export interface ModelFromDocumentState {
	equations: AssetBlock<EquationBlock | EquationFromImageBlock>[];
	text: string;
	modelFramework: string;
	modelId: string | null;
}

export const ModelFromDocumentOperation: Operation = {
	name: WorkflowOperationTypes.ModelFromDocument,
	description: 'Create model from document',
	displayName: 'Create model from document',
	isRunnable: true,
	inputs: [{ type: 'equations', label: 'Equations' }],
	outputs: [],
	action: () => {},

	initState: () => {
		const init: ModelFromDocumentState = {
			equations: [],
			text: '',
			modelFramework: 'petrinet',
			modelId: null
		};
		return init;
	}
};
