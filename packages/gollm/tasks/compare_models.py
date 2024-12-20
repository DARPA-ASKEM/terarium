import sys
from entities import ModelCompareModel
from gollm_openai.tool_utils import compare_models

from taskrunner import TaskRunnerInterface


def cleanup():
    pass


def main():
    exitCode = 0
    try:
        taskrunner = TaskRunnerInterface(description="Model Comparison CLI")
        taskrunner.on_cancellation(cleanup)

        input_dict = taskrunner.read_input_dict_with_timeout()

        taskrunner.log("Creating ModelCompareModel from input")
        input_model = ModelCompareModel(**input_dict)

        taskrunner.log("Sending request to OpenAI API")
        response = compare_models(amrs=input_model.amrs, goal=input_model.goal)
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
