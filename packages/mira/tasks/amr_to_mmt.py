import sys
import json
import traceback

from taskrunner import TaskRunnerInterface
from mira.sources.amr import model_from_json

def cleanup():
    pass

def main():
    exitCode = 0

    try:
        taskrunner = TaskRunnerInterface(description="AMR to MMT")
        taskrunner.on_cancellation(cleanup)

        data = taskrunner.read_input_str_with_timeout()
        amr = json.loads(data)
        mmt = model_from_json(amr)

        template_params = {}

        count = 0
        for tm in mmt.templates:
            # Sanitize
            count = count + 1
            if tm.name == None or tm.name == "":
                tm.name = "generated-" + str(count)

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

        # Summarize observables, extract out concepts

        # concept_names = list(map(lambda x: x.name, mmt.get_concepts_map().values()))
        # FIXME: get_concept_map seems to be unreliable, need better/model-agnostic way to parse
        concept_names = list(map(lambda x: x["id"], amr["model"]["states"]))

        observable_summary = {}
        for ob in mmt.observables.items():
            obKey = ob[0]
            obValue = ob[1]
            observable_summary[obKey] = {
                "name": obKey,
                "display_name": obValue.display_name,
                "expression": str(obValue.expression),
                "references": list(obValue.get_parameter_names(concept_names))
            }

        result = {
            "template_params": template_params,
            "observable_summary": observable_summary,
            "mmt": json.loads(mmt.json())
        }
        taskrunner.write_output_dict_with_timeout({"response": result})

        print("AMR to MMT conversion succeeded")
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
