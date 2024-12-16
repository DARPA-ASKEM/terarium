<template>
	<section class="math-editor">
		<div :class="expandedDiv">
			<div v-if="isEditable && isEditingEquation" class="input-label">MathLive</div>
			<div
				class="eq"
				@mouseenter="hover = true"
				@mouseleave="hover = false"
				@focus="hover = true"
				@blur="hover = false"
			>
				<math-field
					:class="mathFieldStyle"
					ref="mathLiveField"
					:disabled="!isEditingEq"
					@click="isEditingEq ? (isEditingEquation = true) : null"
					@focus="showKeyboard"
					@blur="hideKeyboard"
					@keyup="updateEquationMathField"
				>
				</math-field>
				<section class="menu">
					<Button
						type="button"
						class="p-button-secondary p-button-sm p-button-outlined delete"
						label="Delete"
						@click="toggleMenu"
						aria-haspopup="true"
						aria-controls="overlay_menu"
						:style="hover && isEditingEq && !isEditingEquation ? `display: flex` : ``"
					/>
					<Menu ref="menu" id="overlay_menu" :model="menuItems" :popup="true" />
				</section>
			</div>
			<div v-if="isEditable && isEditingEquation">
				<div class="input-label spacer">LaTeX</div>
				<section class="latex-input" v-if="isEditingEquation">
					<InputText
						v-model="latexTextInput"
						class="latex-input-text"
						id="latexInput"
						type="text"
						aria-label="latexInput"
						:unstyled="true"
						@keyup="updateEquationLatex"
						@click="isEditingEq ? (isEditingEquation = true) : null"
					/>
				</section>
				<div class="controls">
					<span v-if="showMetadata">
						<span class="meta-property">Name:</span>
						<span class="meta-property-value" @dblclick="nameInput = false"
							><InputText
								v-model="name"
								class="control-button"
								:disabled="nameInput"
								@blur="nameInput = true"
							></InputText
						></span>
						<span class="meta-property">ID:</span>
						<span class="meta-property-value" @dblclick="idInput = false"
							><InputText v-model="id" class="control-button" :disabled="idInput" @blur="idInput = true"></InputText>
						</span>
					</span>
					<Button
						class="p-button-secondary p-button-sm p-button-outlined cancel-button"
						label="Cancel"
						aria-label="Cancel"
						@click="isEditingEquation = false"
					></Button>
					<Button
						class="p-button-sm"
						label="Save"
						aria-label="Save"
						:disabled="props.id === '' && showMetadata"
						@click="isEditingEquation = false"
					></Button>
				</div>
			</div>
		</div>
	</section>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, computed } from 'vue';
import { Mathfield, MathfieldElement } from 'mathlive';
import InputText from 'primevue/inputtext';
import Button from 'primevue/button';
import Menu from 'primevue/menu';
// import { logger } from '@/utils/logger';

const mathLiveField = ref<Mathfield | null>(new MathfieldElement({ fontsDirectory: 'fonts/' }));
const emit = defineEmits(['equation-updated', 'delete']);
const hover = ref(false);
const latexTextInput = ref<string>('');
const isEditingEquation = ref(false);

const menu = ref();
const nameInput = ref(true);
const idInput = ref(true);

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
		default: 0
	},
	id: {
		type: String,
		default: ''
	},
	name: {
		type: String,
		default: ''
	},
	showMetadata: {
		type: Boolean,
		default: false
	},
	keepOpen: {
		type: Boolean,
		default: false
	}
});

const id = ref(props.id);
const name = ref(props.name);
const expandedDiv = computed(() => (props.keepOpen || isEditingEquation.value ? `expanded-div` : ``));

defineExpose({
	mathLiveField,
	isEditingEquation,
	id: id.value,
	name: name.value
});

const menuItems = ref([
	{
		items: [
			{
				label: 'Cancel',
				command: () => {
					/* do nothing */
				}
			},
			{
				label: 'Confirm delete?',
				command: () => {
					emit('delete', props.index);
				}
			}
		]
	}
]);

