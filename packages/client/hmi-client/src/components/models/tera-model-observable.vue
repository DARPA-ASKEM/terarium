<template>
	<section class="controls">
		<span v-if="props.isEditable" class="equation-edit-button">
			<Button
				v-if="isEditing"
				@click="cancelEditObservables"
				label="Cancel"
				class="p-button-sm p-button-outlined edit-button"
			/>
			<Button
				v-if="observervablesList.length !== 0"
				@click="updateObservables"
				:label="isEditing ? 'Update observable' : 'Edit observables'"
				:class="isEditing ? 'p-button-sm' : 'p-button-sm p-button-outlined edit-button'"
			/>
		</span>
	</section>
	<section class="observable-editor-container">
		<tera-math-editor
			v-for="(ob, index) in observervablesList"
			:key="index"
			:index="index"
			:is-editable="isEditable"
			:latex-equation="ob.expression || ''"
			:id="ob.id"
			:name="ob.name"
			:is-editing-eq="isEditing"
			@equation-updated="setNewObservables"
			@delete="deleteObservable"
			ref="observablesRefs"
		>
		</tera-math-editor>
		<Button
			v-if="observablesList.length === 0 || isEditing"
			class="p-button-sm add-equation-button"
			icon="pi pi-plus"
			label="Add Observable"
			@click="addObservable"
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
import { Model, Observable } from '@/types/Types';

const props = defineProps<{
	model: Model | null;
	isEditable: boolean;
}>();

// References
const isEditing = ref<boolean>(false);
const equationsRef = ref<any[]>([]);
const latexEquationList = ref<string[]>([]);
const isMathMLValid = ref<boolean>(true);
const observablesRefs = ref<any[]>([]);
const observervablesList = ref<Observable[]>([]);

// Utils
const addObservable = () => {
	isEditing.value = true;
	const obs: Observable = {
		id: '',
		name: '',
		states: [],
		expression: '',
		expression_mathml: ''
	};
	observervablesList.value.push(obs);
};

const updateObservables = () => {
	if (isEditingObservables.value) {
		isEditingObservables.value = false;
		// update
		emit(
			'update-model-observables',
			observablesRefs.value.map((eq, index) => {
				console.log(eq);
				return {
					id: eq.id || observablesRefs.value[index].id,
					name: eq.name || observablesRefs.value[index].name,
					expression: eq.mathLiveField.value,
					expression_mathml: eq.mathLiveField.getValue('math-ml'),
					states: extractVariablesFromMathML(eq.mathLiveField.getValue('math-ml'))
				};
			})
		);
		observablesRefs.value.forEach((eq) => {
			eq.isEditingEquation = false;
		});
	} else {
		isEditingObservables.value = true;
	}
};

const cancelEditObservables = () => {
	isEditing.value = false;
	observervablesList.value = observervablesList.value.filter((eq) => eq.expression !== '');
	observablesRefs.value.forEach((eq) => {
		eq.isEditingEquation = false;
	});
};

const deleteObservable = (index) => {
	observervablesList.value.splice(index, 1);
};

const setNewObservables = (
	index: number,
	latexEq: string,
	mathmlEq: string,
	name: string,
	id: string
) => {
	console.log(name);
	const obs: Observable = {
		id,
		name,
		states: [],
		expression: latexEq,
		expression_mathml: mathmlEq
	};
	observervablesList.value[index] = obs;
	emit('update-model-observables', observervablesList.value);
};

function extractVariablesFromMathML(mathML: string): string[] {
	const parser = new DOMParser();
	const xmlDoc = parser.parseFromString(mathML, 'text/xml');

	const variables: string[] = [];

	const miElements = xmlDoc.getElementsByTagName('mi');
	for (let i = 0; i < miElements.length; i++) {
		const miElement = miElements[i];
		const variable = miElement.textContent?.trim();
		if (variable) {
			variables.push(variable);
		}
	}

	return variables;
}

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

const observablesList = computed(() => props.model?.semantics?.ode?.observables ?? []);

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

.diagram-container {
	border: 1px solid var(--surface-border);
	border-radius: var(--border-radius);
}

.diagram-container-editing {
	box-shadow: inset 0 0 0 1px #1b8073, inset 0 0 0 1px #1b8073, inset 0 0 0 1px #1b8073,
		inset 0 0 0 1px var(--primary-color);
	border: 2px solid var(--primary-color);
	border-radius: var(--border-radius);
}

.math-editor-container {
	display: flex;
	position: absolute;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	flex-direction: column;
	border: 4px solid transparent;
	border-radius: 0px var(--border-radius) var(--border-radius) 0px;
	overflow: auto;
	padding-top: 50px;
	padding-bottom: 20px;
}

.observable-editor-container {
	display: flex;
	position: absolute;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
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
