import { defineStore } from 'pinia';
import { ProjectAssetTypes } from '@/types/Project';
import { WorkflowNode } from '@/types/workflow';

interface StringValueMap {
	[key: string]: number;
}

// Probably will be an array later
export const useOpenedWorkflowNodeStore = defineStore('opened-workflow-node', {
	state: () => ({
		assetId: null as string | null,
		pageType: null as ProjectAssetTypes | null,
		selectedOutputIndex: 0 as number,
		// model node
		initialValues: null as StringValueMap[] | null,
		parameterValues: null as StringValueMap[] | null,
		node: null as WorkflowNode | null,
		// simulate node
		numCharts: 1,
		chartConfigs: [] as any[]
	}),
	actions: {
		// model node
		setDrilldown(
			assetId: string | null,
			pageType: ProjectAssetTypes | null,
			node: WorkflowNode | null
		) {
			this.assetId = assetId;
			this.pageType = pageType;
			this.node = node;
		},
		setModelConfig(initialValues: StringValueMap[], parameterValues: StringValueMap[]) {
			this.initialValues = initialValues;
			this.parameterValues = parameterValues;
		},
		// simulate node
		appendChart() {
			this.numCharts++;
		},
		setChartConfig(chartIdx: number, chartConfig) {
			this.chartConfigs[chartIdx] = {
				...this.chartConfigs[chartIdx],
				...chartConfig
			};
		}
	}
});
