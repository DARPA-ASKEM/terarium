import sys
import traceback
from chains import latex_to_sympy_chain
from entities import EquationsModel
from llms.azure.AzureTools import AzureTools
from llms.llama.LlamaTools import LlamaTools
from llms.openai.OpenAiTools import OpenAiTools

from taskrunner import TaskRunnerInterface


def cleanup():
    pass


def main():
    exitCode = 0
    try:
        taskrunner = TaskRunnerInterface(description="Converting latex to sympy")
        taskrunner.on_cancellation(cleanup)

        input_dict = taskrunner.read_input_dict_with_timeout()

        taskrunner.log("Creating EquationsModel from input")
        input_model = EquationsModel(**input_dict)

        if input_model.llm == "llama":
            taskrunner.log("Using Llama LLM")
            llm = LlamaTools()
        elif input_model.llm == "openai":
            taskrunner.log("Using OpenAI LLM")
            llm = OpenAiTools()
        elif input_model.llm == "azure":
            taskrunner.log("Using Azure OpenAI LLM")
            llm = AzureTools()
        else:
            taskrunner.log("No LLM specified, Defaulting to Azure OpenAI LLM")
            llm = AzureTools()

        response = latex_to_sympy_chain(llm, equations=input_model.equations)
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
