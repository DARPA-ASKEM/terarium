import { WorkflowOperationTypes } from '@/types/workflow';
import type { Operation, BaseState } from '@/types/workflow';
import code from '@assets/svg/operator-images/code.svg';

export interface CodeAssetState extends BaseState {
	codeAssetId: string | null;
}

export const CodeAssetOperation: Operation = {
	name: WorkflowOperationTypes.CODE,
	description: 'Select a code asset',
	imageUrl: code,
	displayName: 'Code',
	isRunnable: true,
	inputs: [],
	outputs: [{ type: 'codeAssetId', label: 'Code asset' }],
	action: () => {},

	initState: () => {
		const init: CodeAssetState = {
			codeAssetId: null
		};
		return init;
	}
};
