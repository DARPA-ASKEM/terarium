import { cloneDeep } from 'lodash';
import { WorkflowNode } from '@/types/workflow';

export interface NotebookHistory {
	code: string;
	llmQuery: string;
	llmThoughts: any[];
	timestamp: number;
}

// A common pattern used to save code from a notebook within an operator
// One more operator to port this to: tera-stratify-mira
// Not ported yet since this will ruin the states of the nodes that already exist due to their differently named properties
export const saveCodeToState = (
	node: WorkflowNode<any>,
	code: string,
	hasCodeRun: boolean,
	llmQuery: string = '',
	llmThoughts: any[] = []
) => {
	const state = cloneDeep(node.state);

	if (!('notebookHistory' in state) || !('hasCodeRun' in state)) return state;

	state.hasCodeRun = hasCodeRun;
	// for now only save the last code executed, may want to save all code executed in the future
	const notebookHistoryLength = state.notebookHistory.length;
	const timestamp = Date.now();
	if (notebookHistoryLength > 0) {
		state.notebookHistory[0] = { code, timestamp, llmQuery, llmThoughts };
	} else {
		state.notebookHistory.push({ code, timestamp, llmQuery, llmThoughts });
	}
	return state;
};
