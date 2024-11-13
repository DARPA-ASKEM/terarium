import { Workflow } from '@/types/workflow';
import { useProjects } from '@/composables/project';
import { AssetType } from '@/types/Types';

export abstract class BaseScenarioTemplate {
	public static templateId: string;

	public static templateName: string;

	workflowName: string;

	constructor() {
		const workflows = useProjects().getActiveProjectAssets(AssetType.Workflow);
		this.workflowName = `workflow ${workflows.length + 1}`;
	}

	abstract createWorkflow(): Workflow;
}
