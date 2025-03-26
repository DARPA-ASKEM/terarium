import { BaseScenario } from '@/components/workflow/scenario-templates/base-scenario';
import * as workflowService from '@/services/workflow';

export class PolicyDesignScenario extends BaseScenario {
	public static templateId = 'policy-design';

	public static templateName = 'Policy Design';

	private modelConfigId;

	private interventionPolicyId;

	private datasetId;

	constructor() {
		super();
		this.workflowName = PolicyDesignScenario.templateName;
	}

	toJSON() {
		return {
			templateId: PolicyDesignScenario.templateId,
			workflowName: this.workflowName,
			modelConfigId: this.modelConfigId,
			interventionPolicyId: this.interventionPolicyId,
			datasetId: this.datasetId
		};
	}

	async createWorkflow() {
		const wf = new workflowService.WorkflowWrapper();
		wf.setWorkflowName(this.workflowName);
		wf.setWorkflowScenario(this.toJSON()); // TOM: How does this work?
		return wf.dump();
	}

	getModelConfigId() {
		return this.modelConfigId;
	}

	setModelConfigId(modelConfigId: string) {
		this.modelConfigId = modelConfigId;
	}

	getInterventionPolicyId() {
		return this.interventionPolicyId;
	}

	setInterventionPolicyId(interventionPolicyId: string) {
		this.interventionPolicyId = interventionPolicyId;
	}

	getDatasetId() {
		return this.datasetId;
	}

	setDatasetId(datasetId: string) {
		this.datasetId = datasetId;
	}
}
