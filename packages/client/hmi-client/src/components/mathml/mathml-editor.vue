<template>
	<section class="math-editor">
		<math-field class="equation" @change="updateValue" ref="mf" virtual-keyboard-mode="false"
			><slot></slot
		></math-field>
	</section>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { Mathfield } from 'mathlive';
import Mathml2latex from 'mathml-to-latex';
import { logger } from '@/utils/logger';

const mf = ref<Mathfield | null>(null);
const formula = ref<string | undefined>('');
const selected = ref(false);

const emit = defineEmits(['formula-updated', 'mathml', 'latex']);

const props = defineProps({
	value: {
		type: String,
		required: true
	},
	highlightFormula: {
		type: Boolean,
		default: true
	},
	highlighStyle: {
		type: Object,
		default: () => ({
			color: 'white',
			backgroundColor: '#1B8073'
		})
	}
});

const updateValue = () => {
	formula.value = mf.value?.getValue('latex-unstyled');
	emit('formula-updated', formula.value);
	emit('mathml', mf.value?.getValue('math-ml'));
	// mf.value?.setValue(formula.value);
};

type Range = [start: number, end: number];

const highlight = (_range: Range = [0, -1]) => {
	mf.value?.applyStyle(
		{
			color: selected.value ? 'black' : 'white',
			backgroundColor: selected.value ? 'none' : '#1B8073'
		},
		{ range: _range }
	);
	selected.value = !selected.value;
};

const isMathML = (val: string): boolean => {
	const regex = /<math[>\s][\s\S]*?<\/math>/s;
	return regex.test(val);
};

onMounted(() => {
	mf.value?.applyStyle(
		{
			color: 'red',
			backgroundColor: '#1B8073'
		},
		{ range: [0, -1] }
	);
	if (isMathML(props.value)) {
		try {
			formula.value = Mathml2latex.convert(props.value);
		} catch (error) {
			logger.error(error, { showToast: false, silent: true });
		}
	} else {
		formula.value = props.value;
	}
	mf.value?.setValue(formula.value);
});

watch(
	() => props.value,
	(newValue) => {
		const currentValue = mf.value?.getValue();
		if (newValue !== currentValue) {
			formula.value = newValue;
			mf.value?.setValue(newValue, { suppressChangeNotifications: true });
		}
	}
);

watch(
	() => props.highlightFormula,
	() => {
		highlight();
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
	margin-left: 5px;
	margin-right: 5px;
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
	width: 90%;
	flex-grow: 1;
	flex-direction: column;
	resize: horizontal;
}
</style>
