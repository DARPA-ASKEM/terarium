import sys
import io
from core.taskrunner import TaskRunnerInterface
from mira.metamodel.ops import simplify_rate_laws
from mira.modeling import Model
from mira.modeling.amr.petrinet import AMRPetriNetModel
from mira.sources.sbml import template_model_from_sbml_string


def cleanup():
    pass


def main():
    exitCode = 0

    try:
        taskrunner = TaskRunnerInterface(description="SBML to PetriNet")
        taskrunner.on_cancellation(cleanup)

        sbml = taskrunner.read_input_with_timeout()

        model_tm = template_model_from_sbml_string(io.StringIO(sbml))
        model_tm_ = simplify_rate_laws(model_tm)
        model_pn = AMRPetriNetModel(Model(model_tm_))
        model_pn_json = model_pn.to_json()

        taskrunner.write_output_with_timeout({"response": model_pn_json})
        print("SBML to PetriNet conversion succeeded")

    except Exception as e:
        sys.stderr.write(f"Error: {str(e)}\n")
        sys.stderr.flush()
        exitCode = 1

    taskrunner.log("Shutting down")
    taskrunner.shutdown()
    sys.exit(exitCode)


if __name__ == "__main__":
    main()
