import { Workflow } from '@/types/workflow';

interface ScenarioHeader {
	title: string;
	question?: string;
	description?: string;
	examples?: string[];
}
export abstract class BaseScenario {
	public static templateId: string = '';

	public static templateName: string = '';

	public static header: ScenarioHeader = {
		title: ''
	};

	public static examples: string[] = [];

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
