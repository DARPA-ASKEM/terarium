import sys
from taskrunner import TaskRunnerInterface
from mira.metamodel.ops import simplify_rate_laws
from mira.modeling import Model
from mira.modeling.amr.petrinet import AMRPetriNetModel
from mira.sources.sbml import template_model_from_sbml_file


def cleanup():
    pass


def get_filename(id: str):
    return f"/tmp/{id}.stella"


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
        taskrunner = TaskRunnerInterface(description="SBML to PetriNet")
        taskrunner.on_cancellation(cleanup)

        sbml = taskrunner.read_input_str_with_timeout()

        filename = get_filename(taskrunner.id)

        create_temp_file(filename, sbml)

        model_tm = template_model_from_sbml_file(filename)
        model_tm_ = simplify_rate_laws(model_tm)
        model_pn = AMRPetriNetModel(Model(model_tm_))
        model_pn_json = model_pn.to_json()

        taskrunner.write_output_dict_with_timeout({"response": model_pn_json})
        print("SBML to PetriNet conversion succeeded")

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
