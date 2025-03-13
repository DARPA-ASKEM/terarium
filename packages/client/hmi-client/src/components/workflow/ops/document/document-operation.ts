import type { Operation, BaseState } from '@/types/workflow';
import { WorkflowOperationTypes } from '@/types/workflow';
import document from '@assets/svg/operator-images/document.svg';

export interface DocumentOperationState extends BaseState {
	documentId: string | null;
	taskProgress?: number;
}

export const DocumentOperation: Operation = {
	name: WorkflowOperationTypes.DOCUMENT,
	displayName: 'Document',
	description: 'Document',
	imageUrl: document,
	inputs: [],
	outputs: [{ type: 'documentId', label: 'Document' }],
	isRunnable: true,
	action: () => {},
	initState: () => {
		const init: DocumentOperationState = {
			documentId: null
		};
		return init;
	}
};
