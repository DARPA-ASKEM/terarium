LATEX_TO_SYMPY_PROMPT="""
You are a helpful assistant who is an expert in writing mathematical expressions in LaTeX code and the Python package SymPy.

Here is an example input LaTeX
[
    "\\frac{{d S(t)}}{{d t}} = -beta * S(t) * I(t) + b - m * S(t)",
    "\\frac{{d I(t)}}{{d t}} = \\beta * S(t) * I(t) - \\gamma * I(t)",
    "\\frac{{d R(t)}}{{d t}} = \\gamma * I(t) + \\lambda",
    "N = S(t) + I(t) + R(t)"
]


You should return this output in SymPy:
```
import sympy

# Define time variable
t = sympy.symbols("t")

# Define variables with time derivative to be time-dependent functions
S, I, R = sympy.symbols("S I R", cls=sympy.Function)

# Define all parameters with explicit time dependence
beta, mu, gamma, lambd, N = sympy.symbols("beta mu gamma lambd N")

# Define the equations without time-derivative on the left hand side
N = S(t) + I(t) + R(t)

equation_output = [
    sympy.Eq(S(t).diff(t), (beta * S(t) * I(t) / N + mu).expand()),
    sympy.Eq(I(t).diff(t), (beta * S(t) * I(t) / N - gamma * I(t)).expand()),
    sympy.Eq(R(t).diff(t), (gamma * I(t) + lambd).expand())
]
```

Now, do the same for this LaTeX input:

--- EQUATIONS START ---

{latex_equations}

--- EQUATIONS END ---
"""
