import type { DocumentExtraction } from '@/types/Types';
import { Operation, AssetBlock, WorkflowOperationTypes } from '@/types/workflow';

export enum DocumentOperationPortType {
	EQUATION = 'equations',
	TABLE = 'tables',
	FIGURE = 'figures'
}
export interface DocumentOperationState {
	documentId: string | null;
	[DocumentOperationPortType.EQUATION]: AssetBlock<DocumentExtraction>[];
	[DocumentOperationPortType.TABLE]: AssetBlock<DocumentExtraction>[];
	[DocumentOperationPortType.FIGURE]: AssetBlock<DocumentExtraction>[];
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
