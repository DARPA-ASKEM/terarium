<template>
	<section class="controls">
		<span v-if="props.isEditable" class="equation-edit-button">
			<Button
				v-if="isEditingEq"
				@click="cancelEditEquation"
				label="Cancel"
				class="p-button-sm p-button-outlined edit-button"
			/>
			<Button
				@click="toggleEditEquation"
				:label="isEditingEq ? 'Save equation' : 'Edit equation'"
				class="p-button-sm p-button-outlined edit-button"
			/>
		</span>
	</section>
	<section class="math-editor">
		<section v-if="showDevOptions" class="dev-options">
			<div style="align-self: center">[Math Renderer]</div>
			<div class="math-options">
				<label>
					<input type="radio" v-model="mathMode" :value="MathEditorModes.KATEX" />
					KaTeX
				</label>
				<label>
					<input type="radio" v-model="mathMode" :value="MathEditorModes.LIVE" />
					MathLIVE
				</label>
			</div>
		</section>
		<section v-if="mathMode === MathEditorModes.LIVE">
			<math-field
				class="mathlive-equation"
				ref="mathLiveField"
				virtual-keyboard-mode="false"
				:disabled="!isEditingEq"
				><slot v-if="mathMode === MathEditorModes.LIVE"></slot
			></math-field>
		</section>
		<section class="katex-math-container" v-else-if="mathMode === MathEditorModes.KATEX">
			<Textarea
				v-model="katexEquation"
				class="katex-equation"
				id="katex"
				type="text"
				rows="2"
				aria-label="katex"
				:disabled="!isEditingEq"
				autoResize
			/>
			<div ref="katexMathElement"></div>
		</section>
	</section>
</template>

<script setup lang="ts">
import { ref, watch, onUpdated, onMounted } from 'vue';
import { Mathfield, MathfieldElement } from 'mathlive';
import Textarea from 'primevue/textarea';
import { MathEditorModes } from '@/utils/math';
import Button from 'primevue/button';
import katex from 'katex';
import { logger } from '@/utils/logger';

// const mfe = ref(new MathfieldElement());

const mathLiveField = ref<Mathfield | null>(new MathfieldElement({ fontsDirectory: 'fonts/' }));
const katexEquation = ref<string>('');
const mathMode = ref<string | null>(null);
const katexMathElement = ref<HTMLElement | null>(null);

const emit = defineEmits(['equation-updated', 'validate-mathml', 'cancel-editing', 'set-editing']);

const KATEX_CONFIG = {
	displayMode: true,
	throwOnError: true,
	errorColor: 'blue',
	strict: 'warn',
	output: 'htmlAndMathml',
	trust: true
};

const props = defineProps({
	// LaTeX formula to be populated
	latexEquation: {
		type: String,
		required: true
	},
	// Math Renderer mode
	mathMode: {
		type: String as () => MathEditorModes,
		default: MathEditorModes.LIVE
	},
	// Show edit button
	isEditable: {
		type: Boolean,
		default: true
	},
	// Is the edit button activated?
	isEditingEq: {
		type: Boolean,
		default: true
	},
	// Show the renderer selection box
	showDevOptions: {
		type: Boolean,
		default: false
	},
	// check if the mathml is valid
	isMathMlValid: {
		type: Boolean,
		default: true
	}
});

onMounted(() => {
	if (props.mathMode) {
		mathMode.value = props.mathMode;
	}
});

onUpdated(() => {
	if (mathMode.value === MathEditorModes.LIVE) {
		mathLiveField.value?.setValue(props.latexEquation);
	} else if (mathMode.value === MathEditorModes.KATEX && katexMathElement.value) {
		katex.render(addTagToEquations(katexEquation.value, `\\notag`), katexMathElement.value, {
			displayMode: true,
			throwOnError: true,
			errorColor: 'blue',
			strict: 'red',
			trust: true
		});
	}
});

