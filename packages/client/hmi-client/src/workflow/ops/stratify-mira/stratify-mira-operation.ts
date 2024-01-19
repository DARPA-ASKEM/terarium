import { Operation, WorkflowOperationTypes } from '@/types/Types';

export interface StratifyGroup {
	borderColour: string;
	name: string;
	selectedVariables: string[];
	groupLabels: string;
	cartesianProduct: boolean;
}

export interface StratifyCode {
	code: string;
	timestamp: number;
}

export interface StratifyOperationStateMira {
	strataGroup: StratifyGroup;
	strataCodeHistory: StratifyCode[];
	hasCodeBeenRun: boolean;
}

export const blankStratifyGroup: StratifyGroup = {
	borderColour: '#c300a6',
	name: '',
	selectedVariables: [],
	groupLabels: '',
	cartesianProduct: true
};

export const StratifyMiraOperation: Operation = {
	name: WorkflowOperationTypes.STRATIFY_MIRA,
	displayName: 'Stratify MIRA',
	description: 'Stratify a model',
	inputs: [{ type: 'modelId', label: 'Model', acceptMultiple: false }],
	outputs: [{ type: 'model' }],
	isRunnable: false,
	action: () => {},
	initState: () => {
		const init: StratifyOperationStateMira = {
			strataGroup: blankStratifyGroup,
			strataCodeHistory: [],
			hasCodeBeenRun: false
		};
		return init;
	}
};
