<template>
	<tera-equation-container
		:is-editing="isEditing"
		:isUpdating="isUpdating"
		@cancel-edit="cancelEdit"
		@add-equation="addEquation"
		@start-editing="isEditing = true"
	>
		<template #math-editor>
			<div v-if="exceedEquationsThreshold == false">
				<tera-math-editor
					v-for="(eq, index) in equations"
					:key="index"
					:index="index"
					:is-editable="false"
					:is-editing-eq="isEditing"
					:latex-equation="eq"
					@equation-updated="setNewEquation"
					@delete="deleteEquation"
					ref="equationsRef"
				/>
			</div>
			<div v-else>
				<p>Number of equations exceed LaTex generator threshold.</p>
			</div>
		</template>
	</tera-equation-container>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import TeraMathEditor from '@/components/mathml/tera-math-editor.vue';
import TeraEquationContainer from '@/components/model/petrinet/tera-equation-container.vue';
import type { Model } from '@/types/Types';
import { cleanLatexEquations } from '@/utils/math';
import { isEmpty, isEqual } from 'lodash';
import { getModelEquation } from '@/services/model';

const props = defineProps<{
	model: Model;
}>();

const MAX_EQUATION_THRESHOLD = 100;

const equationsRef = ref<any[]>([]);
const equations = ref<string[]>([]);
const originalEquations = ref<string[]>([]);
const isEditing = ref(false);
const isUpdating = ref<boolean>(false);
const exceedEquationsThreshold = ref(false);

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

watch(
	() => props.model.semantics,
	async (newSemantics, oldSemantics) => {
		if (isEqual(newSemantics, oldSemantics)) return;

		// If there are too many states, don't bother generating the equations - Jan 2025
		let eqLength = 0;
		eqLength += props.model.model.states.length;
		if (props.model.semantics?.ode.observables) {
			eqLength += props.model.semantics.ode.observables.length;
		}
		if (eqLength > MAX_EQUATION_THRESHOLD) {
			exceedEquationsThreshold.value = true;
			return;
		}
		exceedEquationsThreshold.value = false;

		const latexFormula = await getModelEquation(props.model);
		if (latexFormula) {
			updateLatexFormula(cleanLatexEquations(latexFormula.split(' \\\\')));
		}
	},
	{ deep: true, immediate: true }
);
</script>
