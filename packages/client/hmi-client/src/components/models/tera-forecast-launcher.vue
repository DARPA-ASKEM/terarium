<template>
	<tera-modal class="modal" @modal-mask-clicked="close()">
		<template #default>
			<!-- initial values -->
			<h4>Initial values</h4>
			<section>
				<div v-for="(s, i) of props.model.model.states" :key="i" class="row">
					<span>{{ s.name }}</span>
					<InputText class="p-inputtext-sm" v-model="initialValues[s.name]" />
				</div>
			</section>

			<!-- params -->
			<h4>Parameter values</h4>
			<section>
				<div v-for="(t, i) of props.model.model.transitions" :key="i" class="row">
					<span>{{ t.name }}</span>
					<InputText class="p-inputtext-sm" v-model="parameterValues[t.name]" />
				</div>
			</section>
		</template>
		<template #footer>
			<Button @click="launch">Launch</Button>
			<Button class="p-button-secondary" @click="close()">Cancel</Button>
		</template>
	</tera-modal>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { Model, Simulation, SimulationParams } from '@/types/Types';
import TeraModal from '@/components/widgets/tera-modal.vue';
import InputText from 'primevue/inputtext';
import Button from 'primevue/button';
import { makeForecast, createSimulation } from '@/services/models/simulation-service';
import { shimPetriModel } from '@/services/models/petri-shim';
import { StringValueMap, NumericValueMap } from '@/types/common';
import { AMRToPetri } from '@/model-representation/petrinet/petrinet-service';

const props = defineProps<{
	model: Model;
}>();

const initialValues = ref<StringValueMap>({});
const parameterValues = ref<StringValueMap>({});

props.model.model.states.forEach((s) => {
	initialValues.value[s.name] = `${1}`;
});
props.model.model.transitions.forEach((s) => {
	parameterValues.value[s.name] = `${0.5}`;
});

const emit = defineEmits(['close', 'launch-forecast']);

const launch = async () => {
	const initials: NumericValueMap = {};
	const params: NumericValueMap = {};

	Object.keys(initialValues.value).forEach((key) => {
		initials[key] = +initialValues.value[key];
	});

	Object.keys(parameterValues.value).forEach((key) => {
		params[key] = +parameterValues.value[key];
	});

	const payload: SimulationParams = {
		model: shimPetriModel(AMRToPetri(props.model)),
		initials,
		params,
		tspan: [0, 50]
	};

	const run = await makeForecast(payload);

	const simulation: Simulation = {
		name: 'New simulation',
		simulationParams: payload,
		result: run.id,
		modelId: props.model.id as string
	};
	await createSimulation(simulation);

	// FIXME: Cache into sessionStorage, should be DB
	const storage = window.sessionStorage;
	const key = `${props.model.id}`;
	const modelItemStr = storage.getItem(key);
	if (!modelItemStr) {
		const modelItem = [
			{
				id: run.id,
				input: payload
			}
		];
		storage.setItem(key, JSON.stringify(modelItem));
	} else {
		const modelItem: any[] = JSON.parse(modelItemStr);
		modelItem.push({
			id: run.id,
			input: payload
		});
		storage.setItem(key, JSON.stringify(modelItem));
	}
	emit('launch-forecast', run.id);
};

const close = () => {
	emit('close');
};
</script>

<style scoped>
.row {
	display: flex;
	flex-direction: row;
	justify-content: flex-end;
	padding-top: 2px;
	padding-bottom: 2px;
}

.p-inputtext.p-inputtext-sm {
	padding: 0.25rem 0.5rem;
	font-size: 1rem;
	margin-left: 1rem;
}
</style>
