import { Workflow } from '@/types/workflow';

export interface ScenarioHeader {
	title: string;
	question?: string;
	description?: string;
	examples?: string[];
}
export abstract class BaseScenario {
	public static templateId: string = '';

	public static templateName: string = '';

	workflowName: string;

	constructor() {
		this.workflowName = '';
	}

	abstract createWorkflow(): Promise<Workflow>;

	isValid(): boolean {
		return !!this.workflowName;
	}

	setWorkflowName(name: string) {
		this.workflowName = name;
	}
}
