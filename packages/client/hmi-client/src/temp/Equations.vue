<template>
	<section>
		<div class="row">
			<h4>LaTeX => Cleaned LaTeX</h4>
			<div style="display: flex; flex-direction: row">
				<textarea ref="latex1"></textarea>
				<pre class="result" ref="result1"></pre>
			</div>
			<Button @click="latex2latex()" label="Run" size="small"></Button>
		</div>
		<div class="row">
			<h4>Cleaned LaTeX => SymPy equation strings + AMR json</h4>
			<div style="display: flex; flex-direction: row">
				<textarea ref="latex2"></textarea>
				<pre class="result" ref="resultSympy"></pre>
				<pre class="result" ref="resultAmr"></pre>
			</div>
			<Button @click="latex2amr()" label="Run" size="small"></Button>
		</div>
	</section>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import Button from 'primevue/button';
import API from '@/api/api';

const latex1 = ref<HTMLTextAreaElement | null>(null);
const latex2 = ref<HTMLTextAreaElement | null>(null);
const result1 = ref<HTMLDivElement | null>(null);
const resultSympy = ref<HTMLDivElement | null>(null);
const resultAmr = ref<HTMLDivElement | null>(null);

const latex2latex = async () => {
	const inputStr = latex1.value?.value || '[]';
	const equations = JSON.parse(inputStr);

	if (result1.value) {
		result1.value.innerHTML = 'processing...';
	}

	const resp = await API.post('/knowledge/clean-equations', equations);
	const respData = resp.data;

	if (result1.value) {
		result1.value.innerHTML = '';
		result1.value.innerHTML = JSON.stringify(respData.cleanedEquations, null, 2);
	}
};

const latex2amr = async () => {
	const inputStr = latex2.value?.value || '[]';
	const equations = JSON.parse(inputStr);

	if (resultSympy.value) {
		resultSympy.value.innerHTML = 'processing...';
	}
	if (resultAmr.value) {
		resultAmr.value.innerHTML = 'processing...';
	}

	const resp = await API.post('/mira/latex-to-amr', equations);
	const respData = resp.data;

	if (resultSympy.value) {
		resultSympy.value.innerHTML = '';
		resultSympy.value.innerHTML = JSON.stringify(respData.response.sympyExprs, null, 2);
	}
	if (resultAmr.value) {
		resultAmr.value.innerHTML = '';
		resultAmr.value.innerHTML = JSON.stringify(respData.response.amr, null, 2);
	}
};
</script>

<style scoped>
section {
	display: flex;
	flex-direction: column;
	padding: 5px;
}
.row {
	padding-bottom: 15px;
}

textarea {
	height: 200px;
	width: 450px;
	font-size: 90%;
}
.result {
	margin: 0;
	height: 200px;
	width: 450px;
	border: 1px solid #bbb;
	margin-left: 10px;
	overflow: scroll;
}
</style>
