import sys
import itertools
import traceback
import time
import base64
import json
from pydantic import BaseModel
from typing import List
from taskrunner import TaskRunnerInterface
from mira.metamodel import *
from mira.sources.amr.petrinet import template_model_from_amr_json
from mira.metamodel.utils import is_ontological_child

class CompareModelConcepts(BaseModel):
    amrs: List[str]  # expects AMRs to be a stringifies JSON object

def cleanup():
    pass

def main():
    exitCode = 0

    try:
        start = time.time()
        taskrunner = TaskRunnerInterface(description="Compare Model Concepts")
        taskrunner.on_cancellation(cleanup)

        taskrunner.log("Creating Models from input")
        input_dict = taskrunner.read_input_dict_with_timeout()
        amrs = CompareModelConcepts(**input_dict).amrs

        taskrunner.log(f"Received {len(amrs)} AMRs")

        ### Create MMT (MIRA Model Template) from input
        models = {}
        tags = []

        for i, amr_string in enumerate(amrs):
            amr = json.loads(amr_string)
            tags.append(f"Model {amr['header']['name']}")
            models[i] = template_model_from_amr_json(amr)

        end = time.time()
        taskrunner.log(f"Creating {len(models)} MMTs from input took {(end - start) * 1000} ms")

        ### Concept context comparison
        taskrunner.log("Concept context comparison")

        # comp = TemplateModelComparison(models.values(), is_ontological_child)
        comp = TemplateModelComparison(models.values(), is_ontological_child, tags, run_on_init=False)
        concept_context_comparison = comp.compare_context().to_csv(encoding='utf-8')

        previous_end = end
        end = time.time()
        taskrunner.log(f"Concept context comparison took {(end - previous_end) * 1000} ms")

        ### Tabular concept comparison
        taskrunner.log("Tabular concept comparison")

        tabular_comparison = {}
        for i, j in itertools.combinations(models.keys(), 2):
            table = get_concept_comparison_table(models[i], models[j], name_only=True)
            tabular_comparison[(i, j)] = table.to_csv(encoding='utf-8')

        previous_end = end
        end = time.time()
        taskrunner.log(f"Tabular concept comparison took {(end - previous_end) * 1000} ms")

        ### Concept graph comparison
        taskrunner.log("Concept graph comparison")

        concept_graph_comparison = {}
        for i, j in itertools.combinations(models.keys(), 2):
            image = TemplateModelDelta(
                template_model1=models[i],
                template_model2=models[j],
                refinement_function=is_ontological_child,
                concepts_only=True,
            )

            # Encode the image in base64
            image_base64 = base64.b64encode(image).decode('utf-8')

            # Store the base64 encoded image in the dictionary
            concept_graph_comparison[(i, j)] = image_base64

        previous_end = end
        end = time.time()
        taskrunner.log(f"Concept graph comparison took {(end - previous_end) * 1000} ms")


        ### Send the results back
        result = {
            "concept_context_comparison": concept_context_comparison,
            "tabular_comparison": tabular_comparison,
            "concept_graph_comparison": concept_graph_comparison
        }
        taskrunner.write_output_dict_with_timeout({"response": result})

        # Log the time taken to compare model concepts
        end = time.time()
        taskrunner.log(f"Compare Model Concepts took {(end - start) * 1000} ms")

        print("Compare Model Concepts succeeded")
    except Exception as e:
        sys.stderr.write(f"Error: {str(e)}\n")
        sys.stderr.write(traceback.format_exc())
        sys.stderr.flush()
        exitCode = 1


    taskrunner.log("Shutting down")
    taskrunner.shutdown()
    sys.exit(exitCode)


if __name__ == "__main__":
    main()
