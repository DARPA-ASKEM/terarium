import { defineStore } from 'pinia';
import { ProjectAssetTypes } from '@/types/Project';

// Probably will be an array later
export const useOpenedWorkflowNodeStore = defineStore('opened-workflow-node', {
	state: () => ({
		assetId: null as string | null,
		pageType: null as ProjectAssetTypes | null
	}),
	actions: {
		set(assetId: string | null, pageType: ProjectAssetTypes | null) {
			this.assetId = assetId;
			this.pageType = pageType;
		}
	}
});
