<template>
	<tera-progress-spinner v-if="isLoading" :font-size="2" is-centered style="height: 100%">
		Processing...
	</tera-progress-spinner>
	<ul v-else-if="visibleChartSettings.length">
		<li v-for="setting of visibleChartSettings" :key="setting.id">
			<vega-chart
				expandable
				:are-embed-actions-visible="areEmbedActionsVisible"
				:visualization-spec="preparedCharts[setting.id]"
				:interactive="interactive"
			/>
		</li>
	</ul>
	<tera-operator-placeholder v-else-if="placeholder" :node="node">
		{{ placeholder }}
	</tera-operator-placeholder>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { VisualizationSpec } from 'vega-embed';

import VegaChart from '@/components/widgets/VegaChart.vue';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import { WorkflowNode } from '@/types/workflow';

const props = defineProps<{
	node: WorkflowNode<any>;
	preparedCharts: Record<string, VisualizationSpec>;
	chartSettings: any;
	isLoading?: boolean;
	placeholder?: string;
	interactive?: boolean;
	areEmbedActionsVisible?: boolean;
}>();

const visibleChartSettings = computed(() => props.chartSettings.filter((setting) => !setting?.hideInNode));
</script>