const renderEquations = () => {
	if (mathMode.value === MathEditorModes.LIVE) {
		mathLiveField.value?.setValue(props.latexEquation, { suppressChangeNotifications: true });
	} else if (mathMode.value === MathEditorModes.KATEX) {
		katexEquation.value = props.latexEquation;
	} else {
		logger.warn(`Invalid mathMode set : ${mathMode.value}`);
	}
};

const katexToMathML = (equation: string) =>
	katex.renderToString(equation, {
		displayMode: true,
		throwOnError: true,
		output: 'mathml'
	});

watch(katexEquation, (value) => {
	katex.render(addTagToEquations(value, `\\notag`), katexMathElement.value, KATEX_CONFIG);
});

watch(
	() => mathMode.value,
	(newValue, oldValue) => {
		if (newValue !== oldValue) {
			renderEquations();
		}
	}
);

/**
 * Adds a new 'tag' to the multiLineString
 */
function addTagToEquations(multiLineString: string, tag: string): string {
	const lines = multiLineString.split('\n');
	const outputLines = lines.map((line) => {
		if (!line.includes('begin') && !line.includes('end')) {
			return `${tag} ${line}`;
		}
		return line;
	});
	return outputLines.join('\n');
}

watch(
	() => props.latexEquation,
	(newValue) => {
		if (katexMathElement.value) {
			katex.render(addTagToEquations(newValue, `\\notag`), katexMathElement.value, KATEX_CONFIG);
		}
		renderEquations();
	}
);

/**
 * Toggles equation editing mode and emits "equation-updated" and "validate-mathml" events.
 */
const toggleEditEquation = () => {
	if (props.isEditingEq) {
		if (mathMode.value === MathEditorModes.LIVE) {
			emit('equation-updated', mathLiveField.value?.getValue('latex-unstyled'));
			emit('validate-mathml', mathLiveField.value?.getValue('math-ml'));
		} else if (mathMode.value === MathEditorModes.KATEX) {
			emit('equation-updated', katexEquation.value);
			emit('validate-mathml', katexToMathML(katexEquation.value));
		}
	} else if (!props.isEditingEq) {
		if (mathMode.value === MathEditorModes.LIVE) {
			emit('validate-mathml', mathLiveField.value?.getValue('math-ml'), true);
		} else {
			emit('validate-mathml', katexToMathML(katexEquation.value), true);
		}
	}
};

/**
 * Cancel the editing of an equation event
 */
const cancelEditEquation = () => {
	emit('cancel-editing', true);
};
</script>

<style scoped>
math-field {
	background-color: var(--gray-100);
	border-radius: 4px;
	border: none;
	outline: none;
	padding: 5px;
	margin: 10px;
	font-size: 1em;
	transition: background-color 0.3s ease-in-out, color 0.3s ease-in-out, opacity 0.3s ease-in-out;
}

math-field[disabled] {
	background-color: var(--gray-0);
	opacity: 1;
}

.controls {
	display: flex;
	flex-direction: row;
	margin: 10px 10px 0px 10px;
	justify-content: flex-end;
}

.mathlive-equation {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: center;
	width: 99%;
	margin: 5px;
}

.edit-button {
	margin-left: 5px;
	margin-right: 5px;
}

.dev-options {
	display: flex;
	flex-direction: column;
	align-self: center;
	width: 100%;
	font-size: 0.75em;
	font-family: monospace;
}

.math-options {
	display: flex;
	flex-direction: row;
	justify-self: center;
	justify-content: center;
}

.math-editor {
	display: flex;
	flex-grow: 1;
	flex-direction: column;
	resize: horizontal;
	justify-content: center;
}

.katex-equation {
	flex-direction: row;
	background-color: var(--gray-100);
	border-color: var(--gray-0);
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

.katex-math-container {
	display: flex;
	flex-direction: column;
	background-color: var(--gray-0);
}

.katex-math-container Textarea[disabled] {
	opacity: 1;
	background-color: var(--gray-0);
}
</style>
