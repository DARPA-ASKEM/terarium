import { cloneDeep } from 'lodash';
import { WorkflowNode } from '@/types/workflow';
import axios from 'axios';

export interface NotebookHistory {
	code: string;
	timestamp: number;
}

// A common pattern used to save code from a notebook within an operator
// This is ready to be ported to nodes such as tera-model-config and tera-stratify-mira
// Not ported yet since this will ruin the states of the nodes that already exist due to their differently named properties
export const saveCodeToState = (node: WorkflowNode<any>, code: string, hasCodeRun: boolean) => {
	const state = cloneDeep(node.state);

	if (!('notebookHistory' in state) || !('hasCodeRun' in state)) return state;

	state.hasCodeRun = hasCodeRun;
	// for now only save the last code executed, may want to save all code executed in the future
	const notebookHistoryLength = state.notebookHistory.length;
	const timestamp = Date.now();
	if (notebookHistoryLength > 0) {
		state.notebookHistory[0] = { code, timestamp };
	} else {
		state.notebookHistory.push({ code, timestamp });
	}
	return state;
};

/**
 * Create a notebook from a code
 * @param code code to be added to the notebook
 * @param language language of the code
 * @param llmQuery llm query used to generate the code if any
 * @param thought llm thought generated from the query if any
 * @returns
 */
export const createNotebookFromCode = async (
	code: string,
	language: string,
	llmQuery?: string,
	llmThought?: any
) => {
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
			events: llmThought ? [{ type: 'thought', content: llmThought?.content }] : [],
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
		outputs: [],
		source: code,
		status: 'idle'
	};
	notebook.cells.push(beakerCodeCell);

	const res = await axios.post('/beaker/summary', { notebook });
	console.log(res);
	console.log(JSON.stringify(notebook, null, 2));
	return notebook;
};
