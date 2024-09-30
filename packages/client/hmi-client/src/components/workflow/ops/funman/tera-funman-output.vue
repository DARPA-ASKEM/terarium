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
			<vega-chart
				v-for="(parameterChart, index) in parameterCharts"
				:key="index"
				:visualization-spec="parameterChart"
				:are-embed-actions-visible="false"
			/>
			<p class="secondary-text" v-if="selectedParam2 === ''">
				Adjust parameter ranges to only include values in the green region or less.
			</p>
			<div class="variables-table" v-if="selectedParam2 === ''">
				<div class="variables-header">
					<header v-for="(title, index) in ['Parameter', 'Lower bound', 'Upper bound', '', '']" :key="index">
						{{ title }}
					</header>
				</div>
				<div v-for="(bound, parameter) in lastTrueBox?.bounds" :key="parameter + Date.now()">
					<div class="variables-row" v-if="parameterOptions.includes(parameter.toString())">
						<div>{{ parameter }}</div>
						<div>{{ formatNumber(bound.lb) }}</div>
						<div>{{ formatNumber(bound.ub) }}</div>
						<div v-if="selectedBoxId !== ''">
							{{ formatNumber(selectedBox[parameter][0]) }} :
							{{ formatNumber(selectedBox[parameter][1]) }}
						</div>
					</div>
				</div>
			</div>
		</AccordionTab>
		<AccordionTab v-if="contractedModel" header="Validated configuration">
			<!--TODO: Read only model configuration-->
			<!-- <tera-model-diagram :model="contractedModel"/> -->
		</AccordionTab>
	</Accordion>
	{{ funModelId }}
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { ProcessedFunmanResult, processFunman } from '@/services/models/funman-service';
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
import type { MiraModel, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import { collapseParameters } from '@/model-representation/mira/mira';
import { ConstraintGroup } from './funman-operation';

const props = defineProps<{
	funModelId: string;
	trajectoryState?: string;
	// TODO: Once the output model is saved properly in the backend we won't need these as props, they can be generated within here.
	mmt: MiraModel;
	mmtParams: MiraTemplateParams;
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
const parameterCharts = ref<any[]>([]);

const lastTrueBox = ref<any>();
const processedData = ref<ProcessedFunmanResult>();

const selectedBoxId = ref('');
const selectedBox = ref<any>({});

let constraintGroups: ConstraintGroup[] = [];

// TODO: better range-bound logic
const formatNumber = (v: number) => {
	if (v.toString().includes('.')) {
		return v.toFixed(4);
	}
	return v;
};

const initalizeParameters = async () => {
	// props.funModelId
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

	const parametersOfInterest = funmanResult.request.parameters.filter((d: any) => d.label === 'all');

	if (processedData.value) {
		const collapsedParameters = collapseParameters(props.mmt, props.mmtParams);
		collapsedParameters.forEach((value, key) => {
			if (!processedData.value) return;
			// console.log(parametersOfInterest );
			console.log(key, value);
			// const parameterChildren =
			parameterCharts.value.push(createFunmanParameterChart(parametersOfInterest, processedData.value.boxes));
		});
	}

	// console.log(funmanResult.structure_parameters);

	lastTrueBox.value = funmanResult.parameter_space.true_boxes?.at(-1);
};

function changeStateChart() {
	if (!processedData.value) return;
	stateChart.value = createFunmanStateChart(
		processedData.value,
		constraintGroups,
		selectedState.value
		// selectedBoxId.value
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
</style>
