import sys
import json
import traceback
import time

from taskrunner import TaskRunnerInterface
from mira.sources.amr import model_from_json
from mira.modeling.amr.ops import *


def cleanup():
    pass

def main():
    exitCode = 0

    try:
        start = time.time()
        taskrunner = TaskRunnerInterface(description="model ratelaw simplification")
        taskrunner.on_cancellation(cleanup)

        data = taskrunner.read_input_str_with_timeout()
        amr = json.loads(data)

        amr_simplified = simplify_rate_laws(amr)
        end = time.time()

        result = {
            "amr": amr_simplified
        }
        taskrunner.write_output_dict_with_timeout({"response": result})
        taskrunner.log(f"model ratelaw simplification took {(end - start) * 1000} ms")

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

