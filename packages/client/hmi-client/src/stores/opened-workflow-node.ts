import { WorkflowNode } from '@/types/workflow';
import { defineStore } from 'pinia';

// Probably will be an array later
export const useOpenedWorkflowNodeStore = defineStore('opened-workflow-node', {
	state: () => ({
		workflowNode: null as WorkflowNode | null
	}),
	actions: {
		setWorkflowNode(newWorkflowNode: WorkflowNode) {
			this.workflowNode = newWorkflowNode;
		}
	}
});
