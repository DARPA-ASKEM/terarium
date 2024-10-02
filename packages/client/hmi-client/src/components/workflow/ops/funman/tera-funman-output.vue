<template>
	<header class="flex align-items-start">
		<div>
			<h4>{{ contractedModel?.header.name }}</h4>
			<span class="secondary-text">Output generated date</span>
		</div>
		<div class="btn-group">
			<Button label="Add to report" outlined severity="secondary" disabled />
			<Button label="Save for reuse" outlined severity="secondary" />
		</div>
	</header>
	<Accordion multiple :active-index="[0, 1, 2, 3]">
		<AccordionTab header="Summary"> Summary text </AccordionTab>
		<AccordionTab>
			<template #header> State variables<i class="pi pi-info-circle" /> </template>
			<template v-if="stateChart">
				<Dropdown v-model="selectedState" :options="stateOptions" @update:model-value="updateStateChart" />
				<vega-chart :visualization-spec="stateChart" :are-embed-actions-visible="false" />
			</template>
			<span class="ml-4" v-else> No boxes were generated. </span>
		</AccordionTab>
		<AccordionTab>
			<template #header>Parameters<i class="pi pi-info-circle" /></template>
			<vega-chart :visualization-spec="parameterCharts" :are-embed-actions-visible="false" />
		</AccordionTab>
		<!--TODO: Read only model configuration-->
		<!-- <AccordionTab v-if="contractedModel" header="Validated configuration"> -->
		<!-- <tera-model-diagram :model="contractedModel"/> -->
		<!-- </AccordionTab> -->
	</Accordion>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import {
	type ProcessedFunmanResult,
	type FunmanConstraintsResponse,
	processFunman
} from '@/services/models/funman-service';
import { createFunmanStateChart, createFunmanParameterChart } from '@/services/charts';
import VegaChart from '@/components/widgets/VegaChart.vue';
import { getRunResult } from '@/services/models/simulation-service';
import Dropdown from 'primevue/dropdown';
import Button from 'primevue/button';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
// import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue'; // TODO: Once we save the output model properly in the backend we can use this.
import { logger } from '@/utils/logger';
import type { Model } from '@/types/Types';

const props = defineProps<{
	funModelId: string;
	trajectoryState?: string;
}>();

const emit = defineEmits(['update:trajectoryState']);

let processedFunmanResult: ProcessedFunmanResult | null = null;
let constraintsResponse: FunmanConstraintsResponse[] = [];

const contractedModel = ref<Model | null>(null);
const stateOptions = ref<string[]>([]);
const selectedState = ref<string>('');

const stateChart = ref();
const parameterCharts = ref();

const initalize = async () => {
	// props.funModelId bf7ef7f4-b8ab-4008-b03c-d0c96a7c763f
	const rawFunmanResult = await getRunResult(props.funModelId, 'validation.json');
	if (!rawFunmanResult) {
		logger.error('Failed to fetch funman result');
		return;
	}
	const funmanResult = JSON.parse(rawFunmanResult);
	constraintsResponse = funmanResult.request.constraints;
	stateOptions.value = funmanResult.model.petrinet.model.states.map(({ id }) => id);

	processedFunmanResult = processFunman(funmanResult);
	console.log('processedFunmanResult', processedFunmanResult);
	console.log(funmanResult);

	console.log('processedFunmanResult', processedFunmanResult);
	console.log(funmanResult);

	// State chart
	selectedState.value = props.trajectoryState ?? stateOptions.value[0];
	updateStateChart();

	// Parameter charts
	const parametersOfInterest = funmanResult.request.parameters.filter((d: any) => d.label === 'all');
	if (processedFunmanResult.boxes) {
		parameterCharts.value = createFunmanParameterChart(parametersOfInterest, processedFunmanResult.boxes);
	}
};

function updateStateChart() {
	if (!processedFunmanResult) return;
	emit('update:trajectoryState', selectedState.value);
	stateChart.value = createFunmanStateChart(processedFunmanResult, constraintsResponse, selectedState.value);
}

watch(
	() => props.funModelId,
	() => {
		initalize();
	},
	{ immediate: true }
);
</script>

<style scoped>
.btn-group {
	display: flex;
	align-items: center;
	gap: var(--gap-small);
	margin-left: auto;
}

.pi-info-circle {
	margin-left: var(--gap-2);
}

.secondary-text {
	color: var(--text-color-subdued);
	font-size: var(--font-caption);
}
</style>
