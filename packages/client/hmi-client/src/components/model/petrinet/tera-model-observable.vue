<template>
	<tera-equation-container
		:is-editing="isEditing"
		:is-editable="isEditable"
		@cancel-edit="cancelEdit"
		@add-equation="addObservable"
		@start-editing="isEditing = true"
		@update-model-from-equation="updateObservables"
	>
		<template #math-editor>
			<tera-math-editor
				v-for="(ob, index) in editableObservables"
				:key="index"
				:index="index"
				:is-editable="isEditable"
				:latex-equation="ob.expression || ''"
				:id="ob.id"
				:name="ob.name"
				:is-editing-eq="isEditing"
				:show-metadata="true"
				@equation-updated="setNewObservables"
				@delete="deleteObservable"
				ref="observablesRefs"
		/></template>
	</tera-equation-container>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import TeraMathEditor from '@/components/mathml/tera-math-editor.vue';
import TeraEquationContainer from '@/components/model/petrinet/tera-equation-container.vue';
import { Model, Observable } from '@/types/Types';
import { extractVariablesFromMathML, EquationSide } from '@/utils/math';
import { cloneDeep, isEmpty } from 'lodash';

const props = defineProps<{
	model: Model;
	isEditable: boolean;
}>();

const emit = defineEmits(['update-model']);

const observablesRefs = ref<any[]>([]);
const editableObservables = ref<Observable[]>([]);
const isEditing = ref(false);

// const disableSaveObservable = computed(() => {
// 	const numEmptyObjservables = editableObservables.value.filter((ob) => ob.id === '').length;
// 	if (editableObservables.value.length > 0 && numEmptyObjservables === 0) {
// 		return false;
// 	}
// 	if (numEmptyObjservables > 0) {
// 		return true;
// 	}
// 	return false;
// });

const observables = computed(() => props.model?.semantics?.ode?.observables ?? []);

const setNewObservables = (index: number, latexEq: string, mathmlEq: string) => {
	const id = extractVariablesFromMathML(mathmlEq, EquationSide.Left).join('_');
	const obs: Observable = {
		id,
		name: id,
		states: [],
		expression: latexEq,
		expression_mathml: mathmlEq
	};
	editableObservables.value[index] = obs;
	updateModelFromObservables(editableObservables.value);
};

const deleteObservable = (index) => {
	editableObservables.value.splice(index, 1);
};

const addObservable = () => {
	isEditing.value = true;
	const obs: Observable = {
		id: '',
		name: '',
		states: [],
		expression: '',
		expression_mathml: ''
	};
	editableObservables.value.push(obs);
};

const cancelEdit = () => {
	isEditing.value = false;
	editableObservables.value = editableObservables.value.filter((ob) => ob.expression !== '');
	observablesRefs.value.forEach((ob) => {
		ob.isEditingEquation = false;
	});
};
function updateModelFromObservables(observableMathMLList) {
	const modelClone = cloneDeep(props.model);
	if (modelClone.semantics?.ode?.observables) {
		modelClone.semantics.ode.observables = observableMathMLList;
		emit('update-model', modelClone);
	}
}

const updateObservables = () => {
	isEditing.value = false;

	updateModelFromObservables(
		observablesRefs.value.map((ob) => ({
			id: ob.target.id,
			name: ob.target.name,
			expression: ob.target.mathLiveField.value,
			expression_mathml: ob.target.mathLiveField.getValue('math-ml'),
			states: extractVariablesFromMathML(ob.target.mathLiveField.getValue('math-ml'))
		}))
	);
	observablesRefs.value.forEach((ob) => {
		ob.isEditingEquation = false;
	});
};

watch(
	() => observables.value,
	() => {
		if (!isEmpty(observables)) {
			editableObservables.value = observables.value.filter((ob) => ob.expression);
		}
	},
	{ deep: true, immediate: true }
);
</script>
