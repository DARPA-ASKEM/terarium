<template>
	<main v-if="isReady" style="margin: 2rem; display: flex; flex-direction: column">
		<p>The following variables are preset: x=1, y=2, z=3</p>
		<textarea style="width: 75rem; height: 10rem" v-model="exprString"> </textarea>
		<br />
		<div style="display: flex; flex-direction: row">
			<div style="padding: 1rem; font-size: 120%; width: 25rem; border: 1px solid #888">
				MathML: <br />
				{{ mathml }}
			</div>
			<div style="padding: 1rem; font-size: 120%; width: 25rem; border: 1px solid #888">
				Latex: <br />
				{{ latex }}
			</div>
			<div style="padding: 1rem; font-size: 120%; width: 25rem; border: 1px solid #888">
				Eval: <br />
				{{ evalResult }}
			</div>
		</div>
	</main>
	<main v-if="!isReady">
		<h4>Loading Python and modules ...</h4>
	</main>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { loadPyodide } from 'pyodide';

let python: any;
const mathml = ref('');
const latex = ref('');
const evalResult = ref<any>('');

const exprString = ref('');
const isReady = ref(false);

const variableMap: Object = { x: 1, y: 2, z: 3 };

async function main() {
	console.log('hello python world');
	const pyodide = await loadPyodide({
		indexURL: 'https://cdn.jsdelivr.net/pyodide/v0.23.4/full'
	});
	await pyodide.loadPackage('sympy');
	pyodide.runPython('import sympy');
	pyodide.runPython(
		'from sympy.parsing.sympy_parser import parse_expr, standard_transformations, implicit_multiplication_application, convert_xor'
	);
	pyodide.runPython('from sympy.printing.latex import latex');

	console.log(pyodide.runPython('1 + 2'));
	console.log(
		pyodide.runPython(`
		x, y, z = sympy.symbols('x y z')
		eq = sympy.factor(x**2 + 4 * x  + 4)
		sympy.print_mathml(eq)
	`)
	);

	// console.log(pyodide.runPython('symify("x + 3")'));
	isReady.value = true;
	return pyodide;
}

const runParser = (expr: string) => {
	if (!python) return;

	// function to convert expression to mathml
	let result = python.runPython(`
		eq = sympy.factor("${expr}")
		sympy.mathml(eq)
	`);
	mathml.value = result;

	result = python.runPython(`
		eq = sympy.factor("${expr}")
		latex(eq)
	`);
	latex.value = result;
};

const evaluateExpression = (expr: string) => {
	const subs: any[] = [];
	if (!python) return;

	Object.keys(variableMap).forEach((key) => {
		subs.push(`${key}: ${variableMap[key]}`);
	});

	// function to evaluate
	const result = python.runPython(`
		eq = sympy.factor("${expr}")
		eq.evalf(subs={${subs.join(', ')}})
	`);
	evalResult.value = result;
};

const test = async () => {
	python = await main();
	const xyz = python.runPython('99 + 1');
	console.log('xyz is', xyz);
};

test();

watch(
	() => exprString.value,
	() => {
		runParser(exprString.value);
		evaluateExpression(exprString.value);
	}
);
</script>
