import { WorkflowNode } from '@/types/workflow';
import { defineStore } from 'pinia';
import { ProjectAssetTypes } from '@/types/Project';

// Probably will be an array later
export const useOpenedWorkflowNodeStore = defineStore('opened-workflow-node', {
	state: () => ({
		workflowNode: null as WorkflowNode | null,
		assetId: null as string | null,
		pageType: null as ProjectAssetTypes | null
	}),
	actions: {
		setWorkflowNode(newWorkflowNode: WorkflowNode) {
			this.workflowNode = newWorkflowNode;

			// Grabs the model id from the last model config output from the node
			let index: number = 0;
			if (newWorkflowNode?.outputs.length) {
				index = newWorkflowNode.outputs.length - 1; // Grab index of last output port
			}
			this.assetId = newWorkflowNode?.outputs[index].value.model.id.toString() ?? undefined;

			// Just testing models now change this according to the asset/process you want to see
			this.pageType = ProjectAssetTypes.MODELS;
		}
	}
});
