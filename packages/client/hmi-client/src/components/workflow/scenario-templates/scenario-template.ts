import { Workflow } from '@/types/workflow';
import { useProjects } from '@/composables/project';
import { AssetType } from '@/types/Types';

export abstract class BaseScenarioTemplate {
	templateId: string;

	templateName: string;

	description: string;

	workflowName: string;

	constructor(nameId: string, name: string, description: string) {
		this.templateId = nameId;
		this.templateName = name;
		this.description = description;
		const workflows = useProjects().getActiveProjectAssets(AssetType.Workflow);
		this.workflowName = `workflow ${workflows.length + 1}`;
	}

	abstract createWorkflow(): Workflow;
}
