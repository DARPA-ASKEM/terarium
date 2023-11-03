<template>
	<div class="section-row">
		<label>Select Parameter One</label>
		<Dropdown v-model="selectedParamOne" :options="parameterOptions"> </Dropdown>
	</div>
	<div class="section-row">
		<label>Select Parameter Two</label>
		<Dropdown v-model="selectedParamTwo" :options="parameterOptions"> </Dropdown>
	</div>
	<div class="container">
		<div ref="boxRef"></div>
		<div ref="trajRef"></div>
	</div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
// import funModel from '@/examples/fun-example.json';
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
const boxRef = ref();
const trajRef = ref();

// const width = 300;
// const height = 100;
const timestep = 7;
const boxId = 'box2';

const setParameters = async () => {
	parameterOptions.value = [];
	props.funModel.model.petrinet.semantics.ode.parameters.map((ele) =>
		parameterOptions.value.push(ele.id)
	);
	selectedParamOne.value = parameterOptions.value[0];
	selectedParamTwo.value = parameterOptions.value[0];
};

onMounted(() => {
	setParameters();
});

// watch (
// 	() => props.funModel, async() => {
// 			setParameters();
// 	}, { immediate: true }
// );

watch(
	() => [selectedParamOne.value, selectedParamTwo.value],
	async () => {
		const width = 800;
		const height = 250;
		console.log('Selected Param changed, update rengerer');
		const processedData = processFunman(props.funModel);
		renderFunmanBoundaryChart(
			boxRef.value,
			processedData,
			selectedParamOne.value,
			selectedParamTwo.value,
			timestep,
			{
				width,
				height
			}
		);
		renderFumanTrajectories(trajRef.value as HTMLElement, processedData, boxId, {
			width,
			height
		});
	},
	{ immediate: true }
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
