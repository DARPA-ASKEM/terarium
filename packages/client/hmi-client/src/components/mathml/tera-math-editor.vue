<template>
	<section class="controls">
		<span v-if="props.isEditable" class="equation-edit-button">
			<Button
				v-if="isEditingEQ"
				@click="cancelEditEquation"
				label="Cancel"
				class="p-button-sm p-button-outlined edit-button"
			/>
			<Button
				@click="toggleEditEquationMode"
				:label="isEditingEQ ? 'Save equation' : 'Edit equation'"
				class="p-button-sm p-button-outlined edit-button"
			/>
		</span>
	</section>
	<section class="math-editor">
		<section v-if="props.showDevOptions" class="dev-options">
			<div style="align-self: center">[Math Renderer]</div>
			<div class="math-options">
				<label>
					<input type="radio" v-model="mathMode" :value="MathEditorModes.JAX" />
					MathJAX
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
				:disabled="!isEditingEQ"
				><slot v-if="props.mathMode === MathEditorModes.LIVE"></slot
			></math-field>
		</section>
		<section class="mathjax-container" v-else-if="mathMode === MathEditorModes.JAX">
			<Textarea
				v-model="jaxEquation"
				class="mathjax-equation"
				id="mathjax"
				type="text"
				rows="2"
				aria-label="mathjax"
				:disabled="!isEditingEQ"
				autoResize
			/>
			<div><vue-mathjax :formula="jaxEquationString" /></div>
		</section>
	</section>
</template>

<script setup lang="ts">
import { ref, watch, onUpdated, computed, onMounted } from 'vue';
import { Mathfield } from 'mathlive';
import Mathml2latex from 'mathml-to-latex';
import { logger } from '@/utils/logger';
import Textarea from 'primevue/textarea';
import { isMathML, MathEditorModes } from '@/utils/math';
import Button from 'primevue/button';

const mathLiveField = ref<Mathfield | null>(null);
const latexEquation = ref<string | undefined>('');
const jaxEquation = ref<string | undefined>('');
const isEditingEQ = ref<boolean>(false);
const mathMode = ref<string | null>(null);

const jaxEquationString = computed((): string =>
	latexEquation.value ? `$$${latexEquation.value}$$` : ''
);

const emit = defineEmits(['equation-updated', 'mathml-updated']);

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
	// Show the renderer selection box
	showDevOptions: {
		type: Boolean,
		default: false
	}
});

const updateMathLiveValue = () => {
	latexEquation.value = mathLiveField.value?.getValue('latex-unstyled');

	emit('equation-updated', latexEquation.value);
	if (!latexEquation.value?.includes(`color`)) {
		emit('mathml-updated', mathLiveField.value?.getValue('math-ml'));
	}
};

const getLaTeX = (equationString: string): string => {
	if (isMathML(equationString)) {
		try {
			return Mathml2latex.convert(equationString);
		} catch (error) {
			logger.error(error, { showToast: false, silent: true });
			return '';
		}
	}

	return equationString;
};

const updateMathJaxValue = () => {
	emit('equation-updated', jaxEquation.value);
};

onMounted(() => {
	if (props.mathMode) {
		mathMode.value = props.mathMode;
	}
});

onUpdated(() => {
	if (mathMode.value === MathEditorModes.LIVE) {
		mathLiveField.value?.setValue(latexEquation.value);
		if (!latexEquation.value?.includes(`color`)) {
			emit('mathml-updated', mathLiveField.value?.getValue('math-ml'));
		}
	}
});

const updateEquation = () => {
	if (mathMode.value === MathEditorModes.LIVE) {
		mathLiveField.value?.setValue(latexEquation.value, { suppressChangeNotifications: true });
	} else if (mathMode.value === 'mathJAX') {
		jaxEquation.value = latexEquation.value;
	}
};

watch(
	() => mathMode.value,
	(newValue, oldValue) => {
		if (newValue !== oldValue) {
			updateEquation();
		}
	}
);

watch(
	() => props.latexEquation,
	(newValue, oldValue) => {
		if (newValue !== oldValue) {
			latexEquation.value = getLaTeX(newValue);
			updateEquation();
		}
	}
);

const toggleEditEquationMode = async () => {
	isEditingEQ.value = !isEditingEQ.value;
	if (!isEditingEQ.value) {
		if (mathMode.value === MathEditorModes.JAX) {
			updateMathJaxValue();
		} else if (mathMode.value === MathEditorModes.LIVE) {
			updateMathLiveValue();
		}
	}
};

const cancelEditEquation = async () => {
	isEditingEQ.value = false;
};
</script>

<style scoped>
math-field {
	background-color: var(--editing-color);
	border: 1px solid var(--gray-1000);
	border-radius: 4px;
	padding: 5px;
	padding: 5px;
	margin: 10px;
	font-size: 1em;
	transition: background-color 0.3s ease-in-out, color 0.3s ease-in-out, opacity 0.3s ease-in-out;
}

math-field:focus-within {
	outline: 1px solid var(--primary-color-light);
	border-radius: 4px;
}

math-field[disabled] {
	opacity: 1;
	background-color: var(--surface-secondary);
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
	max-width: 100%;
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

.mathjax-equation {
	background-color: var(--editing-color);
	border: 1px solid var(--gray-1000);
	border-radius: 4px;
	padding: 5px;
	padding: 5px;
	margin: 10px;
	height: auto;
	resize: none;
	overflow-y: hidden;
}

.mathjax-container {
	display: flex;
	flex-direction: column;
}

.mathjax-container Textarea[disabled] {
	opacity: 1;
	background-color: var(--surface-secondary);
}
</style>
