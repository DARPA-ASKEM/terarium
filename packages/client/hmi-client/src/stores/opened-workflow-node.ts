import { defineStore } from 'pinia';
import { ProjectAssetTypes } from '@/types/Project';

interface StringValueMap {
	[key: string]: number;
}

// Probably will be an array later
export const useOpenedWorkflowNodeStore = defineStore('opened-workflow-node', {
	state: () => ({
		assetId: null as string | null,
		pageType: null as ProjectAssetTypes | null,
		// model node
		initialValues: null as StringValueMap[] | null,
		parameterValues: null as StringValueMap[] | null
	}),
	actions: {
		set(assetId: string | null, pageType: ProjectAssetTypes | null) {
			this.assetId = assetId;
			this.pageType = pageType;
		},
		setModelConfig(initialValues: StringValueMap[], parameterValues: StringValueMap[]) {
			this.initialValues = initialValues;
			this.parameterValues = parameterValues;
		}
	}
});
