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
		<section class="mathjax-container" v-if="props.mathmode === 'mathJAX'">
			<vue-mathjax :formula="`$$${formula}$$`"></vue-mathjax>
			<Textarea
				v-model="jaxFormula"
				class="mathjax"
				id="mathjax"
				type="text"
				@change="updateMathJaxValue"
				rows="2"
				aria-label="mathjax"
			/>
		</section>
	</section>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUpdated } from 'vue';
import { Mathfield } from 'mathlive';
import Mathml2latex from 'mathml-to-latex';
import { logger } from '@/utils/logger';
import Textarea from 'primevue/textarea';
import VueMathjax from 'vue-mathjax-next';

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

const updateMathJaxValue = () => {
	emit('formula-updated', jaxFormula.value);
};

// simple check to test if it's a mathml string
const isMathML = (val: string): boolean => {
	const regex = /<math[>\s][\s\S]*?<\/math>/s;
	return regex.test(val);
};

const setPropsFormula = () => {
	if (isMathML(props.value)) {
		try {
			formula.value = Mathml2latex.convert(props.value);
		} catch (error) {
			logger.error(error, { showToast: false, silent: true });
		}
	} else {
		formula.value = props.value;
	}
};

onUpdated(() => {
	setPropsFormula();
	if (props.mathmode === 'mathLIVE') {
		mf.value?.setValue(formula.value);
	}
});

onMounted(() => {
	setPropsFormula();
	if (props.mathmode === 'mathLIVE') {
		mf.value?.setValue(formula.value);
	}
});

watch(
	() => props.value,
	(newValue) => {
		formula.value = newValue;
		mf.value?.setValue(newValue, { suppressChangeNotifications: true });
	}
);

watch(
	() => formula.value,
	(newValue, oldValue) => {
		if (newValue !== oldValue) {
			setPropsFormula();
			if (props.mathmode === 'mathLIVE') {
				mf.value?.setValue(formula.value);
			}
			jaxFormula.value = formula.value;
		}
	}
);
</script>

<style scoped>
math-field {
	background-color: beige;
	border-radius: 4px;
	border: 1px solid rgba(0, 0, 0, 0.3);
	padding: 5px;
	min-height: 100%;
	padding: 5px;
	margin: 10px;
	font-size: 1.5em;
}

math-field:focus-within {
	outline: 1px solid #1b8073;
	border-radius: 4px;
	background: rgba(251, 187, 182, 0.1);
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
	flex-direction: column-reverse;
}
</style>
