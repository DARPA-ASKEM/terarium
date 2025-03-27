import { AssetBlock, Operation, WorkflowOperationTypes } from '@/types/workflow';
import createModelFromEquations from '@assets/svg/operator-images/create-model-from-equation.svg';

const DOCUMENTATION_URL = 'https://documentation.terarium.ai/modeling/create-model-from-equations/';

export interface EquationBlock {
	text: string;
	provenance?: {
		documentId: string;
		extractionAssetId: string;
	};
}

export interface ModelFromEquationsState {
	includedEquations: AssetBlock<EquationBlock>[];
	excludedEquations: AssetBlock<EquationBlock>[];
	text: string;
	modelFramework: string;
	modelId: string | null;
}

export const ModelFromEquationsOperation: Operation = {
	name: WorkflowOperationTypes.MODEL_FROM_EQUATIONS,
	description: 'Create model from equations',
	displayName: 'Create model from equations',
	documentationUrl: DOCUMENTATION_URL,
	imageUrl: createModelFromEquations,
	isRunnable: true,
	inputs: [{ type: 'documentId', label: 'Document' }],
	outputs: [{ type: 'modelId', label: 'Model' }],
	action: () => {},

	initState: () => {
		const init: ModelFromEquationsState = {
			includedEquations: [],
			excludedEquations: [],
			text: '',
			modelFramework: 'petrinet',
			modelId: null
		};
		return init;
	}
};
