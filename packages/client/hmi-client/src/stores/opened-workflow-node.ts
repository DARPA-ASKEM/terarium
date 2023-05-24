import { defineStore } from 'pinia';
import { ProjectAssetTypes } from '@/types/Project';
import { WorkflowNode } from '@/types/workflow';

// Probably will be an array later
export const useOpenedWorkflowNodeStore = defineStore('opened-workflow-node', {
	state: () => ({
		assetId: null as string | null,
		pageType: null as ProjectAssetTypes | null,
		node: null as WorkflowNode | null
	}),
	actions: {
		set(assetId: string | null, pageType: ProjectAssetTypes | null, node: WorkflowNode | null) {
			this.assetId = assetId;
			this.pageType = pageType;
			this.node = node;
		}
	}
});
