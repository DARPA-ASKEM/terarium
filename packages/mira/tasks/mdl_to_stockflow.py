import sys
import os
from taskrunner import TaskRunnerInterface
from mira.sources.system_dynamics.vensim import template_model_from_mdl_file
from mira.modeling.amr.stockflow import template_model_to_stockflow_json


def cleanup():
    pass


def get_filename(id: str):
    return f"/tmp/{id}.mdl"


def create_temp_file(name, contents):
    with open(name, 'w') as f:
        f.write(contents)


def delete_temp_file(name):
    try:
        os.remove(name)
    except:
        pass


def main():
    exitCode = 0

    try:
        taskrunner = TaskRunnerInterface(description="MDL to StockFlow")
        taskrunner.on_cancellation(cleanup)

        mdl = taskrunner.read_input_str_with_timeout()

        filename = get_filename(taskrunner.id)

        create_temp_file(filename, mdl)

        taskrunner.log("Converting MDL to template model...")
        model_tm = template_model_from_mdl_file(filename)

        taskrunner.log("Converting template model to stockflow...")
        stockflow_json = template_model_to_stockflow_json(model_tm)

        taskrunner.write_output_dict_with_timeout({"response": stockflow_json})
        print("MDL to StockFlow conversion succeeded")

    except Exception as e:
        sys.stderr.write(f"Error: {str(e)}\n")
        sys.stderr.flush()
        exitCode = 1

    delete_temp_file(filename)

    taskrunner.log("Shutting down")
    taskrunner.shutdown()
    sys.exit(exitCode)


if __name__ == "__main__":
    main()
