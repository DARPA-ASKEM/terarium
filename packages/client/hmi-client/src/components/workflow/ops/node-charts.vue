<template>
	<tera-progress-spinner v-if="isLoading" :font-size="2" is-centered style="height: 100%">
		<div v-if="processing">{{ processing }}</div>
		<div v-else>Processing...</div>
	</tera-progress-spinner>
	<div v-else-if="visibleChartSettings.length">
		<div v-for="setting of visibleChartSettings" :key="chartSettingKey || setting.id">
			<vega-chart
				:expandable="expandable"
				:are-embed-actions-visible="areEmbedActionsVisible"
				:visualization-spec="preparedCharts[chartSettingKey || setting.id]"
				:interactive="interactive"
			/>
		</div>
	</div>
	<tera-operator-placeholder v-else-if="placeholder" :node="node">
		{{ placeholder }}
	</tera-operator-placeholder>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import VegaChart from '@/components/widgets/VegaChart.vue';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import { WorkflowNode } from '@/types/workflow';

const props = defineProps<{
	node: WorkflowNode<any>;
	preparedCharts: any;
	chartSettings: any;
	chartSettingKey?: string;
	isLoading?: boolean;
	placeholder?: string;
	processing?: string;
	interactive?: boolean;
	expandable?: boolean;
	areEmbedActionsVisible?: boolean;
}>();

const visibleChartSettings = computed(() => props.chartSettings.filter((setting) => !setting?.hideInNode));
</script>
