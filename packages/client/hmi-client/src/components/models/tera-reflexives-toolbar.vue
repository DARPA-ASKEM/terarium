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
						(newValue) => updateStatesToAddReflexives(newValue, transition, stateType as string)
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
	updateRateExpression,
	removeTransition
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
let unassignedTransitionTypes: Transition[] = [];
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
// save a list of what reflexives we have added so that if the user undoes any of their selections, we can remove the transitions they de-selected
const addedReflexivesHistory: Set<string> = new Set();

function updateStatesToAddReflexives(
	newValue: {
		id: string;
		name: string;
	}[], // list of id+name of state to which to add reflexives
	typeOfTransition: Transition, // e.g. infect, recover
	typeIdOfState: string // e.g. pop
) {
	statesToAddReflexives.value[typeOfTransition.id] = newValue;
	const updatedTypeMap = typedModel.value.semantics?.typing?.map;
	const updatedTypeSystem = typedModel.value.semantics?.typing?.system;
	const reflexivesAddedForCurrentSelection: Set<string> = new Set();

	if (updatedTypeMap && updatedTypeSystem) {
		newValue.forEach((state) => {
			// For the type of reflexive transition that we are adding, get the number of inputs and outputs that share the same type as the state that we are updating
			// E.g. if we are adding an 'Infect' reflexive to a node of type 'Pop', get the number of 'Pop' inputs and outputs for 'Infect'
			const newTransitionId = `${typeIdToTransitionIdMap.value[typeOfTransition.id]}${state.id}`;

			// Assume for now that the number of inputs and outputs for a given type are always equal, though in general this may not be the case
			// TODO: implement logic for more generalized case where the above assumption is not true
			const numInputsOfStateType = typeOfTransition.input.filter((i) => i === typeIdOfState).length;
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

			// Every time the user updates their selection of which states to add reflexives to, we iterate through each selected state and add the reflexive.
			// If the user deselects any states, we don't know about it. As far as I'm aware this is a limitation of the PrimeVue MultiSelect component.
			// So we have to store a history of what reflexive transitions the user has previously added, store a list of what reflexive transitions the user is adding for the current selection
			// and compare them to remove any previously added transitions if they aren't supposed to be added based on the user selection.
			// E.g. if the user previously added reflexives to states S, I, and R, and the user's current selection is [S, I] then remove the transition added to R
			addedReflexivesHistory.add(reflexive.id);
			reflexivesAddedForCurrentSelection.add(reflexive.id);
		});
		const reflexiveIdsToRemove: string[] = [...addedReflexivesHistory].filter(
			(r) => !reflexivesAddedForCurrentSelection.has(r)
		);
		reflexiveIdsToRemove.forEach((r) => {
			removeTransition(typedModel.value, r);
		});
		const updatedTyping: TypingSemantics = {
			map: updatedTypeMap,
			system: updatedTypeSystem
		};
		addTyping(typedModel.value, updatedTyping);
	}
	emit('model-updated', typedModel.value);
}

watch(
	() => props.modelToUpdate,
	() => {
		if (props.modelToCompare) {
			// typedModel.value = props.modelToUpdate;
			// emit('model-updated', typedModel.value);
		}
	},
	{ immediate: true }
);

function populateReflexiveOptions() {
	if (modelToCompareTypeSystem.value) {
		const modelToUpdateTransitionIds =
			props.modelToUpdate.semantics?.typing?.system.model.transitions.map((t) => t.id);
		const modelToCompareTypeTransitionIds = modelToCompareTypeSystem.value?.transitions.map(
			(t) => t.id
		);
		if (modelToUpdateTransitionIds && modelToCompareTypeTransitionIds) {
			const unassignedIds = modelToCompareTypeTransitionIds.filter(
				(id) => !modelToUpdateTransitionIds.includes(id)
			);

			const unassignedTransitions: Transition[] =
				modelToCompareTypeSystem.value?.transitions.filter((t) => unassignedIds.includes(t.id));
			if (unassignedTransitions.length > 0) {
				unassignedTransitionTypes = unassignedTransitionTypes.concat(unassignedTransitions);
			}
		}
		props.modelToUpdate.model.states.forEach((state) => {
			// get type of state for each state in model to update model
			const type: string =
				props.modelToUpdate.semantics?.typing?.map.find((m) => m[0] === state.id)?.[1] ?? '';
			// for each unassigned transition type, check if inputs or ouputs have the type of this state
			const allowedTransitionsForState: Transition[] = unassignedTransitionTypes.filter(
				(unassigned) => unassigned.input.includes(type) || unassigned.output.includes(type)
			);
			if (!reflexiveOptions.value[type]) {
				reflexiveOptions.value[type] = allowedTransitionsForState;
			}
		});
	}
}

watch(
	[() => props.modelToCompare],
	() => {
		populateReflexiveOptions();
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
