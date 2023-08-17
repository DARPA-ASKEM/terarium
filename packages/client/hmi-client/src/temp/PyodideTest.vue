<template>
	<main v-if="isReady" style="margin: 2rem; display: flex; flex-direction: column">
		<h4>Python loaded in {{ ((endLoad - startLoad) / 1000).toFixed(2) }} seconds</h4>
		<section>
			The following variables are preset:
			<div v-for="(k) in Object.keys(variableMap) as string[]" :key="k">
				<div style="margin-left: 2rem">{{ k }} = {{ variableMap[k] }}</div>
			</div>
		</section>
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
			<div style="padding: 1rem; font-size: 120%; width: 25rem; border: 1px solid #888">
				Symbols: <br />
				{{ freeSymbols }}
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
const freeSymbols = ref([]);
const evalResult = ref<any>('');

const exprString = ref('');
const isReady = ref(false);
const startLoad = ref(0);
const endLoad = ref(0);

const variableMap: Object = {
	S: 1,
	I: 2,
	R: 'x + y',
	x: 100,
	y: 1000,
	N: 'S + I + R'
};

async function main() {
	startLoad.value = Date.now();
	const pyodide = await loadPyodide({
		indexURL: 'https://cdn.jsdelivr.net/pyodide/v0.23.4/full'
	});
	await pyodide.loadPackage('sympy');
	pyodide.runPython('import sympy');
	pyodide.runPython(
		'from sympy.parsing.sympy_parser import parse_expr, standard_transformations, implicit_multiplication_application, convert_xor'
	);
	pyodide.runPython('from sympy.printing.latex import latex');
	pyodide.runPython('from sympy.abc import _clash1, _clash2, _clash');
	pyodide.runPython('from sympy import S');

	// Utility function to resolve nested subsitutions - not used
	pyodide.runPython(`
		def recursive_sub(expr, replace):
				for _ in range(0, len(replace) + 1):
						new_expr = expr.subs(replace)
						if new_expr == expr:
								return new_expr, True
						else:
								expr = new_expr
				return new_expr, False
	`);

	// Bootstrap
	const keys = Object.keys(variableMap);
	pyodide.runPython(`
		${keys.join(',')} = sympy.symbols('${keys.join(' ')}')
	`);

	endLoad.value = Date.now();
	isReady.value = true;
	return pyodide;
}

const runParser = (expr: string) => {
	if (!python) return;
	console.log(`evalulating .... [${expr}]`);
	if (!expr || expr.length === 0) {
		mathml.value = '';
		latex.value = '';
		freeSymbols.value = [];
		return;
	}

	// function to convert expression to mathml
	let result = python.runPython(`
		eq = sympy.S("${expr}", locals=_clash)
		sympy.mathml(eq)
	`);
	mathml.value = result;

	result = python.runPython(`
		eq = sympy.S("${expr}", locals=_clash)
		latex(eq)
	`);
	latex.value = result;

	result = python.runPython(`
		eq = sympy.S("${expr}", locals=_clash)
		list(eq.free_symbols)
	`);
	freeSymbols.value = result;
};

const evaluateExpression = (expr: string) => {
	const subs: any[] = [];
	if (!python) return;

	Object.keys(variableMap).forEach((key) => {
		subs.push(`${key}: ${variableMap[key]}`);
	});

	// function to evaluate
	const result = python.runPython(`
		eq = sympy.S("${expr}", locals=_clash)
		eq.evalf(subs={${subs.join(', ')}})
	`);
	evalResult.value = result;
};

const start = async () => {
	python = await main();
};

start();

watch(
	() => exprString.value,
	() => {
		runParser(exprString.value);
		evaluateExpression(exprString.value);
	}
);
</script>
