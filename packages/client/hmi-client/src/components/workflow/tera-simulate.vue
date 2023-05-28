<template>
	<section class="tera-simulate">
		<h3>Simulate</h3>
		<SimulateChart
			v-for="index in openedWorkflowNodeStore.numCharts"
			:key="index"
			:run-results="props.node.outputs[0].value.runResults"
			:run-id-list="props.node.outputs[0].value.runIdList"
			:chart-idx="index"
		/>
	</section>
</template>

<script setup lang="ts">
// import { computed } from 'vue';
// import { ModelConfig } from '@/types/ModelConfig';
import { WorkflowNode } from '@/types/workflow';

import { useOpenedWorkflowNodeStore } from '@/stores/opened-workflow-node';
import SimulateChart from './tera-simulate-chart.vue';

const props = defineProps<{
	node: WorkflowNode;
}>();
const openedWorkflowNodeStore = useOpenedWorkflowNodeStore();
</script>

<style scoped>
.tera-simulate {
	background: white;
	z-index: 1;
}
</style>
