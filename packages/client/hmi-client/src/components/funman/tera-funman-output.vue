<template>
	<div class="section-row">
		<label>Trajectory State</label>
		<Dropdown v-model="selectedTrajState" :options="modelStates"> </Dropdown>
	</div>
	<Chart type="scatter" :data="trajData" :options="CHART_OPTIONS" />
	<h4>Configuration parameters <i class="pi pi-info-circle" /></h4>
	<p class="secondary-text">
		Adjust parameter ranges to only include values in the green region or less.
	</p>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { getQueries, processFunman, FunmanProcessedData } from '@/services/models/funman-service';
import Dropdown from 'primevue/dropdown';
import Chart from 'primevue/chart';

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
const trajData = ref({});

const CATEGORYPERCENTAGE = 0.9;
const BARPERCENTAGE = 0.6;
const MINBARLENGTH = 1;

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
	const processedData = processFunman(funModel);
	trajData.value = setFumanTrajectories(processedData);
};

// X = traj[i].timestep
// Y = traj[i].(selectedTrajState)
const setFumanTrajectories = (processedData: FunmanProcessedData) => {
	const documentStyle = getComputedStyle(document.documentElement);
	const weights = processedData.trajs.map((traj) => ({
		x: traj.timestep,
		y: traj[selectedTrajState.value]
	}));
	const labels = processedData.trajs.map((traj) => traj.pointId);
	console.log(weights);
	return {
		labels,
		datasets: [
			{
				backgroundColor: documentStyle.getPropertyValue('--text-color-secondary'),
				borderColor: '##CACBCC',
				borderWidth: 1,
				data: weights,
				categoryPercentage: CATEGORYPERCENTAGE,
				barPercentage: BARPERCENTAGE,
				minBarLength: MINBARLENGTH
			}
		]
	};
};

const CHART_OPTIONS = {
	devicePixelRatio: 4,
	maintainAspectRatio: false,
	pointStyle: false,
	animation: {
		duration: 0
	},
	showLine: true,
	plugins: {
		legend: {
			display: false
		}
	},
	scales: {
		x: {
			ticks: {
				color: '#aaa',
				maxTicksLimit: 5,
				includeBounds: true,
				// this rounds the tick label to nearest int
				callback: (num) => num
			},
			grid: {
				color: '#fff',
				borderColor: '#fff'
			}
		},
		y: {
			ticks: {
				color: '#aaa',
				maxTicksLimit: 3,
				includeBounds: true,
				precision: 4
			},
			grid: {
				color: '#fff',
				borderColor: '#fff'
			}
		}
	}
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

.p-chart {
	width: 100%;
	height: 200px;
	margin-top: 0.5em;
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
</style>
