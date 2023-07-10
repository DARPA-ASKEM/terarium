<template>
	<tera-modal @modal-mask-clicked="close()">
		<table v-if="amrCopy">
			<!--
			<tr>
				<td> id </td>
				<td> name </td>
			</tr>
			<tr v-for="(state, index) of amrCopy.model.states" :key="index">
				<td>{{ state.id }}</td>
				<td> <input v-model="state.name" type="text" /> </td>
			</tr>
			<tr v-for="(transition, index) of amrCopy.model.transitions" :key="index">
				<td>{{ transition.id }}</td>
				<td> <input v-model="transition.properties.name" type="text" /> </td>
			</tr>
			-->
			<tr v-for="(stateEdit, index) of stateEdits" :key="index">
				<td>{{ stateEdit.state.id }}</td>
				<td>{{ stateEdit.state.name }}</td>
				<td>{{ stateEdit.initial.expression_mathml }}</td>
				<td>{{ stateEdit.param.id }}</td>
				<td><input v-model.number="stateEdit.param.value" /></td>
			</tr>
			<hr />
			<tr v-for="(transitionEdit, index) of transitionEdits" :key="index">
				<td>{{ transitionEdit.transition.id }}</td>
				<td>{{ transitionEdit.transition.properties.name }}</td>
				<td>{{ transitionEdit.rate.expression_mathml }}</td>
				<td>{{ transitionEdit.param.id }}</td>
				<td><input v-model.number="transitionEdit.param.value" /></td>
			</tr>
		</table>
		<Button label="Save model" @click="save(amrCopy)" />
	</tera-modal>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { ref, onMounted } from 'vue';
import {
	Model,
	PetriNetState,
	PetriNetTransition,
	ModelParameter,
	Initial,
	Rate
} from '@/types/Types';
import TeraModal from '@/components/widgets/tera-modal.vue';
import Button from 'primevue/button';
import { updateRateExpression } from '@/model-representation/petrinet/petrinet-service';

interface StateEdit {
	state: PetriNetState;
	param: ModelParameter;
	initial: Initial;
}

interface TransitionEdit {
	transition: PetriNetTransition;
	param: ModelParameter;
	rate: Rate;
}

const props = defineProps<{
	amr: Model;
}>();

const emit = defineEmits(['close', 'save']);

const amrCopy = ref<Model | null>(null);
const stateEdits = ref<StateEdit[]>([]);
const transitionEdits = ref<TransitionEdit[]>([]);

const close = () => emit('close');
const save = (amr: Model) => emit('save', amr);

onMounted(() => {
	const copy = _.cloneDeep(props.amr);

	// Test
	copy.model.transitions.forEach((t) => {
		updateRateExpression(copy, t);
	});

	amrCopy.value = copy;

	const noParams = amrCopy.value.semantics?.ode?.parameters?.filter((d) => Number.isNaN(d.value));

	console.log('No params', noParams);

	noParams.forEach((param: ModelParameter) => {
		const ode = amrCopy.value?.semantics?.ode;
		const model = amrCopy.value?.model;

		const paramId = param.id;
		const initial = ode?.initials?.find((d) => d.expression.includes(paramId));
		if (initial) {
			const state = model?.states.find((d) => d.id === initial.target);
			if (state) {
				console.log('Need to edit state', state);
				stateEdits.value.push({
					state,
					param,
					initial
				});
			}
		}
		const rate = ode?.rates.find((d) => d.expression.includes(paramId));
		if (rate) {
			const transition = model?.transitions.find((d) => d.id === rate.target);
			if (transition) {
				console.log('Need to edit transition', transition);
				transitionEdits.value.push({
					transition,
					param,
					rate
				});
			}
		}
	});
});
</script>
