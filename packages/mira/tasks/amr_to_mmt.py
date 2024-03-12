import sys
import os
import json
from core.taskrunner import TaskRunnerInterface
from mira.sources.amr.petrinet import template_model_from_amr_json
from mira.sources.amr import model_from_json

def cleanup():
    pass

def main():
    exitCode = 0

    try:
        taskrunner = TaskRunnerInterface(description="AMR to MMT")
        taskrunner.on_cancellation(cleanup)

        data = taskrunner.read_input_with_timeout()
        amr = json.loads(data)
        mmt = model_from_json(amr)

        template_params = {}
        for tm in mmt.templates:
            params = tm.get_parameter_names()
            params = list(params)

            subject = None
            outcome = None

            if hasattr(tm, 'subject'):
                subject = tm.subject.name

            if hasattr(tm, "outcome"):
                outcome = tm.outcome.name

            controllers = [x.name for x in tm.get_controllers()]
            entry = {
                "name": tm.name,
                "params": params,
                "subject": subject,
                "outcome": outcome,
                "controllers": controllers
            }
            template_params[tm.name] = entry

        result = {
            "template_params": template_params,
            "mmt": json.loads(mmt.json())
        }
        taskrunner.write_output_with_timeout({"response": result})

        print("AMR to MMT conversion succeeded")
    except Exception as e:
        sys.stderr.write(f"Error: {str(e)}\n")
        sys.stderr.flush()
        exitCode = 1


    taskrunner.log("Shutting down")
    taskrunner.shutdown()
    sys.exit(exitCode)


if __name__ == "__main__":
    main()
