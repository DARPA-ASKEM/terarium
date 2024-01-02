import { DocumentExtraction } from '@/types/Types';
import { AssetBlock, Operation, WorkflowOperationTypes } from '@/types/workflow';

export interface EquationFromImageBlock extends DocumentExtraction {
	text: string;
}
export interface ModelFromDocumentState {
	equations: AssetBlock<EquationFromImageBlock>[];
	text: string;
}

export const ModelFromDocumentOperation: Operation = {
	name: WorkflowOperationTypes.MODEL_FROM_DOCUMENT,
	description: 'Create model from document',
	displayName: 'Create model from document',
	isRunnable: true,
	inputs: [{ type: 'documentId', label: 'Document' }],
	outputs: [],
	action: () => {},

	initState: () => {
		const init: ModelFromDocumentState = {
			equations: [],
			text: ''
		};
		return init;
	}
};
