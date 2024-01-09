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
import { Model } from '@/types/Types';
import { convertAMRToACSet } from '@/model-representation/petrinet/petrinet-service';
import { equationsToAMR } from '@/services/knowledge';
import { cleanLatexEquations } from '@/utils/math';
import { petriToLatex } from '@/petrinet/petrinet-service';
import { isEmpty } from 'lodash';
import { useToastService } from '@/services/toast';

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
	const updated = await equationsToAMR('latex', equations.value, 'petrinet', props.model.id);
	if (updated) {
		emit('model-updated');
		useToastService().success('Success', `Model Updated from equation`);
	}
	isUpdating.value = false;
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
