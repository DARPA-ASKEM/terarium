import sys
import traceback
from chains import enrich_dataset_chain
from entities import DatasetCardModel
from llms.azure.AzureTools import AzureTools
from llms.determine_llm import determine_llm

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
        input_model = DatasetCardModel(**input_dict)

        try:
            llm = determine_llm(input_model.llm)
            taskrunner.log(f"Using {llm.name}")
        except Exception as e:
            llm = AzureTools()
            taskrunner.log(f"WARNING: {e}, defaulting to {llm.name}")

        response = enrich_dataset_chain(llm, dataset=input_model.dataset, document=input_model.document)
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
