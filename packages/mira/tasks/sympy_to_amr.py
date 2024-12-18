import sys
import json
import traceback
import sympy

from taskrunner import TaskRunnerInterface
from mira.sources.sympy_ode import template_model_from_sympy_odes
from mira.modeling.amr.petrinet import template_model_to_petrinet_json

from mira.metamodel.template_model import Initial, Parameter
from mira.metamodel.utils import safe_parse_expr

def cleanup():
    pass

def main():
    exitCode = 0

    try:
        taskrunner = TaskRunnerInterface(description="Sympy to AMR")
        taskrunner.on_cancellation(cleanup)

        sympy_code = taskrunner.read_input_str_with_timeout()
        taskrunner.log("== input code")
        taskrunner.log(sympy_code)
        taskrunner.log("")

        globals = {}
        exec(sympy_code, globals) # output should be in placed into "equation_output"
        taskrunner.log("== equations")
        taskrunner.log(globals["equation_output"])
        taskrunner.log("")

        # SymPy to MMT
        mmt = template_model_from_sympy_odes(globals["equation_output"])

        # Set default values for the model parameters
        for p, param in mmt.parameters.items():
            mmt.parameters[p].value = 0.0

        # Initialize the model
        mmt.initials = mmt.initials | {c: Initial(concept = concept, expression = safe_parse_expr(f'{c}0')) for c, concept in mmt.get_concepts_name_map().items()}

        # Set default values for the initial condition parameters
        mmt.parameters = mmt.parameters | {f'{c}0': Parameter(name = f'{c}0', display_name = f'{c}0', description = f'Initial condition of state variable {c}', value = 0.0) for c in mmt.get_concepts_name_map().keys()}
        
        # MMT to AMR
        amr_json = template_model_to_petrinet_json(mmt)

        # Gather results
        response = {}
        response["amr"] = amr_json
        response["sympyCode"] = sympy_code
        response["sympyExprs"] = list(map(lambda x: str(x), globals["equation_output"]))

        taskrunner.log(f"Sympy to AMR conversion succeeded")
        taskrunner.write_output_dict_with_timeout({"response": response })
        
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
