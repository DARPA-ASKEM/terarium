import sys
import json
import traceback

from taskrunner import TaskRunnerInterface
from sympy.parsing.latex import parse_latex
from mira.sources.sympy_ode import template_model_from_sympy_odes
from mira.modeling.amr.petrinet import template_model_to_petrinet_json

def cleanup():
    pass

def main():
    exitCode = 0

    try:
        taskrunner = TaskRunnerInterface(description="LaTeX to AMR")
        taskrunner.on_cancellation(cleanup)

        latex_input = taskrunner.read_input_str_with_timeout()
        latex_json = json.loads(latex_input)

        # LaTeX to SymPy
        sympy_exprs = []
        for latex_expr in latex_json:
            sympy_expr = parse_latex(latex_expr)
            sympy_exprs.append(sympy_expr)

        # SymPy to MMT
        mmt = template_model_from_sympy_odes(sympy_exprs)

        # MMT to AMR
        amr_json = template_model_to_petrinet_json(mmt)

        taskrunner.log(f"LaTeX to AMR conversion succeeded")
        taskrunner.write_output_dict_with_timeout({"response": json.dumps(amr_json)})

        print("LaTeX to AMR conversion succeeded")
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
