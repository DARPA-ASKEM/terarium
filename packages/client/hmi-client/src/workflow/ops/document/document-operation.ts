import { Operation, WorkflowOperationTypes } from '@/types/workflow';

export interface DocumentOperationState {
	documentId: string | null;
}

export const DocumentOperation: Operation = {
	name: WorkflowOperationTypes.DOCUMENT,
	displayName: 'Document',
	description: 'Document',
	inputs: [],
	outputs: [{ type: 'documentId' }],
	isRunnable: true,
	action: () => {},
	initState: () => {
		const init: DocumentOperationState = {
			documentId: null
		};
		return init;
	}
};
