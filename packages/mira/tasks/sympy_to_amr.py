import sys
import json
import traceback
import sympy

from taskrunner import TaskRunnerInterface
from mira.sources.sympy_ode import template_model_from_sympy_odes
from mira.modeling.amr.petrinet import template_model_to_petrinet_json

def cleanup():
    pass

def main():
    exitCode = 0

    try:
        taskrunner = TaskRunnerInterface(description="LaTeX to AMR")
        taskrunner.on_cancellation(cleanup)

        sympy_code = taskrunner.read_input_str_with_timeout()
        taskrunner.log("")
        taskrunner.log("!!!!!")
        taskrunner.log(sympy_code)
        taskrunner.log("")
        taskrunner.log("")

        globals = {}
        exec(sympy_code, globals) # output should be in placed into "equation_output"
        taskrunner.log("")
        taskrunner.log("")
        taskrunner.log("")
        taskrunner.log("")
        taskrunner.log("equations")
        taskrunner.log(globals["equation_output"])
        taskrunner.log("")
        taskrunner.log("")
        taskrunner.log("")
        taskrunner.log("")

        # SymPy to MMT
        mmt = template_model_from_sympy_odes(globals["equation_output"])

        # MMT to AMR
        amr_json = template_model_to_petrinet_json(mmt)

        # amr_json = {}

        taskrunner.log("");
        taskrunner.log("");
        taskrunner.log(amr_json)
        taskrunner.log("");
        taskrunner.log("");


        # Gather results
        response = {}
        # response["sympyExprs"] = list(map(lambda x: str(x), sympy_exprs))
        response["amr"] = amr_json
        taskrunner.log(f"LaTeX to AMR conversion succeeded")
        taskrunner.write_output_dict_with_timeout({"response": response })

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
