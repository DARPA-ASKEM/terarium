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
	processFunman,
	renderFumanTrajectories,
	renderFunmanBoundaryChart
} from '@/services/funman';
import Dropdown from 'primevue/dropdown';

const props = defineProps<{
	funModel: any;
}>();

const parameterOptions = ref<string[]>([]);
const selectedParamOne = ref();
const selectedParamTwo = ref();
const timestepOptions = ref();
const timestep = ref();

const boxRef = ref();
const trajRef = ref();

const boxId = 'box2';

const setParameters = async () => {
	parameterOptions.value = [];
	props.funModel.model.petrinet.semantics.ode.parameters.map((ele) =>
		parameterOptions.value.push(ele.id)
	);
	selectedParamOne.value = parameterOptions.value[0];
	selectedParamTwo.value = parameterOptions.value[0];
	console.log(props.funModel.request.structure_parameters);
	timestepOptions.value = props.funModel.request.structure_parameters[0].schedules[0].timepoints;
	timestep.value = timestepOptions.value[1];
};

const renderGraph = async () => {
	const width = 800;
	const height = 250;
	console.log('Render Graph');
	const processedData = processFunman(props.funModel);
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
	renderFumanTrajectories(trajRef.value as HTMLElement, processedData, boxId, {
		width,
		height
	});
};

onMounted(() => {
	setParameters();
});

watch(
	// When props change reset params rerender graph
	() => props.funModel,
	async () => {
		setParameters();
	}
);

watch(
	// Whenever user changes options rerender.
	() => [selectedParamOne.value, selectedParamTwo.value, timestep.value],
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
