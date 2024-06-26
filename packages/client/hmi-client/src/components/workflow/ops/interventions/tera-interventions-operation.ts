import { WorkflowOperationTypes } from '@/types/workflow';
import type { Operation, BaseState } from '@/types/workflow';

export interface DummyIntervention {
	id: string;
	name: string;
	description: string;
	modelId: string;
	parameterId: string;
	staticValues: DummyInterventionSetting[];
	dynamicValues: DummyInterventionSetting[];
	createdOn: Date;
}

export interface DummyInterventionSetting {
	parameterId?: string;
	threshold: number;
	timestep: number;
}

export interface InterventionsState extends BaseState {
	interventionsId: string | null;
}

export const InterventionsOperation: Operation = {
	name: WorkflowOperationTypes.INTERVENTIONS,
	description: 'Create interventions',
	displayName: 'Create intervention policy',
	isRunnable: true,
	inputs: [{ type: 'modelId', label: 'Model' }],
	outputs: [],
	action: () => {},

	initState: () => {
		const init: InterventionsState = {
			interventionsId: null
		};
		return init;
	}
};
