import { Operation, WorkflowOperationTypes } from '@/types/workflow';
import { v4 as uuidv4 } from 'uuid';

export interface StratifyGroup {
	id: string;
	borderColour: string;
	name: string;
	selectedVariables: string[];
	groupLabels: string;
}

export interface StratifyOperationStateMira {
	strataGroups: StratifyGroup[];
}

export const StratifyMiraOperation: Operation = {
	name: WorkflowOperationTypes.STRATIFY_MIRA,
	displayName: 'Stratify MIRA',
	description: 'Stratify a model',
	inputs: [{ type: 'modelConfigId', label: 'Model configuration', acceptMultiple: false }],
	outputs: [{ type: 'model' }],
	isRunnable: false,
	action: () => {},
	initState: () => {
		const init: StratifyOperationStateMira = {
			strataGroups: [
				{
					id: uuidv4(),
					borderColour: '#c300a6',
					name: '',
					selectedVariables: [],
					groupLabels: ''
				}
			]
		};
		return init;
	}
};
