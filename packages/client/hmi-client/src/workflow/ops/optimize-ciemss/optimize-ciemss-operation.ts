import { Operation, WorkflowOperationTypes } from '@/types/workflow';

export interface OptimizeCiemssOperationState {}

export const SimulateCiemssOperation: Operation = {
	name: WorkflowOperationTypes.OPTIMIZE_CIEMSS,
	displayName: 'Optimization',
	description: 'Optimize model under scenario',
	inputs: [{ type: 'modelConfigId', label: 'Model configuration', acceptMultiple: false }],
	outputs: [],
	isRunnable: true,

	initState: () => {
		const init: OptimizeCiemssOperationState = {};
		return init;
	},

	// TODO: Figure out mapping
	// Calls API, returns results.
	action: async (): Promise<void> => {
		console.log('test');
	}
};
