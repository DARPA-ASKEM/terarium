import { WorkflowOperationTypes } from '@/types/workflow';
import type { Operation, BaseState } from '@/types/workflow';

export interface CodeAssetState extends BaseState {
	codeAssetId: string | null;
}

export const CodeAssetOperation: Operation = {
	name: WorkflowOperationTypes.CODE,
	description: 'Select a code asset',
	displayName: 'Code',
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
