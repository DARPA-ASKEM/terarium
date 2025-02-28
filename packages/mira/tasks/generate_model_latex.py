import sys
import json
import traceback
from taskrunner import TaskRunnerInterface
import sympy
from mira.sources.amr import model_from_json

def cleanup():
    pass

def main():
    exitCode = 0

    try:
        taskrunner = TaskRunnerInterface(description="Generate LaTeX")
        taskrunner.on_cancellation(cleanup)

        data = taskrunner.read_input_str_with_timeout()
        amr = json.loads(data)
        model = model_from_json(amr)

        # =========================================
        # Generate LaTeX code string from MMT model
        # =========================================

        odeterms = {var: [] for var in sorted(model.get_concepts_name_map().keys())}

        # ODE terms from template rate laws
        for template in model.templates:
            if hasattr(template, "subject"):
                var = template.subject.name
                if template.rate_law is not None:
                    odeterms[var].append(-template.rate_law.args[0])
                else:
                    odeterms[var].append(0)

            if hasattr(template, "outcome"):
                var = template.outcome.name
                if template.rate_law is not None:
                    odeterms[var].append(template.rate_law.args[0])
                else:
                    odeterms[var].append(0)

        # Sort the terms such that all negative ones come first
        odeterms = {var: sorted(terms, key = lambda term: str(term)) for var, terms in odeterms.items()}

        # Time
        if model.time and model.time.name:
            time = model.time.name
        else:
            time = "t"
        t = sympy.Symbol(time)

        # Add "(t)" to all the state variables as time-dependent functions
        for var, terms in odeterms.items():
            for i, term in enumerate(terms):
                if hasattr(term, 'atoms'):
                    for atom in term.atoms(sympy.Symbol):
                        if str(atom) in odeterms.keys():
                            term = term.subs(atom, sympy.Function(str(atom))(t))
                    terms[i] = term

 
        # Construct equations
        num_terms = 5 # Max number of terms per line in LaTeX align
        odesys = []
        exprs = ""
        for i, (var, terms) in enumerate(odeterms.items()):

            lhs = sympy.diff(sympy.Function(var)(t), t)
            rhs = sum(terms)
            exprs += sympy.latex(lhs) + " ={}& "

            # Few equation terms = no wrapping needed
            if len(terms) < num_terms:
                exprs += sympy.latex(rhs)

            # otherwise, wrap around
            else:
                rhs = [sympy.latex(sum(terms[j:(j + num_terms)])) for j in range(0, len(terms), num_terms)]
                rhs = [line if (j == 0) | (line[0] == '-') else "+ " + line for j, line in enumerate(rhs)] # Add '+ ' to all lines past the first if not start with '- '
                exprs += " \\\\ \n    &".join(rhs)

            if i < (len(odeterms) - 1):
                exprs += " \\\\ \n"
            
            odesys = [exprs]


        # Repeat for observables if present
        if len(model.observables) > 0:

            # Sort observables alphabetically
            observables = {obs: model.observables[obs].expression.args[0] for obs in sorted(model.observables.keys())}

            # Add "(t)" for all the state variables as time-dependent symbols
            for obs, expr in observables.items():
                if hasattr(expr, 'atoms'):
                    for atom in expr.atoms(sympy.Symbol):
                        if str(atom) in odeterms.keys():
                            expr = expr.subs(atom, sympy.Function(str(atom))(t))
                    observables[obs] = expr

            for i, (obs, expr) in enumerate(observables.items()):
                lhs = sympy.Function(obs)(t)
                rhs = expr
                exprs = "     " + sympy.latex(lhs) + " ={}& " + sympy.latex(rhs)
                if i == 0:
                    exprs = " \\\\ \n" + exprs
                if i < (len(observables) - 1):
                    exprs += " \\\\ \n"

                odesys[0] += exprs

        # Reformat:
        odesys = "\\begin{align*} \n    " + odesys[0] + "\n\\end{align*}"
        # =========================================

        taskrunner.write_output_dict_with_timeout({"response": odesys})
        print("Generate LaTeX succeeded")

    except Exception as e:
        sys.stderr.write(f"Error: {str(e)}\n")
        sys.stderr.write(traceback.format_exc())
        sys.stderr.flush()
        exitCode = 1


    taskrunner.log("Shutting down")
    taskrunner.shutdown()
    sys.exit(exitCode)


if __name__ == "__main__":
    main()
