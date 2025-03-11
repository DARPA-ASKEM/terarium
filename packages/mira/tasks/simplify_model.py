import sys
import json
import traceback
import time

from taskrunner import TaskRunnerInterface
from mira.sources.amr import model_from_json
from mira.modeling.amr.ops import *
from mira.metamodel.ops import check_simplify_rate_laws


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
        result = {
            "amr": amr,
            "max_controller_decrease": 0
        }
        template_model = template_model_from_amr_json(amr)
        check_simplify_result = check_simplify_rate_laws(template_model)
        end = time.time()

        if (check_simplify_result["result"] == "MEANINGFUL_CHANGE"):
            result["amr"]= template_model_to_petrinet_json(check_simplify_result["simplified_model"])
            result["max_controller_decrease"] = check_simplify_result["max_controller_decrease"]

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

