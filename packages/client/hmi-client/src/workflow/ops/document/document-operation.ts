import type { DocumentExtraction } from '@/types/Types';
import { Operation, AssetBlock, WorkflowOperationTypes } from '@/types/workflow';

export interface DocumentOperationState {
	documentId: string | null;
	equations: AssetBlock<DocumentExtraction>[];
	tables: AssetBlock<DocumentExtraction>[];
	figures: AssetBlock<DocumentExtraction>[];
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
			documentId: null,
			equations: [],
			tables: [],
			figures: []
		};
		return init;
	}
};
