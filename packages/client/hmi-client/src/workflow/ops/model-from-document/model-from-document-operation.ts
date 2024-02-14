import type { DocumentExtraction } from '@/types/Types';
import { ModelServiceType } from '@/types/common';
import { AssetBlock, Operation, WorkflowOperationTypes } from '@/types/workflow';

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
	modelService: ModelServiceType;
}

export const ModelFromDocumentOperation: Operation = {
	name: WorkflowOperationTypes.MODEL_FROM_DOCUMENT,
	description: 'Create model from document',
	displayName: 'Create model from document',
	isRunnable: true,
	inputs: [
		{ type: 'equations', label: 'Equations' },
		{ type: 'documentId', label: 'Text' }
	],
	outputs: [],
	action: () => {},

	initState: () => {
		const init: ModelFromDocumentState = {
			equations: [],
			text: '',
			modelFramework: 'petrinet',
			modelId: null,
			modelService: ModelServiceType.TA1
		};
		return init;
	}
};
