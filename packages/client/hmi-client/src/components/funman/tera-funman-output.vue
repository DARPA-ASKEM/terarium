<template>
	<div class="section-row">
		<label>Select Parameter One</label>
		<Dropdown v-model="selectedParamOne" :options="parameterOptions"> </Dropdown>
	</div>
	<div class="section-row">
		<label>Select Parameter Two</label>
		<Dropdown v-model="selectedParamTwo" :options="parameterOptions"> </Dropdown>
	</div>
	<div class="section-row">
		<label>Trajectory State</label>
		<Dropdown v-model="selectedTrajState" :options="modelStates"> </Dropdown>
	</div>
	<div class="section-row">
		<label>Timestep</label>
		<Dropdown v-model="timestep" :options="timestepOptions"> </Dropdown>
	</div>
	<div class="container">
		<div ref="boxRef"></div>
		<div ref="trajRef"></div>
	</div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import {
	getQueries,
	processFunman,
	renderFumanTrajectories,
	renderFunmanBoundaryChart
} from '@/services/models/funman-service';
import Dropdown from 'primevue/dropdown';
// import Chart from 'primevue/chart';

// X = traj[i].timestep
// Y = traj[i].(selectedTrajState)

const props = defineProps<{
	funModelId: string;
}>();

const parameterOptions = ref<string[]>([]);
const selectedParamOne = ref();
const selectedTrajState = ref();
const modelStates = ref<string[]>();
const selectedParamTwo = ref();
const timestepOptions = ref();
const timestep = ref();
const boxRef = ref();
const trajRef = ref();
const boxId = 'box2';

const initalizeParameters = async () => {
	const funModel = await getQueries(props.funModelId);
	parameterOptions.value = [];
	funModel.model.petrinet.semantics.ode.parameters.map((ele) =>
		parameterOptions.value.push(ele.id)
	);
	selectedParamOne.value = parameterOptions.value[0];
	selectedParamTwo.value = parameterOptions.value[0];
	timestepOptions.value = funModel.request.structure_parameters[0].schedules[0].timepoints;
	timestep.value = timestepOptions.value[1];
	const tempList: string[] = [];
	funModel.model.petrinet.model.states.forEach((element) => {
		tempList.push(element.id);
	});
	modelStates.value = tempList;
	selectedTrajState.value = modelStates.value[0];
};

const renderGraph = async () => {
	const funModel = await getQueries(props.funModelId);
	console.log(funModel);
	const width = 800;
	const height = 250;
	const processedData = processFunman(funModel);
	renderFunmanBoundaryChart(
		boxRef.value,
		processedData,
		selectedParamOne.value,
		selectedParamTwo.value,
		timestep.value,
		{
			width,
			height
		}
	);
	renderFumanTrajectories(
		trajRef.value as HTMLElement,
		processedData,
		boxId,
		{
			width,
			height
		},
		selectedTrajState.value
	);
};

// const setBarChartData = () => {
// 	const documentStyle = getComputedStyle(document.documentElement);
// 	const weights = ensembleConfigs.value.map((element) => element.weight);
// 	return {
// 		labels: listModelLabels.value,
// 		datasets: [
// 			{
// 				backgroundColor: documentStyle.getPropertyValue('--text-color-secondary'),
// 				borderColor: documentStyle.getPropertyValue('--text-color-secondary'),
// 				data: weights,
// 				categoryPercentage: CATEGORYPERCENTAGE,
// 				barPercentage: BARPERCENTAGE,
// 				minBarLength: MINBARLENGTH
// 			}
// 		]
// 	};
// };

// const CHART_OPTIONS = {
// 	devicePixelRatio: 4,
// 	maintainAspectRatio: false,
// 	pointStyle: false,
// 	animation: {
// 		duration: 0
// 	},
// 	showLine: true,
// 	plugins: {
// 		legend: {
// 			display: false
// 		}
// 	},
// 	scales: {
// 		x: {
// 			ticks: {
// 				color: '#aaa',
// 				maxTicksLimit: 5,
// 				includeBounds: true,
// 				// this rounds the tick label to nearest int
// 				callback: (num) => num
// 			},
// 			grid: {
// 				color: '#fff',
// 				borderColor: '#fff'
// 			}
// 		},
// 		y: {
// 			ticks: {
// 				color: '#aaa',
// 				maxTicksLimit: 3,
// 				includeBounds: true,
// 				precision: 4
// 			},
// 			grid: {
// 				color: '#fff',
// 				borderColor: '#fff'
// 			}
// 		}
// 	}
// };

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
	() => [selectedParamOne.value, selectedParamTwo.value, timestep.value, selectedTrajState.value],
	async () => {
		renderGraph();
	}
);
</script>

<style scoped>
.container {
	display: flex;
	flex-direction: column;
	gap: 1rem;
}

.section-row {
	display: flex;
	/* flex-direction: column; */
	padding: 0.5rem 0rem;
	align-items: center;
	gap: 0.8125rem;
	align-self: stretch;
}
</style>
