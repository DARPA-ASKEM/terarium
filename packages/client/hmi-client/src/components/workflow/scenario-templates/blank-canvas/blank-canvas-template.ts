import { BaseScenario } from '@/components/workflow/scenario-templates/scenario-template';
import * as workflowService from '@/services/workflow';

export class BlankCanvasScenario extends BaseScenario {
	public static templateId = 'blank-canvas';

	public static templateName = 'Blank Canvas';

	constructor() {
		super();
		this.workflowName = 'Blank Canvas';
	}

	createWorkflow() {
		const wf = new workflowService.WorkflowWrapper();
		const workflow = wf.dump();
		workflow.name = this.workflowName;
		return workflow;
	}
}
