import json
import sys
import traceback
from chains import model_config_from_dataset_chain
from entities import ModelAndDataset
from llms.azure.AzureTools import AzureTools
from llms.llama.LlamaTools import LlamaTools
from llms.openai.OpenAiTools import OpenAiTools

from taskrunner import TaskRunnerInterface


def cleanup():
    pass


def main():
    exitCode = 0
    try:
        taskrunner = TaskRunnerInterface(description="Configure Model from dataset CLI")
        taskrunner.on_cancellation(cleanup)

        input_dict = taskrunner.read_input_dict_with_timeout()

        taskrunner.log("Creating ConfigureModel from input")
        input_model = ModelAndDataset(**input_dict)
        amr = json.dumps(input_model.amr, separators=(",", ":"))

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

        response = model_config_from_dataset_chain(llm, dataset=input_model.dataset, amr=amr, matrix=input_model.matrix)
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
