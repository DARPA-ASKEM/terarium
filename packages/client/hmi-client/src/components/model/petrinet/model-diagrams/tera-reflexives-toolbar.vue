<template>
	<section>
		<div class="reflexives-row" v-for="(option, stateType) in reflexiveOptions" :key="stateType">
			<div v-for="(transition, i) in option" :key="i">
				<div>
					{{ `Which groups of type '${stateType}' are allowed to '${transition.id}'` }}
				</div>
				<MultiSelect
					class="p-inputtext-sm"
					placeholder="Select"
					:options="reflexiveNodeOptions[stateType]"
					optionLabel="id"
					:model-value="statesToAddReflexives[transition.id]"
					@update:model-value="
						(states) =>
							updateStatesToAddReflexives(
								{ states, typeOfTransition: transition, typeIdOfState: stateType as string },
								i
							)
					"
				/>
			</div>
		</div>
	</section>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import MultiSelect from 'primevue/multiselect';
import { Model, PetriNetTransition, Transition, TypeSystem, TypingSemantics } from '@/types/Types';
import {
	addReflexives,
	addTyping,
	updateRateExpression
} from '@/model-representation/petrinet/petrinet-service';
import { cloneDeep } from 'lodash';

const props = defineProps<{
	modelToUpdate: Model; // the model to which we will add reflexives
	modelToCompare: Model; // the model we are checking against to see if there are unassigned types
}>();

const emit = defineEmits(['model-updated']);

const modelToCompareTypeSystem = computed<TypeSystem | undefined>(
	() => props.modelToCompare.semantics?.typing?.system.model
);
const typedModel = ref<Model>(cloneDeep(props.modelToUpdate)); // this is the object that is being edited
const statesToAddReflexives = ref<{ [id: string]: { id: string; name: string }[] }>({});
const typeIdToTransitionIdMap = computed<{ [id: string]: string }>(() => {
	const map: { [id: string]: string } = {};
	props.modelToCompare?.semantics?.typing?.system.model.transitions.forEach((type) => {
		const transitionId =
			props.modelToCompare?.semantics?.typing?.map.find((m) => m[1] === type.id)?.[0] ?? '';
		map[type.id] = transitionId;
	});
	return map;
});
const reflexiveOptions = ref<{ [stateType: string]: Transition[] }>({});
const reflexiveNodeOptions = computed<{ [id: string]: { id: string; name: string }[] }>(() => {
	const options: { [id: string]: { id: string; name: string }[] } = {};
	Object.keys(reflexiveOptions.value).forEach((key) => {
		options[key] =
			props.modelToUpdate.semantics?.typing?.map
				.filter((m) => m[1] === key)
				.map((m) => ({ id: m[0], name: stateId2NameMap.value[m[0]] })) ?? [];
	});
	return options;
});
const stateId2NameMap = computed<{ [id: string]: string }>(() => {
	const map: { [id: string]: string } = {};
	props.modelToUpdate.model.states.forEach((state) => {
		map[state.id] = state.name;
	});
	return map;
});

const addedReflexivesRows: {
	states: {
		id: string;
		name: string;
	}[];
	typeOfTransition: Transition;
	typeIdOfState: string;
}[] = [];

