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
					:model-value="statesToAddReflexives[stateType]"
					@update:model-value="newValue => updateStatesToAddReflexives(newValue, stateType as string, transition.id)"
				/>
			</div>
		</div>
	</section>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import MultiSelect from 'primevue/multiselect';
import { Model, Transition, TypeSystem, TypingSemantics } from '@/types/Types';
import { addReflexives, addTyping } from '@/model-representation/petrinet/petrinet-service';

const props = defineProps<{
	strataModel: Model;
	baseModel: Model;
}>();

const emit = defineEmits(['model-updated']);

const baseModelTypeSystem = computed<TypeSystem | undefined>(
	() => props.baseModel.semantics?.typing?.type_system
);
const typedModel = ref<Model>(props.strataModel); // this is the object that is being edited
const excludedTransitionTypes: Transition[] = [];
const statesToAddReflexives = ref<{ [id: string]: string[] }>({});
const typeIdToTransitionIdMap = computed<{ [id: string]: string }>(() => {
	const map: { [id: string]: string } = {};
	props.baseModel?.semantics?.typing?.type_system.transitions.forEach((type) => {
		const transitionId =
			props.baseModel?.semantics?.typing?.type_map.find((m) => m[1] === type.id)?.[0] ?? '';
		map[type.id] = transitionId;
	});
	return map;
});
const reflexiveOptions = ref<{ [stateType: string]: Transition[] }>({});
const reflexiveNodeOptions = computed<{ [id: string]: string[] }>(() => {
	const options: { [id: string]: string[] } = {};
	Object.keys(reflexiveOptions.value).forEach((key) => {
		options[key] =
			props.strataModel.semantics?.typing?.type_map.filter((m) => m[1] === key).map((m) => m[0]) ??
			[];
	});
	return options;
});

function updateStatesToAddReflexives(
	newValue: string[],
	typeOfState: string,
	typeOfTransition: string
) {
	statesToAddReflexives.value[typeOfState] = newValue;
	const updatedTypeMap = typedModel.value.semantics?.typing?.type_map;
	const updatedTypeSystem = typedModel.value.semantics?.typing?.type_system;

	if (updatedTypeMap && updatedTypeSystem) {
		newValue.forEach((stateId) => {
			const newTransitionId = `${typeIdToTransitionIdMap.value[typeOfTransition]}${stateId}${stateId}`;
			addReflexives(typedModel.value, stateId, newTransitionId);

			const transition = props.baseModel?.semantics?.typing?.type_system.transitions.find(
				(t) => t.id === typeOfTransition
			);
			if (transition) {
				if (!updatedTypeMap.find((m) => m[0] === newTransitionId)) {
					updatedTypeMap.push([newTransitionId, typeOfTransition]);
				}
				if (!updatedTypeSystem.transitions.find((t) => t.id === typeOfTransition)) {
					updatedTypeSystem.transitions.push(transition);
				}
			}
		});
		const updatedTyping: TypingSemantics = {
			type_map: updatedTypeMap,
			type_system: updatedTypeSystem
		};
		addTyping(typedModel.value, updatedTyping);
	}
	emit('model-updated', typedModel.value);
}

watch(
	[() => baseModelTypeSystem],
	() => {
		if (baseModelTypeSystem.value) {
			const strataTypeTransitionIds =
				props.strataModel.semantics?.typing?.type_system.transitions.map((t) => t.id);
			const baseModelTypeTransitionIds = baseModelTypeSystem.value?.transitions.map((t) => t.id);
			if (strataTypeTransitionIds && baseModelTypeTransitionIds) {
				const excludedIds = baseModelTypeTransitionIds.filter(
					(id) => !strataTypeTransitionIds.includes(id)
				);
				const excludedTransitions: Transition[] = baseModelTypeSystem.value?.transitions.filter(
					(t) => excludedIds.includes(t.id)
				);
				excludedTransitionTypes.push(excludedTransitions[0]);
			}

			props.strataModel.model.states.forEach((state) => {
				// get type of state for each state in strata model
				const type: string =
					props.strataModel.semantics?.typing?.type_map.find((m) => m[0] === state.id)?.[1] ?? '';
				// for each excluded transition type, check if inputs or ouputs have the type of this state
				const allowedTransitionsForState: Transition[] = excludedTransitionTypes.filter(
					(excluded) => excluded.input.includes(type) || excluded.output.includes(type)
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
