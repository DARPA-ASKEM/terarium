import { Workflow } from '@/types/workflow';

export abstract class BaseScenario {
	public static templateId: string = '';

	public static templateName: string = '';

	public static header = {
		title: '',
		description: ''
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
