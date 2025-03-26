import { BaseScenario } from '@/components/workflow/scenario-templates/base-scenario';
import * as workflowService from '@/services/workflow';

export class PolicyDesignScenario extends BaseScenario {
	public static templateId = 'policy-design';

	public static templateName = 'Policy Design';

	private modelConfigId;

	private interventionId;

	constructor(modelConfigId, interventionId) {
		super();
		this.workflowName = PolicyDesignScenario.templateName;
		this.modelConfigId = modelConfigId;
		this.interventionId = interventionId;
	}

	toJSON() {
		return {
			templateId: PolicyDesignScenario.templateId,
			workflowName: this.workflowName,
			modelConfigId: this.modelConfigId,
			interventionId: this.interventionId
		};
	}

	async createWorkflow() {
		const wf = new workflowService.WorkflowWrapper();
		wf.setWorkflowName(this.workflowName);
		wf.setWorkflowScenario(this.toJSON()); // TOM: How does this work?
		return wf.dump();
	}
}
