<template>
	<div class="section-row">
		<label>Trajectory State</label>
		<Dropdown
			v-model="selectedTrajState"
			:options="modelStates"
			@update:model-value="emit('update:trajectoryState', $event)"
		>
		</Dropdown>
	</div>
	<div ref="trajRef"></div>

	<h4>Configuration parameters <i class="pi pi-info-circle" /></h4>
	<p class="secondary-text" v-if="selectedParam2 === ''">
		Adjust parameter ranges to only include values in the green region or less.
	</p>

	<div class="variables-table" v-if="selectedParam2">
		<section class="boundary-drilldown">
			<div class="boundary-drilldown-header">
				{{ selectedParam }} : {{ selectedParam2 }} pairwise drilldown
				<Button class="close-mask" icon="pi pi-times" text rounded aria-label="Close" @click="selectedParam2 = ''" />
			</div>
			<tera-funman-boundary-chart
				:processed-data="processedData as FunmanProcessedData"
				:param1="selectedParam"
				:param2="selectedParam2"
				:options="drilldownChartOptions"
				:timestep="timestep"
				:selectedBoxId="selectedBoxId"
			/>
		</section>
	</div>

	<div v-if="selectedParam2 === ''" class="variables-table">
		<div class="variables-header">
			<header v-for="(title, index) in ['select', 'Parameter', 'Lower bound', 'Upper bound', '', '']" :key="index">
				{{ title }}
			</header>
		</div>

		<div v-for="(bound, parameter) in lastTrueBox?.bounds" :key="parameter + Date.now()">
			<div class="variables-row" v-if="parameterOptions.includes(parameter)">
				<RadioButton v-model="selectedParam" :value="parameter" />
				<div>{{ parameter }}</div>
				<div>{{ formatNumber(bound.lb) }}</div>
				<div>{{ formatNumber(bound.ub) }}</div>
				<tera-funman-boundary-chart
					v-if="processedData"
					:processed-data="processedData"
					:param1="selectedParam"
					:param2="parameter"
					:timestep="timestep"
					:selectedBoxId="selectedBoxId"
					@click="selectedParam2 = parameter"
				/>
				<div v-if="selectedBoxId !== ''">
					{{ formatNumber(selectedBox[parameter][0]) }} :
					{{ formatNumber(selectedBox[parameter][1]) }}
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { FunmanProcessedData, processFunman, renderFumanTrajectories } from '@/services/models/funman-service';
import { getRunResult } from '@/services/models/simulation-service';
import Dropdown from 'primevue/dropdown';
import RadioButton from 'primevue/radiobutton';
import Button from 'primevue/button';
// import InputNumber from 'primevue/inputnumber';
import type { FunmanBox, RenderOptions } from '@/services/models/funman-service';
import TeraFunmanBoundaryChart from './tera-funman-boundary-chart.vue';

const props = defineProps<{
	funModelId: string;
	trajectoryState?: string;
}>();

const emit = defineEmits(['update:trajectoryState']);

const parameterOptions = ref<string[]>([]);
const selectedParam = ref<string>('');
const selectedParam2 = ref<string>('');

const selectedTrajState = ref<string>('');
const modelStates = ref<string[]>([]);
const timestepOptions = ref();
const timestep = ref();
const trajRef = ref();

const lastTrueBox = ref<FunmanBox>();
const processedData = ref<FunmanProcessedData>();

const selectedBoxId = ref('');
const selectedBox = ref<any>({});

let inputConstraints: any[] = [];

const drilldownChartOptions = ref<RenderOptions>({
	width: 550,
	height: 275,
	click: (d: any) => {
		if (d.id === selectedBoxId.value) {
			selectedBoxId.value = '';
		} else {
			selectedBoxId.value = d.id;
		}
	}
});

// TODO: better range-bound logic
const formatNumber = (v: number) => {
	if (v.toString().includes('.')) {
		return v.toFixed(4);
	}
	return v;
};

const initalizeParameters = async () => {
	const rawFunmanResult = await getRunResult(props.funModelId, 'validation.json');
	const funmanResult = JSON.parse(rawFunmanResult);

	inputConstraints = funmanResult.request.constraints;
	processedData.value = processFunman(funmanResult);
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

	selectedTrajState.value = props.trajectoryState || modelStates.value[0];

	lastTrueBox.value = funmanResult.parameter_space.true_boxes?.at(-1);

	if (selectedTrajState.value) {
		renderGraph(selectedBoxId.value);
	}
};

const renderGraph = async (boxId: string) => {
	const width = 580;
	const height = 180;
	renderFumanTrajectories(
		trajRef.value as HTMLElement,
		processedData.value as FunmanProcessedData,
		selectedTrajState.value,
		boxId,
		{
			constraints: inputConstraints,
			width,
			height
		}
	);
};

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
	// () => [selectedParam.value, timestep.value, selectedTrajState.value, selectedBoxId.value],
	() => [selectedParam.value, selectedTrajState.value],
	() => {
		renderGraph(selectedBoxId.value);
	}
);

watch(
	() => [selectedBoxId.value],
	() => {
		selectedBox.value = processedData.value?.boxes.find((d) => d.id === selectedBoxId.value);
		renderGraph(selectedBoxId.value);
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
