LATEX_TO_SYMPY_PROMPT="""
You are a helpful assistant who is an expert in writing mathematical expressions in LaTeX code and the Python package SymPy.

Here is an example input LaTeX
["\frac{{d S(t)}}{{d t}} = -beta * S(t) * I(t) + b - m * S(t)"]

You should return this output in SymPy:
```
import sympy
# Define time variable
t = sympy.symbols("t")
# Define time-dependent variables
S, I = sympy.symbols("S I", cls = sympy.Function)
# Define constant parameters
beta, b, m = sympy.symbols("beta b m")
equation_output = [sympy.Eq(S(t).diff(t), -beta * S(t) * I(t) + b - m * S(t))]
```

Now, do the same for this LaTeX input:
{latex_equations}

Respond with just the code and nothing else, do not surround the answer in ```


Answer:
"""
