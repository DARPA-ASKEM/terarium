<template>
	<section class="math-editor">
		<section class="mathlive" v-if="props.mathmode === 'mathLIVE'">
			<math-field
				class="equation"
				@change="updateMathLiveValue"
				ref="mf"
				virtual-keyboard-mode="false"
				><slot v-if="props.mathmode === 'mathLIVE'"></slot
			></math-field>
		</section>
		<section class="mathjax-container" v-else-if="props.mathmode === 'mathJAX'">
			<Textarea
				v-model="jaxFormula"
				class="mathjax"
				id="mathjax"
				type="text"
				rows="2"
				aria-label="mathjax"
				@blur="onBlur"
			/>
			<div><vue-mathjax :formula="`$$${formula}$$`" /></div>
		</section>
	</section>
</template>

<script setup lang="ts">
import { ref, watch, onUpdated } from 'vue';
import { Mathfield } from 'mathlive';
import Mathml2latex from 'mathml-to-latex';
import { logger } from '@/utils/logger';
import Textarea from 'primevue/textarea';
import { isMathML } from '@/utils/math';

const mf = ref<Mathfield | null>(null);
const formula = ref<string | undefined>('');
const jaxFormula = ref<string | undefined>('');

const emit = defineEmits(['formula-updated', 'mathml', 'latex']);

const props = defineProps({
	// Formula to be populated
	value: {
		type: String,
		required: true
	},
	// Math Renderer mode
	mathmode: {
		type: String,
		default: 'mathJAX'
	}
});

const updateMathLiveValue = () => {
	formula.value = mf.value?.getValue('latex-unstyled');
	emit('formula-updated', formula.value);
};

const getLaTeX = (formulaString: string): string => {
	if (isMathML(formulaString)) {
		try {
			formula.value = Mathml2latex.convert(formulaString);
		} catch (error) {
			logger.error(error, { showToast: false, silent: true });
			return 'Error';
		}
	}

	return formulaString;
};

const onBlur = () => {
	emit('formula-updated', jaxFormula.value);
};

onUpdated(() => {
	if (props.mathmode === 'mathLIVE') {
		mf.value?.setValue(formula.value);
	}
});

watch(
	() => props.mathmode,
	() => {
		if (props.mathmode === 'mathLive') {
			mf.value?.setValue(formula.value, { suppressChangeNotifications: true });
		} else if (props.mathmode === 'mathJAX') {
			jaxFormula.value = formula.value;
		}
	}
);

watch(
	() => props.value,
	(newValue, oldValue) => {
		if (newValue !== oldValue) {
			formula.value = getLaTeX(newValue);
		}
		if (props.mathmode === 'mathLive') {
			mf.value?.setValue(formula.value, { suppressChangeNotifications: true });
		} else if (props.mathmode === 'mathJAX') {
			jaxFormula.value = formula.value;
		}
	}
);
</script>

<style scoped>
math-field {
	background-color: beige;
	border-radius: 4px;
	border: 1px solid black;
	padding: 5px;
	min-height: 100%;
	padding: 5px;
	margin: 10px;
	font-size: 1.5em;
}

math-field:focus-within {
	outline: 1px solid var(--primary-color-light);
	border-radius: 4px;
}

.equation {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: center;
	max-width: 100%;
}

.math-editor {
	display: flex;
	flex-grow: 1;
	flex-direction: column;
	resize: horizontal;
}

.mathjax {
	padding: 5px;
	margin: 10px;
}

.mathjax-container {
	display: flex;
	flex-direction: column;
}
</style>
