import { Workflow } from '@/types/workflow';

export abstract class BaseScenario {
	public static templateId: string;

	public static templateName: string;

	workflowName: string;

	constructor() {
		this.workflowName = '';
	}

	abstract createWorkflow(): Workflow;
}
