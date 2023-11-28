<template>
	<div class="section-row">
		<label>Trajectory State</label>
		<Dropdown v-model="selectedTrajState" :options="modelStates"> </Dropdown>
	</div>
	<div ref="trajRef"></div>

	<h4>Configuration parameters <i class="pi pi-info-circle" /></h4>
	<p class="secondary-text">
		Adjust parameter ranges to only include values in the green region or less.
	</p>

	<!-- TODO: add boxes modal per row https://github.com/DARPA-ASKEM/terarium/issues/1924 -->
	<div class="variables-table">
		<div class="variables-header">
			<header
				v-for="(title, index) in ['Parameter', 'Lower bound', 'Upper bound', '', '']"
				:key="index"
			>
				{{ title }}
			</header>
		</div>
		<div v-for="(column, index) in lastTrueBox?.bounds" :key="index">
			<div class="variables-row" v-if="parameterOptions.includes(index.toString())">
				<div>{{ index.toString() }}</div>
				<div>{{ column?.lb }}</div>
				<div>{{ column?.ub }}</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import {
	getQueries,
	processFunman,
	renderFumanTrajectories
} from '@/services/models/funman-service';
import Dropdown from 'primevue/dropdown';

const props = defineProps<{
	funModelId: string;
}>();

const parameterOptions = ref<string[]>([]);
const selectedParam = ref();
const selectedTrajState = ref();
const modelStates = ref<string[]>();
const timestepOptions = ref();
const timestep = ref();
const trajRef = ref();
const boxId = 'box2';
const lastTrueBox = ref();

const initalizeParameters = async () => {
	const funModel = await getQueries(props.funModelId);
	parameterOptions.value = [];
	funModel.model.petrinet.semantics.ode.parameters.map((ele) =>
		parameterOptions.value.push(ele.id)
	);
	selectedParam.value = parameterOptions.value[0];
	timestepOptions.value = funModel.request.structure_parameters[0].schedules[0].timepoints;
	timestep.value = timestepOptions.value[1];
	const tempList: string[] = [];
	funModel.model.petrinet.model.states.forEach((element) => {
		tempList.push(element.id);
	});
	modelStates.value = tempList;
	selectedTrajState.value = modelStates.value[0];

	lastTrueBox.value = funModel.parameter_space.true_boxes.at(-1);
};

const renderGraph = async () => {
	const width = 800;
	const height = 250;
	const funModel = await getQueries(props.funModelId);
	const processedData = processFunman(funModel);
	renderFumanTrajectories(
		trajRef.value as HTMLElement,
		processedData,
		selectedTrajState.value,
		boxId,
		{
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
	() => [selectedParam.value, timestep.value, selectedTrajState.value],
	async () => {
		renderGraph();
	}
);
</script>

<style scoped>
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
	padding: 0.25rem;
}

.variables-row {
	display: grid;
	grid-template-columns: repeat(6, 1fr) 0.5fr;
	grid-template-rows: 1fr 1fr;
	border-top: 1px solid var(--surface-border);
}

.variables-header {
	display: grid;
	grid-template-columns: repeat(6, 1fr) 0.5fr;
}

header {
	padding-right: 1rem;
}
</style>
