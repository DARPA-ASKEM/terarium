import sys
import traceback
from chains import enrich_dataset_chain
from entities import DatasetCardModel
from llms.azure.AzureTools import AzureTools
from llms.llama.LlamaTools import LlamaTools
from llms.openai.OpenAiTools import OpenAiTools

from taskrunner import TaskRunnerInterface


def cleanup():
    pass

def main():
    global taskrunner
    exit_code = 0

    try:
        taskrunner = TaskRunnerInterface(description="Dataset Enrichment CLI")
        taskrunner.on_cancellation(cleanup)

        input_dict = taskrunner.read_input_dict_with_timeout()

        taskrunner.log("Creating DatasetCardModel from input")
        inputs = DatasetCardModel(**input_dict)

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

        response = enrich_dataset_chain(llm, dataset=inputs.dataset, document=inputs.document)
        taskrunner.log("Received response from LLM")

        taskrunner.write_output_dict_with_timeout({"response": response})

    except Exception as e:
        sys.stderr.write(traceback.format_exc())
        sys.stderr.flush()
        exit_code = 1

    taskrunner.log("Shutting down")
    taskrunner.shutdown()
    sys.exit(exit_code)

if __name__ == "__main__":
    main()
