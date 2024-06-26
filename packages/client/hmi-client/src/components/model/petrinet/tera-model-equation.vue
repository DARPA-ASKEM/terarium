<template>
	<tera-equation-container
		:is-editing="isEditing"
		:is-editable="isEditable"
		:isUpdating="isUpdating"
		@cancel-edit="cancelEdit"
		@add-equation="addEquation"
		@start-editing="isEditing = true"
		@update-model-from-equation="updateModelFromEquations"
	>
		<template #math-editor>
			<tera-math-editor
				v-for="(eq, index) in equations"
				:key="index"
				:index="index"
				:is-editable="isEditable"
				:is-editing-eq="isEditing"
				:latex-equation="eq"
				@equation-updated="setNewEquation"
				@delete="deleteEquation"
				ref="equationsRef"
			/>
		</template>
	</tera-equation-container>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import TeraMathEditor from '@/components/mathml/tera-math-editor.vue';
import TeraEquationContainer from '@/components/model/petrinet/tera-equation-container.vue';
import type { Model } from '@/types/Types';
import { equationsToAMR } from '@/services/knowledge';
import { cleanLatexEquations } from '@/utils/math';
import { isEmpty } from 'lodash';
import { useToastService } from '@/services/toast';
import { getModelEquation } from '@/services/model';

const props = defineProps<{
	model: Model;
	isEditable: boolean;
}>();

const emit = defineEmits(['model-updated']);

const equationsRef = ref<any[]>([]);
const equations = ref<string[]>([]);
const originalEquations = ref<string[]>([]);
const isEditing = ref(false);
const isUpdating = ref<boolean>(false);

const setNewEquation = (index: number, latexEq: string) => {
	equations.value[index] = latexEq;
};

const deleteEquation = (index) => {
	equations.value.splice(index, 1);
};

const addEquation = () => {
	equations.value.push('');
};

const cancelEdit = () => {
	isEditing.value = false;
	equations.value = originalEquations.value.map((eq) => eq);
	equationsRef.value.forEach((eq) => {
		eq.isEditingEquation = false;
	});
};

const updateLatexFormula = (equationsList: string[]) => {
	equations.value = equationsList;
	if (isEmpty(originalEquations.value)) originalEquations.value = Array.from(equationsList);
};

const updateModelFromEquations = async () => {
	isUpdating.value = true;
	isEditing.value = false;
	const modelId = await equationsToAMR(equations.value, 'petrinet', props.model.id);
	if (modelId) {
		emit('model-updated');
		useToastService().success('Success', `Model Updated from equation`);
	}
	isUpdating.value = false;
};

watch(
	() => props.model,
	async () => {
		const latexFormula = await getModelEquation(props.model);
		if (latexFormula) {
			updateLatexFormula(cleanLatexEquations(latexFormula.split(' \\\\')));
		}
	},
	{ deep: true, immediate: true }
);
</script>
