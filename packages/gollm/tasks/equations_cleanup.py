import sys
import traceback
from chains import cleanup_equations_chain
from entities import EquationsModel
from llms.azure.AzureTools import AzureTools
from llms.determine_llm import determine_llm

from taskrunner import TaskRunnerInterface


def cleanup():
    pass


def main():
    exitCode = 0
    try:
        taskrunner = TaskRunnerInterface(description="Reformat equations based on style guide")
        taskrunner.on_cancellation(cleanup)

        input_dict = taskrunner.read_input_dict_with_timeout()

        taskrunner.log("Creating EquationsModel from input")
        input_model = EquationsModel(**input_dict)

        try:
            llm = determine_llm(input_model.llm)
            taskrunner.log(f"Using {llm.name}")
        except Exception as e:
            llm = AzureTools()
            taskrunner.log(f"WARNING: {e}, defaulting to {llm.name}")

        response = cleanup_equations_chain(llm, equations=input_model.equations)
        taskrunner.log("Received response from LLM")

        taskrunner.write_output_dict_with_timeout({"response": response})

    except Exception:
        sys.stderr.write(traceback.format_exc())
        sys.stderr.flush()
        exitCode = 1

    taskrunner.log("Shutting down")
    taskrunner.shutdown()
    sys.exit(exitCode)


if __name__ == "__main__":
    main()
