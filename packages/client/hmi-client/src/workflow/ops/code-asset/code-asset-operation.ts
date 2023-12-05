import { Operation, WorkflowOperationTypes } from '@/types/workflow';

export interface CodeAssetState {
	codeAssetId: string | null;
}

export const CodeAssetOperation: Operation = {
	name: WorkflowOperationTypes.CODE,
	description: 'Select a code asset',
	displayName: 'Code Asset',
	isRunnable: true,
	inputs: [],
	outputs: [{ type: 'codeAssetId' }],
	action: () => {},

	initState: () => {
		const init: CodeAssetState = {
			codeAssetId: null
		};
		return init;
	}
};
