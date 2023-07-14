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
					:model-value="statesToAddReflexives[transition.id]"
					@update:model-value="(newValue) => updateStatesToAddReflexives(newValue, transition.id)"
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

const props = defineProps<{
	modelToUpdate: Model; // the model to which we will add reflexives
	modelToCompare: Model; // the model we are checking against to see if there are unassigned types
}>();

const emit = defineEmits(['model-updated']);

const modelToCompareTypeSystem = computed<TypeSystem | undefined>(
	() => props.modelToCompare.semantics?.typing?.system
);
const typedModel = ref<Model>(props.modelToUpdate); // this is the object that is being edited
let unassignedTransitionTypes: Transition[] = [];
const statesToAddReflexives = ref<{ [id: string]: string[] }>({});
const typeIdToTransitionIdMap = computed<{ [id: string]: string }>(() => {
	const map: { [id: string]: string } = {};
	props.modelToCompare?.semantics?.typing?.system.transitions.forEach((type) => {
		const transitionId =
			props.modelToCompare?.semantics?.typing?.map.find((m) => m[1] === type.id)?.[0] ?? '';
		map[type.id] = transitionId;
	});
	return map;
});
const reflexiveOptions = ref<{ [stateType: string]: Transition[] }>({});
const reflexiveNodeOptions = computed<{ [id: string]: string[] }>(() => {
	const options: { [id: string]: string[] } = {};
	Object.keys(reflexiveOptions.value).forEach((key) => {
		options[key] =
			props.modelToUpdate.semantics?.typing?.map.filter((m) => m[1] === key).map((m) => m[0]) ?? [];
	});
	return options;
});

function updateStatesToAddReflexives(newValue: string[], typeOfTransition: string) {
	statesToAddReflexives.value[typeOfTransition] = newValue;
	const updatedTypeMap = typedModel.value.semantics?.typing?.map;
	const updatedTypeSystem = typedModel.value.semantics?.typing?.system;

	if (updatedTypeMap && updatedTypeSystem) {
		newValue.forEach((stateId) => {
			const newTransitionId = `${typeIdToTransitionIdMap.value[typeOfTransition]}${stateId}${stateId}`;
			addReflexives(typedModel.value, stateId, newTransitionId);

			const transition = props.modelToCompare?.semantics?.typing?.system.transitions.find(
				(t) => t.id === typeOfTransition
			);
			if (transition) {
				updateRateExpression(typedModel.value, transition as PetriNetTransition);
				if (!updatedTypeMap.find((m) => m[0] === newTransitionId)) {
					updatedTypeMap.push([newTransitionId, typeOfTransition]);
				}
				if (!updatedTypeSystem.transitions.find((t) => t.id === typeOfTransition)) {
					updatedTypeSystem.transitions.push(transition);
				}
			}
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
	[() => modelToCompareTypeSystem],
	() => {
		if (modelToCompareTypeSystem.value) {
			const modelToUpdateTransitionIds =
				props.modelToUpdate.semantics?.typing?.system.transitions.map((t) => t.id);
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
				// get type of state for each state in strata model
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

.p-multiselect .p-multiselect-label {
	padding: 0.875rem 0.875rem;
}
</style>
