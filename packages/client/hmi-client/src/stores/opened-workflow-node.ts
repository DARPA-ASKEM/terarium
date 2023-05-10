import { ProjectAssetTypes } from '@/types/Project';
import { WorkflowNode } from '@/types/workflow';
import { defineStore } from 'pinia';

// Probably will be an array later
export const useOpenedWorkflowNodeStore = defineStore('opened-workflow-node', {
	state: () => ({
		workflowNode: null as WorkflowNode | null,
		asset: null as {
			id: string, type: ProjectAssetTypes
		} | null
	}),
	actions: {
		setWorkflowNode(newWorkflowNode: WorkflowNode) {
			this.workflowNode = newWorkflowNode;
		}
	}
});
