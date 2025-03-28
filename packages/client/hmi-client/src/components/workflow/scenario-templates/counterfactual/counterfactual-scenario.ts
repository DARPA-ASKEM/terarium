import { BaseScenario } from '@/components/workflow/scenario-templates/base-scenario';
import * as workflowService from '@/services/workflow';

export class CounterfactualScenario extends BaseScenario {
	public static templateId = 'counterfactual';

	public static templateName = 'Necessary or sufficient analysis';

	private modelId: string;

	private modelConfigId: string;

	private interventionPolicyId: string;

	private datasetId: string;

	constructor() {
		super();
		this.workflowName = '';
		this.modelId = '';
		this.modelConfigId = '';
		this.interventionPolicyId = '';
		this.datasetId = '';
	}

	getModelId() {
		return this.modelId;
	}

	setModelId(modelId: string) {
		this.modelId = modelId;
	}

	toJSON() {
		return {
			templateId: CounterfactualScenario.templateId,
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
