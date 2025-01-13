<template>
	<section>
		<ul v-if="node.state.interventionPolicy.id">
			<li v-for="(_interventions, appliedTo) in selectedOutputParameters" :key="appliedTo">
				<vega-chart
					expandable
					:are-embed-actions-visible="false"
					:visualization-spec="preparedCharts[appliedTo]"
					:interactive="false"
				/>
			</li>
		</ul>
		<tera-intervention-summary-card
			class="intervention-title"
			v-for="(intervention, index) in interventionSummary"
			:intervention="intervention"
			:key="index"
		/>
		<tera-progress-spinner is-centered :font-size="2" v-if="isLoading" />
		<Button
			:label="isModelInputConnected ? 'Open' : 'Attach a model'"
			@click="emit('open-drilldown')"
			severity="secondary"
			outlined
			:disabled="!isModelInputConnected"
			class="mt-2"
		/>
	</section>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { WorkflowNode, WorkflowPortStatus } from '@/types/workflow';
import Button from 'primevue/button';
import _, { cloneDeep, groupBy } from 'lodash';
import { blankIntervention, flattenInterventionData } from '@/services/intervention-policy';
import { createInterventionChart } from '@/services/charts';
import VegaChart from '@/components/widgets/VegaChart.vue';
import { useClientEvent } from '@/composables/useClientEvent';
import { type ClientEvent, ClientEventType, type TaskResponse, TaskStatus } from '@/types/Types';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import TeraInterventionSummaryCard from '@/components/intervention-policy/tera-intervention-summary-card.vue';
import { InterventionPolicyState } from './intervention-policy-operation';

const emit = defineEmits(['open-drilldown', 'update-state']);
const props = defineProps<{
	node: WorkflowNode<InterventionPolicyState>;
}>();

const taskIds = ref<string[]>([]);

const interventionEventHandler = async (event: ClientEvent<TaskResponse>) => {
	if (!taskIds.value.includes(event.data?.id)) return;
	if ([TaskStatus.Success, TaskStatus.Cancelled, TaskStatus.Failed].includes(event.data.status)) {
		taskIds.value = taskIds.value.filter((id) => id !== event.data.id);
	}
};

useClientEvent(ClientEventType.TaskGollmInterventionsFromDocument, interventionEventHandler);

const isLoading = computed(() => taskIds.value.length > 0);
const isModelInputConnected = ref(false);

const groupedOutputParameters = computed(() =>
	Object.fromEntries(
		Object.entries(groupBy(flattenInterventionData(props.node.state.interventionPolicy.interventions), 'appliedTo'))
	)
);

const interventionSummary = computed(() => {
	const interventions = cloneDeep(props.node.state.interventionPolicy.interventions);
	return interventions.length > 4 ? interventions.splice(0, 4) : interventions;
});

const selectedOutputParameters = computed(() => {
	const charts = {};
	props.node.state.selectedCharts?.forEach((chart) => {
		const paramOutput = groupedOutputParameters.value[chart];
		if (paramOutput) charts[chart] = paramOutput;
	});
	return charts;
});

const preparedCharts = computed(() =>
	_.mapValues(groupedOutputParameters.value, (interventions, key) =>
		createInterventionChart(interventions, {
			title: key,
			width: 180,
			height: 120,
			xAxisTitle: 'Time',
			yAxisTitle: 'Value',
			hideLabels: false
		})
	)
);

watch(
	() => props.node.inputs,
	(inputs) => {
		const modelId = inputs.find((input) => input.type === 'modelId')?.value?.[0];
		const state = cloneDeep(props.node.state);

		const modelInputs = inputs.filter((input) => input.type === 'modelId');
		if (modelInputs[0].status === WorkflowPortStatus.CONNECTED) {
			isModelInputConnected.value = true;
		}

		if (!modelId || modelId === state.interventionPolicy?.modelId) return;

		// Reset previous model cache
		state.interventionPolicy = {
			modelId,
			interventions: [blankIntervention]
		};
		emit('update-state', state);
	},
	{ immediate: true, deep: true }
);

watch(
	() => props.node.state.taskIds,
	() => {
		taskIds.value = props.node.state.taskIds ?? [];
	}
);

watch(
	() => isLoading.value,
	() => {
		if (!isLoading.value) {
			const state = cloneDeep(props.node.state);
			state.taskIds = [];
			emit('update-state', state);
		}
	}
);
</script>

<style scoped>
ul {
	list-style-type: none;
}
.intervention-title {
	& > :deep(h5) {
		padding-top: 15px;
	}
}
</style>
