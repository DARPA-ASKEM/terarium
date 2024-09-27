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
			<Dropdown
				v-model="selectedState"
				:options="modelStates"
				@update:model-value="emit('update:trajectoryState', $event)"
			>
			</Dropdown>
			<div ref="trajRef" />
			<vega-chart :visualization-spec="stateChart" :are-embed-actions-visible="false" />
		</AccordionTab>
		<AccordionTab>
			<template #header>Parameters<i class="pi pi-info-circle" /></template>
			<p class="secondary-text" v-if="selectedParam2 === ''">
				Adjust parameter ranges to only include values in the green region or less.
			</p>
			<div class="variables-table" v-if="selectedParam2">
				<section class="boundary-drilldown">
					<div class="boundary-drilldown-header">
						{{ selectedParam }} : {{ selectedParam2 }} pairwise drilldown
						<Button
							class="close-mask"
							icon="pi pi-times"
							text
							rounded
							aria-label="Close"
							@click="selectedParam2 = ''"
						/>
					</div>
					<!-- <tera-funman-boundary-chart
						:processed-data="processedData as FunmanProcessedData"
						:param1="selectedParam"
						:param2="selectedParam2"
						:options="drilldownChartOptions"
						:timestep="timestep"
						:selectedBoxId="selectedBoxId"
					/> -->
				</section>
			</div>
			<div class="variables-table" v-if="selectedParam2 === ''">
				<div class="variables-header">
					<header v-for="(title, index) in ['Parameter', 'Lower bound', 'Upper bound', '', '']" :key="index">
						{{ title }}
					</header>
				</div>

				<div v-for="(bound, parameter) in lastTrueBox?.bounds" :key="parameter + Date.now()">
					<div class="variables-row" v-if="parameterOptions.includes(parameter)">
						<div>{{ parameter }}</div>
						<div>{{ formatNumber(bound.lb) }}</div>
						<div>{{ formatNumber(bound.ub) }}</div>
						<!-- <tera-funman-boundary-chart
							v-if="processedData"
							:processed-data="processedData"
							:param1="selectedParam"
							:param2="parameter"
							:timestep="timestep"
							:selectedBoxId="selectedBoxId"
							@click="selectedParam2 = parameter"
						/> -->
						<div v-if="selectedBoxId !== ''">
							{{ formatNumber(selectedBox[parameter][0]) }} :
							{{ formatNumber(selectedBox[parameter][1]) }}
						</div>
					</div>
				</div>
			</div>
		</AccordionTab>
		<AccordionTab v-if="contractedModel" header="Validated configuration"
			><tera-model-diagram :model="contractedModel"
		/></AccordionTab>
	</Accordion>
	{{ funModelId }}
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { ProcessedFunmanResult, processFunman } from '@/services/models/funman-service';
import { createFunmanStateChart } from '@/services/charts';
import VegaChart from '@/components/widgets/VegaChart.vue';
import { getRunResult } from '@/services/models/simulation-service';
import Dropdown from 'primevue/dropdown';
import Button from 'primevue/button';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import type { FunmanBox } from '@/services/models/funman-service';
import { logger } from '@/utils/logger';
import type { Model } from '@/types/Types';
import { ConstraintGroup } from './funman-operation';

const props = defineProps<{
	funModelId: string;
	trajectoryState?: string;
}>();

const emit = defineEmits(['update:trajectoryState']);

const parameterOptions = ref<string[]>([]);
const selectedParam = ref<string>('');
const selectedParam2 = ref<string>('');
const contractedModel = ref<Model | null>(null);

const selectedState = ref<string>('');
const modelStates = ref<string[]>([]);
const timestepOptions = ref();
const timestep = ref();
const trajRef = ref();
const stateChart = ref();

const lastTrueBox = ref<FunmanBox>();
const processedData = ref<ProcessedFunmanResult>();

const selectedBoxId = ref('');
const selectedBox = ref<any>({});

let constraintGroups: ConstraintGroup[] = [];

// const drilldownChartOptions = ref<RenderOptions>({
// 	width: 550,
// 	height: 275,
// 	click: (d: any) => {
// 		if (d.id === selectedBoxId.value) {
// 			selectedBoxId.value = '';
// 		} else {
// 			selectedBoxId.value = d.id;
// 		}
// 	}
// });

