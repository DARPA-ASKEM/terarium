<template>
	<tera-progress-spinner v-if="isLoading" :font-size="2" is-centered style="height: 100%">
		<div v-if="processing">{{ processing }}</div>
		<div v-else>Processing...</div>
	</tera-progress-spinner>
	<div v-else-if="_.isArray(visibleChartSettings[0])">
		<div v-for="(settingsArray, index) of visibleChartSettings" :key="index">
			<vega-chart
				v-for="setting of settingsArray"
				:key="chartSettingKey || setting.id"
				:expandable="expandable"
				:are-embed-actions-visible="areEmbedActionsVisible"
				:visualization-spec="preparedCharts[index][chartSettingKey || setting.id]"
				:interactive="false"
			/>
		</div>
	</div>
	<div v-else-if="visibleChartSettings.length">
		<div v-for="setting of visibleChartSettings" :key="chartSettingKey || setting.id">
			<vega-chart
				:expandable="expandable"
				:are-embed-actions-visible="areEmbedActionsVisible"
				:visualization-spec="preparedCharts[chartSettingKey || setting.id]"
				:interactive="false"
			/>
		</div>
	</div>
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
	chartSettings: ChartSetting[] | null;
	chartSettingKey?: string;
	isLoading?: boolean;
	placeholder?: string;
	processing?: string;
	expandable?: boolean;
	areEmbedActionsVisible?: boolean;
}>();

const visibleChartSettings = computed(() => {
	if (_.isArray(props.chartSettings[0])) {
		return props.chartSettings.map((settingsArray) => settingsArray.filter((setting) => !setting?.hideInNode));
	}
	return props.chartSettings.filter((setting) => !setting?.hideInNode);
});
</script>
