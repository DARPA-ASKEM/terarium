import { Workflow } from '@/types/workflow';

export abstract class BaseScenarioTemplate {
	templateId: string;

	templateName: string;

	description: string;

	workflowName: string;

	constructor(nameId: string, name: string, description: string) {
		this.templateId = nameId;
		this.templateName = name;
		this.description = description;
		this.workflowName = '';
	}

	abstract createWorkflow(): Workflow;
}
