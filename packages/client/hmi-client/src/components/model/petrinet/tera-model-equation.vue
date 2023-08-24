<template>
	<tera-equation-container
		:is-editing="isEditing"
		:is-editable="isEditable"
		@cancel-edit="cancelEdit"
		@add-equation="addEquation"
		@start-editing="isEditing = true"
		@update-model-from-equation="updateModelFromEquations"
	>
		<template #math-editor>
			<tera-math-editor
				v-for="(eq, index) in latexEquationList"
				:key="index"
				:index="index"
				:is-editable="isEditable"
				:is-editing-eq="isEditing"
				:latex-equation="eq"
				@equation-updated="setNewEquation"
				@delete="deleteEquation"
				ref="equationsRef"
		/></template>
	</tera-equation-container>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import TeraMathEditor from '@/components/mathml/tera-math-editor.vue';
import TeraEquationContainer from '@/components/model/petrinet/tera-equation-container.vue';
import { Model } from '@/types/Types';
import {
	convertAMRToACSet,
	updateExistingModelContent
} from '@/model-representation/petrinet/petrinet-service';
import { latexToAMR } from '@/services/models/extractions';
import { cleanLatexEquations } from '@/utils/math';
import { petriToLatex } from '@/petrinet/petrinet-service';
import { isEmpty } from 'lodash';

const props = defineProps<{
	model: Model;
	isEditable: boolean;
}>();

const emit = defineEmits(['update-diagram']);

const equationsRef = ref<any[]>([]);
const latexEquationList = ref<string[]>([]);
const latexEquationsOriginalList = ref<string[]>([]);
const isEditing = ref(false);

const setNewEquation = (index: number, latexEq: string) => {
	latexEquationList.value[index] = latexEq;
};

const deleteEquation = (index) => {
	latexEquationList.value.splice(index, 1);
};

const addEquation = () => {
	latexEquationList.value.push('');
};

const cancelEdit = () => {
	isEditing.value = false;
	latexEquationList.value = latexEquationsOriginalList.value.map((eq) => eq);
	equationsRef.value.forEach((eq) => {
		eq.isEditingEquation = false;
	});
};

const updateLatexFormula = (equationsList: string[]) => {
	latexEquationList.value = equationsList;
	if (isEmpty(latexEquationsOriginalList.value))
		latexEquationsOriginalList.value = equationsList.map((eq) => eq);
};

const updateModelFromEquations = async () => {
	const updatedModel = await latexToAMR(latexEquationList.value);
	if (updatedModel) {
		emit('update-diagram', updateExistingModelContent(updatedModel, props.model));
	}
};

watch(
	() => props.model,
	async () => {
		const latexFormula = await petriToLatex(convertAMRToACSet(props.model));
		if (latexFormula) {
			updateLatexFormula(cleanLatexEquations(latexFormula.split(' \\\\')));
		}
	},
	{ deep: true, immediate: true }
);
</script>
