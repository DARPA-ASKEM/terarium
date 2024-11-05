import sys
import json
import traceback

from taskrunner import TaskRunnerInterface
from sympy.parsing.latex import parse_latex

def cleanup():
    pass

def main():
    exitCode = 0

    try:
        taskrunner = TaskRunnerInterface(description="Latex to SymPy")
        taskrunner.on_cancellation(cleanup)

        latex_str = taskrunner.read_input_str_with_timeout()

        sympy_expr = parse_latex(latex_str)

        taskrunner.log(f"Latex to SymPy conversion succeeded: {sympy_expr}")

        taskrunner.write_output_dict_with_timeout({"response": str(sympy_expr)})

        print("Latex to SymPy conversion succeeded")
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
