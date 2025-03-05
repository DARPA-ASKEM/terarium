import sys
import traceback
from chains import equations_from_image_chain
from entities import EquationsFromImage
from llms.azure.AzureTools import AzureTools
from llms.determine_llm import determine_llm

from taskrunner import TaskRunnerInterface


def cleanup():
    pass


def main():
    exitCode = 0
    try:
        taskrunner = TaskRunnerInterface(description="Equations from image CLI")
        taskrunner.on_cancellation(cleanup)

        input_dict = taskrunner.read_input_dict_with_timeout()

        taskrunner.log("Creating EquationsFromImage from input")
        input_model = EquationsFromImage(**input_dict)

        try:
            llm = determine_llm(input_model.llm)
            taskrunner.log(f"Using {llm.name}")
        except Exception as e:
            llm = AzureTools()
            taskrunner.log(f"WARNING: {e}, defaulting to {llm.name}")

        response = equations_from_image_chain(llm, image=input_model.image)
        taskrunner.log("Received response from LLM")

        taskrunner.write_output_dict_with_timeout({"response": response})

    except Exception as e:
        sys.stderr.write(traceback.format_exc())
        sys.stderr.flush()
        exitCode = 1

    taskrunner.log("Shutting down")
    taskrunner.shutdown()
    sys.exit(exitCode)


if __name__ == "__main__":
    main()
