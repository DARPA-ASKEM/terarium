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
import { PyodideInterface, loadPyodide } from 'pyodide';
import { PyProxy } from 'pyodide/ffi';

export interface PythonExpr {
	pyodide: PyodideInterface;
	substituteExpression: Function;
	removeExpression: Function;
	serializeExpression: Function;
	evaluateExpression: Function;
}

let pythonExpr: PythonExpr;
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

const main = async () => {
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

	// Utility function to return different serialization format
	pyodide.runPython(`
		def serialize_expr(expr):
				eq = sympy.S(expr, locals=_clash)
				return {
						"latex": latex(eq),
						"mathml": sympy.mathml(eq),
						"str": str(eq)
				}
	`);

	// See examples on MMT https://github.com/indralab/mira/blob/modeling_api/mira/modeling/askenet/ops.py
	const serializeExpression = (expressionStr: string) => {
		const result: PyProxy = pyodide.runPython(`
			serialize_expr("${expressionStr}")
		`);

		return {
			latex: result.get('latex'),
			mathml: result.get('mathml'),
			str: result.get('str')
		};
	};

	const substituteExpression = (expressionStr: string, newVar: string, oldVar: string) => {
		const result: PyProxy = pyodide.runPython(`
			eq = sympy.S("${expressionStr}", locals=_clash)
			new_eq = eq.replace(${oldVar}, ${newVar})
			serialize_expr(new_eq)
		`);
		return {
			latex: result.get('latex'),
			mathml: result.get('mathml'),
			str: result.get('str')
		};
	};

	const removeExpression = (expressionStr: string, v: string) => {
		const result: PyProxy = pyodide.runPython(`
			eq = sympy.S("${expressionStr}", locals=_clash)
			new_eq = sq.subs(${v}, 0)
			serialize_expr(new_eq)
		`);
		return {
			latex: result.get('latex'),
			mathml: result.get('mathml'),
			str: result.get('str')
		};
	};

	const evaluateExpression = (expressionStr: string, symbolsTable: Object) => {
		const subs: any[] = [];
		Object.keys(symbolsTable).forEach((key) => {
			subs.push(`${key}: ${symbolsTable[key]}`);
		});

		const result = pythonExpr.pyodide.runPython(`
			eq = sympy.S("${expressionStr}", locals=_clash)
			eq.evalf(subs={${subs.join(', ')}})
		`);
		return result;
	};

	return {
		pyodide,
		substituteExpression,
		removeExpression,
		serializeExpression,
		evaluateExpression
	};
};

const runParser = (expr: string) => {
	if (!pythonExpr) return;
	console.log(`evalulating .... [${expr}]`);
	if (!expr || expr.length === 0) {
		mathml.value = '';
		latex.value = '';
		freeSymbols.value = [];
		return;
	}

	// function to convert expression to mathml
	let result = pythonExpr.pyodide.runPython(`
		eq = sympy.S("${expr}", locals=_clash)
		sympy.mathml(eq)
	`);
	mathml.value = result;

	result = pythonExpr.pyodide.runPython(`
		eq = sympy.S("${expr}", locals=_clash)
		latex(eq)
	`);
	latex.value = result;

	result = pythonExpr.pyodide.runPython(`
		eq = sympy.S("${expr}", locals=_clash)
		list(eq.free_symbols)
	`);
	freeSymbols.value = result;
};

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

const start = async () => {
	startLoad.value = Date.now();
	pythonExpr = await main();
	endLoad.value = Date.now();

	// Bootstrap
	const keys = Object.keys(variableMap);
	pythonExpr.pyodide.runPython(`
		${keys.join(',')} = sympy.symbols('${keys.join(' ')}')
	`);
	isReady.value = true;
};

start();

watch(
	() => exprString.value,
	() => {
		runParser(exprString.value);
		if (pythonExpr) {
			const f = pythonExpr.evaluateExpression(exprString.value, variableMap);
			evalResult.value = f;
		}
	}
);
</script>
