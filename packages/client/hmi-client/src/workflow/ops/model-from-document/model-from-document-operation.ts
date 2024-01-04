import { DocumentExtraction } from '@/types/Types';
import { AssetBlock, Operation, WorkflowOperationTypes } from '@/types/workflow';

export interface EquationFromImageBlock extends DocumentExtraction {
	text: string;
	extractionError?: boolean;
}
export interface ModelFromDocumentState {
	equations: AssetBlock<EquationFromImageBlock>[];
	text: string;
	modelFramework: string;
	modelId: string | null;
}

export const ModelFromDocumentOperation: Operation = {
	name: WorkflowOperationTypes.MODEL_FROM_DOCUMENT,
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
