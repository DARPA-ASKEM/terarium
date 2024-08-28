import json
import sys
from gollm.entities import ConfigureModel
from gollm.openai.tool_utils import amr_enrichment_chain
from taskrunner import TaskRunnerInterface


def cleanup():
    pass


def main():
    exitCode = 0
    try:
        taskrunner = TaskRunnerInterface(description="AMR Enrichment CLI")
        taskrunner.on_cancellation(cleanup)

        input_dict = taskrunner.read_input_dict_with_timeout()

        if isinstance(input_dict['amr'], str):
            amr_dict = json.loads(input_dict['amr'])
        else:
            amr_dict = input_dict['amr']

        taskrunner.log("Sending request to OpenAI API")
        response = amr_enrichment_chain(
            research_paper=input_dict['research_paper'], amr=amr_dict
        )
        taskrunner.log("Received response from OpenAI API")

        taskrunner.write_output_dict_with_timeout({"response": response})

    except Exception as e:
        sys.stderr.write(f"Error: {str(e)}\n")
        sys.stderr.flush()
        exitCode = 1

    taskrunner.log("Shutting down")
    taskrunner.shutdown()
    sys.exit(exitCode)


if __name__ == "__main__":
    main()
