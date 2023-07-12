<template>
	<section class="math-editor">
		<div :class="test2">
			<section class="menu" v-if="isEditingEquation">
				<div v-if="isEditingEquation" class="input-label">MathLive</div>
				<Button
					class="delete"
					icon="pi pi-trash"
					aria-label="Delete"
					@click="console.log('click')"
				></Button>
			</section>
			<math-field
				:class="mathFieldStyle"
				ref="mathLiveField"
				:disabled="!isEditingEq"
				@click="isEditingEq ? (isEditingEquation = true) : console.log(`abc`)"
				><slot>{{ props.latexEquation }}</slot></math-field
			>
			<div v-if="isEditingEquation" class="input-label">LaTeX</div>
			<section class="latex-input" v-if="isEditingEquation">
				<InputText
					v-model="latexTextInput"
					:class="latexTextInputStyle"
					id="latexInput"
					type="text"
					aria-label="latexInput"
					:unstyled="true"
					@keyup="updateEquation"
					@click="isEditingEq ? (isEditingEquation = true) : console.log(`abc`)"
				/>
			</section>
		</div>
	</section>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, computed } from 'vue';
import { Mathfield, MathfieldElement } from 'mathlive';
import InputText from 'primevue/inputtext';
import Button from 'primevue/button';
// import { logger } from '@/utils/logger';

const mathLiveField = ref<Mathfield | null>(new MathfieldElement({ fontsDirectory: 'fonts/' }));

const emit = defineEmits(['equation-updated']);

onMounted(() => {
	mathLiveField.value?.setOptions({ virtualKeyboardMode: 'onfocus' });
	latexTextInput.value = props.latexEquation;
});
const latexTextInput = ref<string>('');
const isEditingEquation = ref(false);
const latexTextInputStyle = computed(() => `latex-input-text`);

const mathFieldStyle = computed(() => {
	if (isEditingEquation.value) {
		return `mathlive-equation editeq`;
	}

	return props.isEditingEq ? `mathlive-equation editing` : `mathlive-equation`;
});

const updateEquation = () => {
	console.log(mathLiveField.value?.getValue('math-ml'));
	emit(
		'equation-updated',
		props.index,
		latexTextInput.value,
		mathLiveField.value?.getValue('math-ml')
	);
};

const test2 = computed(() => (isEditingEquation.value ? `this-class` : ``));

defineExpose({
	mathLiveField
});

const props = defineProps({
	// LaTeX formula to be populated
	latexEquation: {
		type: String,
		required: true
	},
	// Show edit button
	isEditable: {
		type: Boolean,
		default: true
	},
	// Is the edit button activated?
	isEditingEq: {
		type: Boolean,
		default: false
	},
	// check if the mathml is valid
	isMathMlValid: {
		type: Boolean,
		default: true
	},
	index: {
		type: Number,
		required: true
	}
});

const renderEquations = () => {
	mathLiveField.value?.setValue(props.latexEquation, { suppressChangeNotifications: true });
};

watch(
	() => props.latexEquation,
	() => {
		renderEquations();
	}
);

onMounted(() => {
	renderEquations();
});
</script>

<style scoped>
math-field {
	border-radius: 4px;
	border: none;
	outline: none;
	font-size: 1em;
	transition: background-color 0.3s ease-in-out, color 0.3s ease-in-out, opacity 0.3s ease-in-out;
}

math-field[disabled] {
	opacity: 1;
}

math-field:focus {
	border-color: var(--primary-color);
	box-shadow: inset 0 0 0 1px #1b8073, inset 0 0 0 1px #1b8073, inset 0 0 0 1px #1b8073,
		inset 0 0 0 1px var(--primary-color);
}

.mf {
	background-color: var(--gray-100);
}

.mathlive-equation {
	display: flex;
	flex-direction: row;
	padding: 10px;
	align-items: center;
	justify-content: center;
	width: 99%;
	margin: 5px;
	flex-grow: 1;
}

.math-editor {
	display: flex;
	flex-direction: column;
	resize: horizontal;
	justify-content: center;
}

.input-label {
	padding-left: 10px;
	padding-bottom: 5px;
	padding-top: 5px;
	font-family: var(--font-family);
}

.this-class {
	background-color: var(--gray-0);
	padding: 10px;
	margin: 10px;
	box-shadow: 0 3px 10px rgb(0 0 0 / 0.2);
	border: 1px solid var(--primary-color);
	border-radius: 3px;
}

.editing {
	background-color: var(--gray-100);
	cursor: pointer;
}

.editeq {
	border: 1px solid var(--gray-300);
}

.latex-input-text {
	flex-direction: row;
	background-color: var(--gray-0);
	border-color: var(--gray-300);
	padding: 5px;
	height: auto;
	resize: none;
	overflow-y: hidden;
	display: flex;
	align-items: center;
	justify-content: center;
	max-width: 100%;
	width: 99%;
	margin: 5px;
}

.menu {
	display: flex;
	align-items: baseline;
	justify-content: space-between;
	padding-right: 10px;
}

.delete {
	background-color: white;
	color: black;
	justify-content: flex-end;
	width: 50px;
}
</style>