// TODO: better range-bound logic
const formatNumber = (v: number) => {
	if (v.toString().includes('.')) {
		return v.toFixed(4);
	}
	return v;
};

const initalizeParameters = async () => {
	// bf7ef7f4-b8ab-4008-b03c-d0c96a7c763f
	const rawFunmanResult = await getRunResult('bf7ef7f4-b8ab-4008-b03c-d0c96a7c763f', 'validation.json');
	if (!rawFunmanResult) {
		logger.error('Failed to fetch funman result');
		return;
	}

	const funmanResult = JSON.parse(rawFunmanResult);
	console.log(funmanResult);

	// funmanResult.contracted_model.header.schema = funmanResult.contracted_model.header.schema_;
	// delete funmanResult.contracted_model.header.schema_;
	contractedModel.value = funmanResult.contracted_model;

	constraintGroups = funmanResult.request.constraints;
	processedData.value = processFunman(funmanResult);
	console.log(processedData.value);
	parameterOptions.value = [];

	const initialVars = funmanResult.model.petrinet.semantics?.ode.initials.map((d) => d.expression);

	funmanResult.model.petrinet.semantics.ode.parameters
		.filter((ele: any) => !initialVars.includes(ele.id))
		.map((ele: any) => parameterOptions.value.push(ele.id));
	selectedParam.value = parameterOptions.value[0];
	timestepOptions.value = funmanResult.request.structure_parameters[0].schedules[0].timepoints;
	// timestep.value = timestepOptions.value[1];
	timestep.value = timestepOptions.value[timestepOptions.value.length - 1];

	modelStates.value = [];
	funmanResult.model.petrinet.model.states.forEach((element) => {
		modelStates.value.push(element.id);
	});

	selectedState.value = props.trajectoryState || modelStates.value[0];

	lastTrueBox.value = funmanResult.parameter_space.true_boxes?.at(-1);
};

function changeStateChart() {
	if (!processedData.value) return;
	stateChart.value = createFunmanStateChart(
		processedData.value,
		constraintGroups,
		selectedState.value,
		selectedBoxId.value
	);
}

onMounted(() => {
	initalizeParameters();
});

watch(
	// When props change reset params rerender graph
	() => props.funModelId,
	async () => {
		initalizeParameters();
	}
);

watch(
	// Whenever user changes options rerender.
	// () => [selectedParam.value, timestep.value, selectedState.value, selectedBoxId.value],
	() => [selectedParam.value, selectedState.value],
	() => {
		changeStateChart();
	}
);

watch(
	() => [selectedBoxId.value],
	() => {
		selectedBox.value = processedData.value?.boxes.find((d) => d.id === selectedBoxId.value);
		changeStateChart();
	}
);
</script>

<style scoped>
.p-inputtext {
	width: 63px;
	padding: 12px 16px;
	align-items: center;
	gap: 16px;
	align-self: stretch;
	border-radius: 6px;
	border: 1px solid var(--00-neutral-300, #c3ccd6);
	background: var(--White, #fff);
}
.section-row {
	display: flex;
	/* flex-direction: column; */
	padding: 0.5rem 0rem;
	align-items: center;
	gap: 0.8125rem;
}

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
	color: var(--Text-Secondary, #667085);
	/* Body Small/Regular */
	font-size: 0.875rem;
	font-style: normal;
	font-weight: 400;
	line-height: 1.3125rem; /* 150% */
	letter-spacing: 0.01563rem;
}

.variables-table {
	display: grid;
	grid-template-columns: 1fr;
}

.variables-table div {
	padding-top: 0.25rem;
}

.variables-row {
	display: grid;
	grid-template-columns: repeat(6, 1fr) 0.5fr;
	grid-template-rows: 1fr;
	border-top: 1px solid var(--surface-border);
}

.variables-header {
	display: grid;
	grid-template-columns: repeat(6, 1fr) 0.5fr;
}

.boundary-drilldown {
	border: 1px solid var(--00-neutral-300, #c3ccd6);
	padding: 5px;
}

.boundary-drilldown-header {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: center;
	font-size: var(--font-body-medium);
}
</style>
