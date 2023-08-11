<template>
	<main style="margin: 2rem; display: flex; flex-direction: column">
		<textarea style="width: 80rem; height: 10rem" v-model="exprString"> </textarea>
		<br />
		{{ mathml }}
	</main>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { loadPyodide } from 'pyodide';

let python: any;
const mathml = ref('');
const exprString = ref('');

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

	console.log(pyodide.runPython('1 + 2'));
	console.log(
		pyodide.runPython(`
		x, y, z = sympy.symbols('x y z')
		eq = sympy.factor(x**2 + 4 * x  + 4)
		sympy.print_mathml(eq)
	`)
	);

	// console.log(pyodide.runPython('symify("x + 3")'));
	return pyodide;
}

const runParser = (expr: string) => {
	if (!python) return;
	console.log('in parser');
	const result = python.runPython(`
		eq = sympy.factor("${expr}")
		sympy.mathml(eq)
	`);
	console.log('!!!!', result);
	mathml.value = result;
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
	}
);
</script>
