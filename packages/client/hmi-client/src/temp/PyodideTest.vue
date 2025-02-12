<template>
	<main style="margin: 2rem; display: flex; flex-direction: column">
		<h4>Python playground</h4>
		<section>
			The following variables are preset:
			<div v-for="k in Object.keys(variableMap) as string[]" :key="k">
				<div style="margin-left: 2rem">{{ k }} = {{ variableMap[k] }}</div>
			</div>
		</section>
		<textarea style="width: 75rem; height: 10rem" v-model="exprString"> </textarea>
		<br />
		<div style="display: flex; flex-direction: row">
			<div style="padding: 1rem; font-size: 100%; width: 25rem; border: 1px solid #888">
				MathML: <br />
				{{ mathml }}
				<hr />
				{{ pmathml }}
			</div>
			<div style="padding: 1rem; font-size: 120%; width: 25rem; border: 1px solid #888">
				Latex: <br />
				{{ latex }}
			</div>
			<div style="padding: 1rem; font-size: 120%; width: 25rem; border: 1px solid #888">
				Eval: <br />
				{{ evalResult }}
			</div>
			<div style="padding: 1rem; font-size: 120%; width: 25rem; border: 1px solid #888">
				Symbols: <br />
				{{ freeSymbols }}
			</div>
		</div>
	</main>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { pythonInstance } from '@/web-workers/python/PyodideController';

const mathml = ref('');
const pmathml = ref('');
const latex = ref('');
const freeSymbols = ref([]);
const evalResult = ref<any>('');

const exprString = ref('');

/*
const renameParameterId = (amr: Model, newId: string, oldId: string) => {
	const ode = amr.semantics?.ode;
	if (!ode) return;

	ode.parameters?.forEach((param) => {
		if (param.id === oldId) param.id = newId;
	});

	if (ode.observables) {
		ode.observables.forEach((obs) => {
			const expression = obs.expression as string;
			const newExpression = pythonExpr.substituteExpression(expression, newId, oldId);
			obs.expression = newExpression.str;
			obs.expression_mathml = newExpression.mathml;
		});
	}

	if (ode.rates) {
		ode.rates.forEach((rate) => {
			const expression = rate.expression as string;
			const newExpression = pythonExpr.substituteExpression(expression, newId, oldId);
			rate.expression = newExpression.str;
			rate.expression_mathml = newExpression.mathml;
		});
	}
};
*/

const variableMap: Object = {
	S: 1,
	I: 2,
	R: 'x + y',
	x: 100,
	y: 1000,
	N: 'S + I + R'
};

const start = async () => {
	let result: any;

	// Expression parsing example
	result = await pythonInstance.parseExpression('a + b + c + 10');
	console.log('parse test', result);

	// Expression evaluation example
	result = await pythonInstance.evaluateExpression('a + b', { a: 11, b: 22 });
	console.log('eval test', result);

	// Python example
	result = await pythonInstance.runPython('list(map(lambda x: x ** 2, [1, 2, 3, 4, 5]))');
	console.log('code test', result);
};

start();

watch(
	() => exprString.value,
	async () => {
		const f = await pythonInstance.evaluateExpression(exprString.value, variableMap);
		evalResult.value = f;

		const result = (await pythonInstance.parseExpression(exprString.value)) as any;
		mathml.value = result.mathml;
		pmathml.value = result.pmathml;
		latex.value = result.latex;
		freeSymbols.value = result.freeSymbols;
	}
);
</script>
