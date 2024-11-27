import { BaseScenario } from '@/components/workflow/scenario-templates/base-scenario';
import * as workflowService from '@/services/workflow';

export class DecisionMakingScenario extends BaseScenario {
	public static templateId = 'decision-making';

	public static templateName = 'Decision Making';

	modelSpec: { id: string };

	modelConfigSpec: { id: string };

	interventionSpecs: { ids: string[] };

	simulateSpec: { ids: string[] };

	constructor() {
		super();
		this.modelSpec = {
			id: ''
		};
		this.interventionSpecs = {
			ids: []
		};
		this.modelConfigSpec = {
			id: ''
		};
		this.simulateSpec = {
			ids: []
		};
		this.workflowName = '';
	}

	setModelSpec(id: string) {
		this.modelSpec.id = id;
		this.modelConfigSpec.id = '';
		this.interventionSpecs.ids = [];
		this.simulateSpec.ids = [];
	}

	setModelConfigSpec(id: string) {
		this.modelConfigSpec.id = id;
	}

	setInterventionSpecs(ids: string[]) {
		this.interventionSpecs.ids = ids;
	}

	setSimulateSpec(ids: string[]) {
		this.simulateSpec.ids = ids;
	}

	toJSON() {
		return {
			templateId: DecisionMakingScenario.templateId,
			workflowName: this.workflowName
		};
	}

	async createWorkflow() {
		const wf = new workflowService.WorkflowWrapper();
		wf.setWorkflowName(this.workflowName);
		wf.setWorkflowScenario(this.toJSON());
		return wf.dump();
	}
}
