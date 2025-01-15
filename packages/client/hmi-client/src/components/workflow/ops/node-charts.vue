<template>
	<tera-progress-spinner v-if="isLoading" :font-size="2" is-centered style="height: 100%">
		Processing...
	</tera-progress-spinner>
	<tera-operator-placeholder v-else-if="!selectedVariableSettings.length" :node="node"
		>Attach datasets/simulation outputs to compare</tera-operator-placeholder
	>
	<ul v-else>
		<li v-for="setting of selectedVariableSettings" :key="setting.id">
			<vega-chart
				expandable
				:are-embed-actions-visible="true"
				:visualization-spec="preparedCharts[setting.id]"
				:interactive="true"
			/>
		</li>
	</ul>
</template>

<script setup lang="ts">
import VegaChart from '@/components/widgets/VegaChart.vue';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import { WorkflowNode } from '@/types/workflow';
import { VisualizationSpec } from 'vega-embed';

const props = defineProps<{
	preparedCharts: Record<string, VisualizationSpec>;
	isLoading: boolean;
	node: WorkflowNode<any>;
	selectedVariableSettings: any;
}>();

// need to pass lint, can remove when settings filter the charts
console.log(props);
</script>
