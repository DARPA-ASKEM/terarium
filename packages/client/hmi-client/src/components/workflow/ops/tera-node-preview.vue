<template>
	<tera-progress-spinner v-if="isLoading" :font-size="2" is-centered style="height: 100%">
		<div v-if="processing">{{ processing }}</div>
		<div v-else>Processing...</div>
	</tera-progress-spinner>
	<div
		v-else-if="visibleChartSettings && _.isArray(visibleChartSettings[0])"
		v-for="(settingsArray, index) of visibleChartSettings"
		:key="index"
	>
		<vega-chart
			v-for="setting of settingsArray"
			:key="chartSettingKey || setting"
			:expandable="expandable"
			:are-embed-actions-visible="areEmbedActionsVisible"
			:visualization-spec="getChartSpec(preparedCharts[index][chartSettingKey || setting['id']])"
			:interactive="false"
		/>
	</div>
	<vega-chart
		v-else-if="visibleChartSettings?.length"
		v-for="(setting, index) of visibleChartSettings"
		:key="'c' + index"
		:expandable="expandable"
		:are-embed-actions-visible="areEmbedActionsVisible"
		:visualization-spec="getChartSpec(preparedCharts[chartSettingKey || setting['id']])"
		:interactive="false"
	/>
	<vega-chart
		v-else-if="!visibleChartSettings"
		v-for="(chartSpec, index) of preparedCharts"
		:key="chartSpec.id + index"
		:visualization-spec="getChartSpec(chartSpec)"
		:interactive="false"
	/>
	<tera-operator-placeholder v-else-if="placeholder" :node="node">
		{{ placeholder }}
	</tera-operator-placeholder>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import _ from 'lodash';
import VegaChart from '@/components/widgets/VegaChart.vue';
import { ChartSetting } from '@/types/common';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import { WorkflowNode } from '@/types/workflow';

const props = defineProps<{
	node: WorkflowNode<any>;
	preparedCharts: any;
	chartSettings: ChartSetting[][] | ChartSetting[];
	chartSettingKey?: string;
	isLoading?: boolean;
	placeholder?: string;
	processing?: string;
	expandable?: boolean;
	areEmbedActionsVisible?: boolean;
}>();

const getChartSpec = (spec: any[] | any) => (_.isArray(spec) ? spec[0] : spec);

const visibleChartSettings = computed(() => {
	if (props.chartSettings) {
		// null check
		if (_.isArray(props.chartSettings[0])) {
			// ChartSetting[][] check
			return (props.chartSettings as ChartSetting[][]).map((settingsArray) =>
				settingsArray.filter((setting) => !setting?.hideInNode)
			);
		}
		// ChartSetting[]
		return (props.chartSettings as ChartSetting[]).filter((setting) => !setting?.hideInNode);
	}
	return null;
});
</script>
s
