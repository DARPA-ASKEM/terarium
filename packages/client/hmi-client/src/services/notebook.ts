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

/**
 * Create a notebook from a code
 * @param code code to be added to the notebook
 * @param executeResult output of the code execution if any
 * @param language language of the code
 * @param llmQuery llm query used to generate the code if any
 * @param thought llm thought generated from the query if any
 * @returns
 */
export const createNotebookFromCode = (
	code: string,
	language: string,
	executionResult?: any,
	llmQuery?: string,
	llmThoughts: any[] = []
) => {
	// TODO: Consider using jataware/beaker-kernel library to generate notebook (https://github.com/jataware/beaker-kernel/blob/886b2b3913ca1460f0301a5cd97cbcf15de609bc/beaker-ts/src/notebook.ts#L414)
	const notebook = {
		nbformat: 4,
		nbformat_minor: 5,
		cells: [] as any[],
		metadata: {
			kernelspec: {
				display_name: 'Beaker Kernel',
				name: 'beaker',
				language: 'beaker'
			},
			language_info: {
				name: language,
				display_name: language
			}
		}
	};
	if (llmQuery) {
		const beakerQueryCell = {
			cell_type: 'query',
			events: llmThoughts.map((thought) => ({ type: 'thought', content: thought.content })),
			metadata: {},
			source: llmQuery,
			status: 'idle'
		};
		notebook.cells.push(beakerQueryCell);
	}
	const beakerCodeCell = {
		cell_type: 'code',
		execution_count: 1,
		metadata: {},
		outputs: [] as any[],
		source: code,
		status: 'idle'
	};
	if (executionResult) {
		// Make a shallow copy of the execution result
		const data = { ...executionResult };
		// Make sure the values of the data is stringified as the beaker summary endpoint seem to have issue with object json value
		Object.keys(data).forEach((type) => {
			if (typeof data[type] !== 'string') {
				data[type] = JSON.stringify(data[type]);
			}
		});
		beakerCodeCell.outputs.push({
			output_type: 'execute_result',
			data
		});
	}
	notebook.cells.push(beakerCodeCell);
	return notebook;
};