/* Every time the user changes their selection of what states to add reflexives to, overwrite the previous changes with the current ones.
   Since there can be multiple MultiSelect components, the selections for all MultiSelect components are combined in 'addedReflexivesRows'.
   Iterate through 'addedReflexivesRows' and add reflexives according to the selections. 
*/
function updateStatesToAddReflexives(
	selection: {
		states: {
			id: string;
			name: string;
		}[]; // list of id+name of state to which to add reflexives
		typeOfTransition: Transition; // e.g. infect, recover
		typeIdOfState: string; // e.g. pop
	},
	index: number
) {
	typedModel.value = cloneDeep(props.modelToUpdate);
	addedReflexivesRows[index] = selection;
	addedReflexivesRows.forEach(({ states, typeOfTransition, typeIdOfState }) => {
		statesToAddReflexives.value[typeOfTransition.id] = states;
		const updatedTypeMap = typedModel.value.semantics?.typing?.map;
		const updatedTypeSystem = typedModel.value.semantics?.typing?.system;
		if (updatedTypeMap && updatedTypeSystem) {
			states.forEach((state) => {
				// For the type of reflexive transition that we are adding, get the number of inputs and outputs that share the same type as the state that we are updating
				// E.g. if we are adding an 'Infect' reflexive to a node of type 'Pop', get the number of 'Pop' inputs and outputs for 'Infect'
				const newTransitionId = `${typeIdToTransitionIdMap.value[typeOfTransition.id]}${state.id}`;

				// Assume for now that the number of inputs and outputs for a given type are always equal, though in general this may not be the case
				// TODO: implement logic for more generalized case where the above assumption is not true
				const numInputsOfStateType = typeOfTransition.input.filter(
					(i) => i === typeIdOfState
				).length;
				// const numOutputsOfStateType = typeOfTransition.input.filter(i => i === typeIdOfState).length;

				if (!typedModel.value.model.transitions.find((t) => t.id === newTransitionId)) {
					addReflexives(typedModel.value, state.id, newTransitionId, numInputsOfStateType);
				}
				const reflexive = typedModel.value.model.transitions.find((t) => t.id === newTransitionId);

				const transition = props.modelToCompare?.semantics?.typing?.system.model.transitions.find(
					(t) => t.id === typeOfTransition.id
				);
				if (transition) {
					updateRateExpression(typedModel.value, reflexive as PetriNetTransition, '');
					if (!updatedTypeMap.find((m) => m[0] === newTransitionId)) {
						updatedTypeMap.push([newTransitionId, typeOfTransition.id]);
					}
					if (!updatedTypeSystem.model.transitions.find((t) => t.id === typeOfTransition.id)) {
						updatedTypeSystem.model.transitions.push(transition);
					}
				}
			});
			const updatedTyping: TypingSemantics = {
				map: updatedTypeMap,
				system: updatedTypeSystem
			};
			addTyping(typedModel.value, updatedTyping);
		}
	});
	emit('model-updated', typedModel.value);
}

/* Compare the type systems of 'modelToUpdate' and 'modelToCompare' to determine what options the user has for adding reflexives.
   Allow the user to add transitions types that are present in 'modelToCompare' but not in 'modelToUpdate'.
   E.g. if 'modelToUpdate' has ['Pop','Infect'] transitions and 'modelToCompare' has ['Pop,'Infect','Recover'] transitions, 
   user should be prompted to add 'Recover' transitions to 'modelToUpdate'
*/
function populateReflexiveOptions() {
	if (modelToCompareTypeSystem.value) {
		let unassignedTransitions: Transition[];
		const modelToUpdateTransitionIds =
			props.modelToUpdate.semantics?.typing?.system.model.transitions.map((t) => t.id);
		const modelToCompareTypeTransitionIds = modelToCompareTypeSystem.value?.transitions.map(
			(t) => t.id
		);
		if (modelToUpdateTransitionIds && modelToCompareTypeTransitionIds) {
			const unassignedIds = modelToCompareTypeTransitionIds.filter(
				(id) => !modelToUpdateTransitionIds.includes(id)
			);
			// get the transition types that are in 'modelToCompare' but not 'modelToUpdate'
			unassignedTransitions = modelToCompareTypeSystem.value?.transitions.filter((t) =>
				unassignedIds.includes(t.id)
			);
		}
		props.modelToUpdate.model.states.forEach((state) => {
			// get type of state for each state in model to update model
			const type: string =
				props.modelToUpdate.semantics?.typing?.map.find((m) => m[0] === state.id)?.[1] ?? '';
			// for each unassigned transition type, check if inputs or ouputs have the type of this state
			// you should only be allowed to add a transition to a state, if the transition has inputs or outputs of the same type as the state
			const allowedTransitionsForState: Transition[] = unassignedTransitions.filter(
				(unassigned) => unassigned.input.includes(type) || unassigned.output.includes(type)
			);
			reflexiveOptions.value[type] = allowedTransitionsForState;
		});
	}
}

watch(
	[() => props.modelToCompare, () => props.modelToUpdate.semantics?.typing],
	() => {
		if (props.modelToUpdate.semantics?.typing) {
			populateReflexiveOptions();
		}
	},
	{ immediate: true }
);
</script>

<style scoped>
.reflexives-row {
	display: flex;
	flex-direction: column;
}

.reflexives-row > div {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.reflexives-row > div > div {
	margin: 1rem;
}

.p-multiselect {
	min-width: 150px;
}

:deep(.p-multiselect .p-multiselect-label.p-placeholder) {
	padding: 0.875rem 0.875rem;
}
</style>
