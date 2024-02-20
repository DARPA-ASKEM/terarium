import { Operation, WorkflowOperationTypes } from '@/types/workflow';

export interface TextBlockState {
	content: string;
}

export const TextBlockOperation: Operation = {
	name: WorkflowOperationTypes.TEXT_BLOCK,
	description: 'Add a text block to the workflow.',
	displayName: 'Text block',
	isRunnable: false,
	inputs: [],
	outputs: []
};
