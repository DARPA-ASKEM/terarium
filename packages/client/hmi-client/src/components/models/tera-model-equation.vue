<template>
	<section class="controls">
		<span v-if="props.isEditable" class="equation-edit-button">
			<Button
				v-if="isEditing"
				@click="cancelEditEquations"
				label="Cancel"
				class="p-button-sm p-button-outlined edit-button"
				style="background-color: white"
			/>
			<Button
				@click="isEditing = true"
				:label="isEditing ? 'Update model' : 'Edit equation'"
				:class="isEditing ? 'p-button-sm' : 'p-button-sm p-button-outlined edit-button'"
			/>
		</span>
	</section>
	<section class="math-editor-container" :class="mathEditorSelected">
		<tera-math-editor
			v-for="(eq, index) in latexEquationList"
			:key="index"
			:index="index"
			:is-editable="isEditable"
			:latex-equation="eq"
			:is-editing-eq="isEditing"
			:is-math-ml-valid="isMathMLValid"
			@equation-updated="setNewEquation"
			@delete="deleteEquation"
			ref="equationsRef"
		/>
		<Button
			v-if="isEditing"
			class="p-button-sm add-equation-button"
			icon="pi pi-plus"
			label="Add Equation"
			@click="latexEquationList.push('')"
			text
		/>
	</section>
</template>

<script setup lang="ts">
import { computed, watch, ref } from 'vue';
import TeraMathEditor from '@/components/mathml/tera-math-editor.vue';
import Button from 'primevue/button';
import { mathmlToAMR } from '@/services/models/transformations';
import { isArray } from 'lodash';
import { logger } from '@/utils/logger';
import { separateEquations } from '@/utils/math';
import { Model } from '@/types/Types';

const props = defineProps<{
	model: Model | null;
	isEditable: boolean;
}>();

// References
const isEditing = ref<boolean>(false);
const equationsRef = ref<any[]>([]);
const latexEquationList = ref<string[]>([]);
const latexEquationsOriginalList = ref<string[]>([]);
const isMathMLValid = ref<boolean>(true);

// Utils
const setNewEquation = (index: number, latexEq: string) => {
	latexEquationList.value[index] = latexEq;
};

const deleteEquation = (index) => {
	latexEquationList.value.splice(index, 1);
};

const cancelEditEquations = () => {
	isEditing.value = false;
	latexEquationList.value = latexEquationsOriginalList.value.map((eq) => eq);
	equationsRef.value.forEach((eq) => {
		eq.isEditingEquation = false;
	});
};

const mathEditorSelected = computed(() => {
	if (!isMathMLValid.value) {
		return 'math-editor-error';
	}
	if (isEditing.value) {
		return 'math-editor-selected';
	}
	return '';
});

function joinStringLists(lists: string[][]): string[] {
	return ([] as string[]).concat(...lists);
}
const validateMathML = async (mathMLStringList: string[], editMode: boolean) => {
	isMathMLValid.value = false;
	const cleanedMathML = mathMLStringList;
	if (mathMLStringList.length === 0) {
		isMathMLValid.value = true;
	} else if (!editMode) {
		try {
			const amr = await mathmlToAMR(cleanedMathML, 'petrinet'); // This model is not compatible.
			const model = amr?.model;
			if (
				(model && isArray(model) && model.length > 0) ||
				(model && !isArray(model) && Object.keys(model).length > 0 && hasNoEmptyKeys(model))
			) {
				isMathMLValid.value = true;
				if (props.model !== null) updatePetriNet(props.model);
				// if (model !== null) updatePetriNet(model); -> doesn't work because model is NOT an AMR right now.
			} else {
				logger.error(
					'MathML cannot be converted to a Petrinet.  Please try again or click cancel.'
				);
			}
		} catch (e) {
			isMathMLValid.value = false;
		}
	} else if (editMode) {
		isMathMLValid.value = true;
	}
};

watch(
	() => [latexEquationList.value],
	() => {
		const mathMLEquations = equationsRef.value.map((eq) =>
			separateEquations(eq.mathLiveField.getValue('math-ml'))
		);
		validateMathML(joinStringLists(mathMLEquations), false);
	},
	{ deep: true }
);
</script>

<style scoped>
section math-editor {
	justify-content: center;
}

.math-editor-container {
	display: flex;
	position: absolute;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	min-height: 50vh;
	flex-direction: column;
	border: 4px solid transparent;
	border-radius: 0px var(--border-radius) var(--border-radius) 0px;
	overflow: auto;
	padding-top: 50px;
	padding-bottom: 20px;
}

.controls {
	display: flex;
	flex-direction: row;
	margin: 0.5rem 2.5rem 0px 10px;
	justify-content: flex-end;
	position: relative;
	z-index: 20;
}

.add-equation-button {
	width: 150px;
	min-height: 30px;
	margin-left: 5px;
	margin-top: 5px;
	border: none;
	outline: none;
}

.edit-button {
	margin-left: 5px;
	margin-right: 5px;
}
</style>
