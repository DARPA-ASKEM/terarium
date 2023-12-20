import { DocumentExtraction } from '@/types/Types';
import { Operation, SelectableAsset, WorkflowOperationTypes } from '@/types/workflow';

export interface DocumentOperationState {
	documentId: string | null;
	equations?: SelectableAsset<DocumentExtraction>[];
	tables?: SelectableAsset<DocumentExtraction>[];
	figures?: SelectableAsset<DocumentExtraction>[];
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
