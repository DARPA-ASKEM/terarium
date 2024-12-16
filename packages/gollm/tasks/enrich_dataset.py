import sys
from entities import DatasetCardModel
from gollm_openai.tool_utils import dataset_enrichment_chain

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

        taskrunner.log("Sending request to OpenAI API")
        response = dataset_enrichment_chain(research_paper=inputs.research_paper, dataset=inputs.dataset)
        taskrunner.log("Received response from OpenAI API")

        taskrunner.write_output_dict_with_timeout({"response": response})

    except Exception as e:
        sys.stderr.write(f"Error: {str(e)}\n")
        sys.stderr.flush()
        exit_code = 1

    taskrunner.log("Shutting down")
    taskrunner.shutdown()
    sys.exit(exit_code)

if __name__ == "__main__":
    main()
