import { loadPyodide } from 'pyodide';
import { Actions } from './PyodideController';

const variableMap: Object = {
	S: 1,
	I: 2,
	R: 'x + y',
	x: 100,
	y: 1000,
	N: 'S + I + R'
};

const startLoad = Date.now();
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

pyodide.runPython(`
def serialize_expr(expr):
        eq = sympy.S(expr, locals=_clash)
        return {
                "latex": latex(eq),
                "mathml": sympy.mathml(eq),
                "str": str(eq)
        }
`);

// Bootstrap
const keys = Object.keys(variableMap);
pyodide.runPython(`
    ${keys.join(',')} = sympy.symbols('${keys.join(' ')}')
`);

const endLoad = Date.now();

console.log(endLoad - startLoad);
postMessage(true);

const runParser = (expr: string) => {
	const output = {
		mathml: '',
		latex: '',
		freeSymbols: []
	};
	if (!endLoad) return output;

	console.log(`evalulating .... [${expr}]`);
	if (!expr || expr.length === 0) {
		return output;
	}

	// function to convert expression to mathml
	let result = pyodide.runPython(`
		eq = sympy.S("${expr}", locals=_clash)
		sympy.mathml(eq)
	`);
	output.mathml = result;

	result = pyodide.runPython(`
		eq = sympy.S("${expr}", locals=_clash)
		latex(eq)
	`);
	output.latex = result;

	result = pyodide.runPython(`
		eq = sympy.S("${expr}", locals=_clash)
		list(eq.free_symbols)
	`);
	output.freeSymbols = result.toString();

	return output;
};

onmessage = function (e) {
	const { action, params } = e.data;

	switch (action) {
		case Actions.runParser:
			// eslint-disable-next-line
			return postMessage(runParser.apply(null, params));
		default:
			console.error(`${action} INVALID`);
	}
	return '';
};
