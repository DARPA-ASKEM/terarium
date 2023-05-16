<template>
	<tera-modal class="modal" @modal-mask-clicked="close()">
		<template #default>
			<!-- initial values -->
			<h4>Initial values</h4>
			<section>
				<div v-for="(s, i) of props.model.content.S" :key="i" class="row">
					<span>{{ s.sname }}</span>
					<InputText class="p-inputtext-sm" v-model="initialValues[s.sname]" />
				</div>
			</section>

			<!-- params -->
			<h4>Parameter values</h4>
			<section>
				<div v-for="(t, i) of props.model.content.T" :key="i" class="row">
					<span>{{ t.tname }}</span>
					<InputText class="p-inputtext-sm" v-model="parameterValues[t.tname]" />
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
import { ITypedModel } from '@/types/Model';
import { PetriNet } from '@/petrinet/petrinet-service';
import TeraModal from '@/components/widgets/tera-modal.vue';
import InputText from 'primevue/inputtext';
import Button from 'primevue/button';
import { makeForecast, createSimulation } from '@/services/models/simulation-service';
import { Simulation, SimulationParams } from '@/types/Types';

interface StringValueMap {
	[key: string]: string;
}

interface NumericValueMap {
	[key: string]: number;
}

const props = defineProps<{
	model: ITypedModel<PetriNet>;
}>();

const initialValues = ref<StringValueMap>({});
const parameterValues = ref<StringValueMap>({});

props.model.content.S.forEach((s) => {
	initialValues.value[s.sname] = `${1}`;
});
props.model.content.T.forEach((s) => {
	parameterValues.value[s.tname] = `${0.5}`;
});

const emit = defineEmits(['close', 'launch-forecast']);

const launch = async () => {
	// FIXME: current need to strip out metadata, should do serverside
	const cleanedModel: PetriNet = {
		S: [],
		T: [],
		I: [],
		O: []
	};
	if (props.model) {
		cleanedModel.S = props.model.content.S.map((s) => ({ sname: s.sname }));
		cleanedModel.T = props.model.content.T.map((t) => ({ tname: t.tname }));
		cleanedModel.I = props.model.content.I;
		cleanedModel.O = props.model.content.O;
	}

	const initials: NumericValueMap = {};
	const params: NumericValueMap = {};

	Object.keys(initialValues.value).forEach((key) => {
		initials[key] = +initialValues.value[key];
	});

	Object.keys(parameterValues.value).forEach((key) => {
		params[key] = +parameterValues.value[key];
	});

	const payload: SimulationParams = {
		model: JSON.stringify(cleanedModel),
		initials,
		params,
		tspan: [0, 50]
	};

	const run = await makeForecast(payload);

	const simulation: Simulation = {
		name: 'New simulation',
		simulationParams: payload,
		result: run.id,
		modelId: props.model.id as number
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