const toggleMenu = (event) => {
	event.target.style = `display:flex`;
	menu.value.toggle(event);
};

const mathFieldStyle = computed(() => {
	if (isEditingEquation.value) {
		return `mathlive-equation editing2`;
	}

	return props.isEditingEq ? `mathlive-equation editing` : `mathlive-equation`;
});

const updateEquationMathField = () => {
	latexTextInput.value = mathLiveField.value?.getValue('latex-unstyled')
		? mathLiveField.value?.getValue('latex-unstyled')
		: '';
	updateEquationLatex();
};

const updateEquationLatex = () => {
	emit(
		'equation-updated',
		props.index,
		latexTextInput.value,
		mathLiveField.value?.getValue('math-ml'),
		name.value,
		id.value
	);
};

const renderEquations = () => {
	mathLiveField.value?.setValue(props.latexEquation, { suppressChangeNotifications: true });
};

const showKeyboard = () => {
	mathLiveField.value?.setOptions({ virtualKeyboardMode: 'manual' });
};

const hideKeyboard = () => {
	mathLiveField.value?.setOptions({ virtualKeyboardMode: 'off' });
};

watch(
	() => props.latexEquation,
	() => {
		if (props.latexEquation === '') {
			isEditingEquation.value = true;
		}
		name.value = props.name;
		id.value = props.id;
		renderEquations();
	}
);

onMounted(() => {
	if (props.latexEquation === '') {
		isEditingEquation.value = true;
	}
	latexTextInput.value = props.latexEquation;
	if (props.keepOpen) {
		isEditingEquation.value = true;
		hover.value = false;
	}
	renderEquations();
});
</script>

<style scoped>
.eq {
	position: relative;
	display: flex;
	flex-grow: 1;
}

math-field {
	border-radius: var(--border-radius);
	border: none;
	outline: none;
	font-size: 1em;
	width: 99%;
	flex-grow: 1;
	transition:
		background-color 0.3s ease-in-out,
		color 0.3s ease-in-out,
		opacity 0.3s ease-in-out;
}

math-field[disabled] {
	opacity: 1;
}

.mathlive-equation {
	flex-direction: row;
	padding: 10px;
	align-items: center;
	width: 99%;
	padding-left: 20px;
	flex-grow: 1;
}

.math-editor {
	display: flex;
	flex-direction: column;
}

.input-label {
	padding-left: 10px;
	padding-bottom: 5px;
	padding-top: 5px;
	font-family: var(--font-family);
}

.expanded-div {
	/* removed all this styling */
}

.editing {
	background-color: var(--gray-0);
	cursor: pointer;
}

.editing:hover {
	background-color: var(--surface-highlight);
	cursor: pointer;
}

.editeq {
	border: 1px solid var(--gray-200);
}

.spacer {
	padding-top: 1rem;
}
.latex-input-text {
	flex-direction: row;
	background-color: var(--gray-0);
	border-color: var(--gray-200);
	padding: 1rem;
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

.latex-input-text:focus {
	box-shadow: none !important;
	outline: none;
}

.menu {
	position: absolute;
	right: 20px;
	padding-right: 10px;
	top: 50%;
	transform: translateY(-50%);
	margin: auto 0;
}
.delete {
	color: black;
	background-color: var(--gray-0) !important;
	justify-content: flex-end;
	border: 1px solid var(--surface-border);
	display: none;
}

.delete:hover {
	border: 1px solid var(--surface-border-warning) !important;
}

.delete:hover:deep(.p-button-label) {
	color: var(--surface-border-warning);
}
.controls {
	display: flex;
	flex-direction: row;
	justify-content: flex-end;
	gap: 0.5rem;
}

.cancel-button:hover {
	border: 1px solid var(--surface-border);
}
.cancel-button:hover:deep(.p-button-label) {
	color: var(--primary-color);
}

.meta-data {
	padding-left: 10px;
	padding-bottom: 5px;
}

.meta-property {
	font-weight: bold;
	padding-right: 15px;
}

.meta-property-value {
	padding-right: 15px;
}
</style>
