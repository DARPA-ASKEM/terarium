import sys
from gollm_openai.tool_utils import generate_response

from taskrunner import TaskRunnerInterface


def cleanup():
    pass


def main():
    exitCode = 0
    try:
        taskrunner = TaskRunnerInterface(description="Generate Response CLI")
        taskrunner.on_cancellation(cleanup)

        input_dict = taskrunner.read_input_dict_with_timeout()

        taskrunner.log("Generating a response from input")

        taskrunner.log("Sending request to OpenAI API")
        response = generate_response(instruction=input_dict["instruction"], response_format=input_dict.get("responseFormat", None))
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
