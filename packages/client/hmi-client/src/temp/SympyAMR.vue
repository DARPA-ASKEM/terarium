<template>
	<section>
		<div class="row">
			<h4>SymPy Python code => AMR</h4>
			<div style="display: flex; flex-direction: row">
				<textarea ref="sympyCode"></textarea>
				<pre class="result" ref="resultAmr"></pre>
				<!--
				<div style="padding: 10px">
					<InputText size="large" style="width: 300px; margin-bottom: 5px" placeholder="project uuid" />
					<br />
					<Button @click="saveIntoProject()" label="Save into project" severity="warning"></Button>
				</div>
				-->
			</div>
			<Button @click="sympy2amr()" label="Run"></Button>
		</div>
	</section>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import Button from 'primevue/button';
// import InputText from 'primevue/inputtext';
import API from '@/api/api';

const sympyCode = ref<HTMLTextAreaElement | null>(null);
const resultAmr = ref<HTMLDivElement | null>(null);

const sympy2amr = async () => {
	const inputStr = sympyCode.value?.value || '';
	if (inputStr === '') return;

	if (resultAmr.value) {
		resultAmr.value.innerHTML = 'processing...';
	}

	const resp = await API.post('/knowledge/sympy-code-to-amr', { code: inputStr });
	const respData = resp.data;

	if (respData.error) {
		if (resultAmr.value) {
			resultAmr.value.style.color = 'red';
			resultAmr.value.innerHTML = '';
			resultAmr.value.innerHTML = respData.error;
		}
	} else {
		if (resultAmr.value) {
			resultAmr.value.style.color = '';
			resultAmr.value.innerHTML = '';
			resultAmr.value.innerHTML = JSON.stringify(respData.response.amr, null, 2);
		}
	}
};

// const saveIntoProject = async () => {};
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
	height: 400px;
	width: 500px;
	font-size: 90%;
}
.result {
	margin: 0;
	height: 400px;
	width: 500px;
	border: 1px solid #bbb;
	margin-left: 10px;
	overflow: scroll;
}
</style>
