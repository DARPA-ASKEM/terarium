import { BaseScenarioTemplate } from '@/components/workflow/scenario-templates/scenario-template';
import * as workflowService from '@/services/workflow';

export class BlankCanvasScenarioTemplate extends BaseScenarioTemplate {
	constructor() {
		super('blank-canvas', 'Blank Canvas', 'Blank canvas for creating a new workflow');
	}

	createWorkflow() {
		const wf = new workflowService.WorkflowWrapper();

		const workflow = wf.dump();
		workflow.name = this.workflowName;
		return workflow;
	}
}

export const BlankCanvasTemplate = new BlankCanvasScenarioTemplate();
