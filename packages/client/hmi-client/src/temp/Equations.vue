<template>
	<section>
		<div class="row">
			<h4>
				<Button @click="latex2latex()" label="Run" size="small"></Button>
				LaTeX => Cleaned LaTeX
			</h4>
			<div style="display: flex; flex-direction: row">
				<textarea ref="latex1"></textarea>
				<div class="result" ref="result1"></div>
			</div>
		</div>
		<div class="row">
			<h4>
				<Button label="Run" size="small"></Button>
				LaTeX => SymPy String, AMR
			</h4>
			<div style="display: flex; flex-direction: row">
				<textarea></textarea>
				<div class="result"></div>
				<div class="result"></div>
			</div>
		</div>
	</section>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import Button from 'primevue/button';
import API from '@/api/api';

const latex1 = ref<HTMLTextAreaElement | null>(null);
const result1 = ref<HTMLDivElement | null>(null);

const latex2latex = async () => {
	const inputStr = latex1.value?.value || '[]';
	const equations = JSON.parse(inputStr);
	console.log('latex2latex', equations);

	const resp = await API.post('/knowledge/clean-equations', equations);
	const respData = resp.data;
	console.log('resp', respData);

	if (result1.value) {
		result1.value.innerHTML = '';
		result1.value.innerHTML = JSON.stringify(respData.cleanedEquations, null, 2);
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
	min-height: 150px;
	min-width: 450px;
	font-size: 90%;
}
.result {
	min-height: 150px;
	min-width: 400px;
	border: 1px solid #bbb;
	margin-left: 10px;
}
</style>
