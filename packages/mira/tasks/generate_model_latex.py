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
        taskrunner = TaskRunnerInterface(description="Generate latex")
        taskrunner.on_cancellation(cleanup)

        data = taskrunner.read_input_str_with_timeout()
        amr = json.loads(data)
        model = model_from_json(amr)

        odeterms = {var: 0 for var in model.get_concepts_name_map().keys()}

        for t in model.templates:
            if hasattr(t, "subject"):
                var = t.subject.name
                odeterms[var] -= t.rate_law.args[0]

            if hasattr(t, "outcome"):
                var = t.outcome.name
                odeterms[var] += t.rate_law.args[0]

        # Time
        symb = lambda x: sympy.Symbol(x)
        try:
            time = model.time.name
        except:
            time = "t"
        finally:
            t = symb(time)

        # Observables
        if len(model.observables) != 0:
            obs_eqs = [
                f"{{{obs.name}}}(t) = " + sympy.latex(obs.expression.args[0])
                for obs in model.observables.values()
            ]

        # Construct Sympy equations
        odesys = [
            sympy.latex(sympy.Eq(sympy.diff(sympy.Function(var)(t), t), terms))
            for var, terms in odeterms.items()
        ]

        #add observables.
        odesys += obs_eqs
		    #Reformat:
        odesys = "\\begin{align} \n    " + " \\\\ \n    ".join([eq for eq in odesys]) + "\n\\end{align}"

        taskrunner.write_output_dict_with_timeout({"response": odesys})
        print("Generate latex succeeded")

    except Exception as e:
        sys.stderr.write(f"Error: {str(e)}\n")
        sys.stderr.write(traceback.format_exc())
        sys.stderr.flush()
        exitCode = 1
        sys.exit(exitCode)


    taskrunner.log("Shutting down")
    taskrunner.shutdown()
    sys.exit(exitCode)


if __name__ == "__main__":
    main()
