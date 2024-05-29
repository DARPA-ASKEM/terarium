import { getModel } from '@/services/model';
import { createNotebookFromCode } from '@/services/notebook';
import type { Operation, BaseState } from '@/types/workflow';
import { WorkflowOperationTypes } from '@/types/workflow';

const DOCUMENTATION_URL =
	'https://github.com/gyorilab/mira/blob/main/notebooks/viz_strat_petri.ipynb';

export interface StratifyGroup {
	borderColour: string;
	name: string;
	selectedVariables: string[];
	groupLabels: string;
	cartesianProduct: boolean;

	directed: boolean;
	structure: null | any[];

	useStructure: boolean;
}

export interface StratifyCode {
	llmQuery: string;
	llmThoughts: any[];
	code: string;
	timestamp: number;
}

export interface StratifyOperationStateMira extends BaseState {
	strataGroup: StratifyGroup;
	strataCodeHistory: StratifyCode[];
	hasCodeBeenRun: boolean;
}

export const blankStratifyGroup: StratifyGroup = {
	borderColour: '#c300a6',
	name: '',
	selectedVariables: [],
	groupLabels: '',

	// Allow existing transitions to involve multiple strata
	cartesianProduct: true,

	// Create new transitions between strata, this act as a proxy to MIRA "structure", which is
	//   null = everything
	//   [] = nothing
	//   [[a, b], [c, d]] = somewhere in between
	//
	// Here we use a simpler proxy useStructure, where
	//   true => structure = null
	//   false => structuer = []
	structure: null,
	useStructure: true,

	// Always true for now - Feb 2024
	directed: true
};

export const StratifyMiraOperation: Operation = {
	name: WorkflowOperationTypes.STRATIFY_MIRA,
	displayName: 'Stratify model',
	description: 'Stratify a model',
	documentationUrl: DOCUMENTATION_URL,
	inputs: [
		{ type: 'modelId|modelConfigId', label: 'Model or Model configuration', acceptMultiple: false }
	],
	outputs: [{ type: 'modelId', label: 'Model' }],
	isRunnable: false,
	action: () => {},
	initState: () => {
		const init: StratifyOperationStateMira = {
			strataGroup: blankStratifyGroup,
			strataCodeHistory: [],
			hasCodeBeenRun: false
		};
		return init;
	},
	createNotebook: async (state: StratifyOperationStateMira, value?: any[] | null) => {
		const modelIdToLoad = value?.[0];
		const outputModel = await getModel(modelIdToLoad);
		const code = state.strataCodeHistory?.[0].code ?? '';
		// TODO: Add llm query and thought to the notebook
		const notebook = createNotebookFromCode(code, 'python3', { 'application/json': outputModel });
		return notebook;
	}
};
