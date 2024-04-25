import type { DocumentExtraction } from '@/types/Types';
import { ModelServiceType } from '@/types/common';
import { AssetBlock, Operation, WorkflowOperationTypes } from '@/types/workflow';

const DOCUMENTATION_URL =
	'https://github.com/DARPA-ASKEM/model-service/blob/07ae21cae2d5465f9ac5b5bbbe6c7b28b7259f04/src/ModelService.jl#L54';

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

export interface ModelFromEquationsState {
	equations: AssetBlock<EquationBlock | EquationFromImageBlock>[];
	text: string;
	modelFramework: string;
	modelId: string | null;
	modelService: ModelServiceType;
}

export const ModelFromEquationsOperation: Operation = {
	name: WorkflowOperationTypes.MODEL_FROM_EQUATIONS,
	description: 'Create model from equations',
	displayName: 'Create model from equations',
	documentationUrl: DOCUMENTATION_URL,
	isRunnable: true,
	inputs: [{ type: 'documentId', label: 'Document' }],
	outputs: [],
	action: () => {},

	initState: () => {
		const init: ModelFromEquationsState = {
			equations: [],
			text: '',
			modelFramework: 'petrinet',
			modelId: null,
			modelService: ModelServiceType.TA1
		};
		return init;
	}
};
