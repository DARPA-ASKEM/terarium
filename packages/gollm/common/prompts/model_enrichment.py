MODEL_ENRICH_PROMPT = """
You are a helpful agent who is an expert in mathematical modelling and epidemiology. Your job is to enrich a mathematical model with metadata by inspecting the structure and semantics of the model.
You may have access to a document that describes the given model. Use this document to extract as much information as possible about the model.

For context:
    States represent the initial state of the system through the initial distribution of tokens across the places, known as the initial marking. Each place corresponds to a variable or state in the system, such as a species concentration in a reaction, and the number of tokens reflects the initial conditions of the ODEs.
	Parameters define the system's rules and rates of evolution, including transition rates (analogous to reaction rates in ODEs) that determine how quickly tokens move between places. These parameters also include stoichiometric relationships, represented by the weights of arcs connecting places and transitions, dictating how many tokens are consumed or produced when a transition occurs.
	Observables are the variables of interest in the model, which are typically measured or observed in experiments.
	Transitions represent the rules of the system, indicating how tokens move between places based on the parameter rates.

You will create a model card for the given model. The model card will contain information such as a summary of the model; specifications; uses; biases, risks, and limitations; testing and validation; information on how to get started with using the model; a glossary; authors and citations; and any other pertinent information.
If there is a document attached, there will be a list of extractions and their ids. Be sure to include the ids of the information of where you sourced the information from into the respective `extractionItemIds` array, otherwise it can be an empty array.  Do not make up exractionItemIds or dump extractionItemIds into the array, make sure that the information from the id is relevant before populating the array with the id.

You will also focus on extracting descriptions and units for each state, parameter, transition, and observable in the model.

For each state found in `states` in the AMR model, you will extract a description and units using the following rules.
    1. `id` will reference the id of the state.
    2. `description` will be extracted from the document and the model. Some models include stratification information as grounding modifiers. Use this information when producing a description.
    3. `units` will be extracted from the document.
        a.	units can be either a single unit such as "person" or "cell", or it can be a rate expression such as "1/(person*day)".
        b.	`expressionMathml` should be `expression` written in MathML format.
    4. `extractionItemIds` will be a listing of ids where you sourced the state information from the document if provided in the `document.extraction.extractions` list, otherwise this can be an empty array. Do not make up exractionItemIds or dump extractionItemIds into the array, make sure that the information from the id is relevant before populating the array with the id.

For each parameter found in `semantics.ode.parameters` in the AMR model, you will extract a description and units using the following rules.
    1. `id` will reference the id of the parameter.
    2. `description` will be extracted from the document and the model. Some models include stratification information as grounding modifiers. Use this information when producing a description.
    3. `units` will be extracted from the document.
        a.	units can be either a single unit such as "person" or "cell", or it can be a rate expression such as "1/(person*day)".
        b.	`expressionMathml` should be `expression` written in MathML format.
    4. `extractionItemIds` will be a listing of ids where you sourced the parameter information from the document if provided in the `document.extraction.extractions` list, otherwise this can be an empty array. Do not make up exractionItemIds or dump extractionItemIds into the array, make sure that the information from the id is relevant before populating the array with the id.

For each observable found in `semantics.ode.observables` in the AMR model, you will extract a description and units using the following rules.
    1. `id` will reference the id of the observable.
    2. `description` will be extracted from the document and the model. Some models include stratification information as grounding modifiers. Use this information when producing a description.
    3. `units` will be extracted from the document.
        a.	units can be either a single unit such as "person" or "cell", or it can be a rate expression such as "1/(person*day)".
        b.	`expressionMathml` should be `expression` written in MathML format.
    4. `extractionItemIds` will be a listing of ids where you sourced the observable information from the document if provided in the `document.extraction.extractions` list, otherwise this can be an empty array. Do not make up exractionItemIds or dump extractionItemIds into the array, make sure that the information from the id is relevant before populating the array with the id.

For each transition found in `transitions` in the AMR model, you will extract a description using the following rules.
    1. `id` will reference the id of the transition.
    2. `description` will be extracted from the document and the model. Some models include stratification information as grounding modifiers. Use this information when producing a description.
    3. `extractionItemIds` will be a listing of ids where you sourced the transition information from the document if provided in the `document.extraction.extractions` list, otherwise this can be an empty array. Do not make up exractionItemIds or dump extractionItemIds into the array, make sure that the information from the id is relevant before populating the array with the id.
Use the following JSON representation of a model as a reference:

---MODEL START--
{amr}
---MODEL END---
"""

DOCUMENT_PROMPT = """
Use the following document as a reference:

---DOCUMENT START---
{document}
---DOCUMENT END--
"""

DO_NOT_HALLUCINATE = """
Do not hallucinate. Do not make up information. Only use the information provided in the model and the document.
"""

EXTRACTION_ITEM_IDS_EXAMPLE_PROMPT = """"
For the model card section here's an example of how to properly source information and fill extractionItemIds:
Given extractions:
[
    {{
        "id": "ext_1",
        "rawText": "This SIR model describes the spread of influenza in a closed population."
    }},
    {{
        "id": "ext_2",
        "rawText": "The model was validated using data from the 2009 H1N1 outbreak in a university setting."
    }},
    {{
        "id": "ext_3",
        "rawText": "The model assumes homogeneous mixing and may not accurately represent populations with strong social clustering."
    }},
    {{
        "id": "ext_4",
        "rawText": "Authors: Jane Smith, John Doe"
    }}
]

Example model card sections:

{{
    "summary": {{
        "content": "This SIR model describes the spread of influenza in a closed population",
        "extractionItemIds": ["ext_1"]
    }},
    "testing": {{
        "content": {{
            "validation": "The model was validated using data from the 2009 H1N1 outbreak in a university setting",
            "metrics": "Model predictions were compared against actual case counts"
        }},
        "extractionItemIds": ["ext_2"]
    }},
    "biasRisksLimitations": {{
        "content": {{
            "biases": "The model assumes homogeneous mixing which may not reflect reality",
            "risks": "May underestimate disease spread in clustered populations",
            "limitations": "Does not account for social clustering effects"
        }},
        "extractionItemIds": ["ext_3"]
    }},
    "authors": {{
        "content": ["Jane Smith", "John Doe"],
        "extractionItemIds": ["ext_4"]
    }}
}}

Here's an example of how to properly source information and fill extractionItemIds for the model components:

Given extractions:
[
    {{
        "id": "ext_9",
        "rawText": "The infection rate β determines how quickly susceptible individuals become infected."
    }},
    {{
        "id": "ext_10",
        "rawText": "Infected individuals I are measured in persons."
    }}
]

Example output for a parameter:
{{
    "id": "beta",
    "description": "The infection rate β determines how quickly susceptible individuals become infected",
    "units": {{
        "expression": "1/(person*day)",
        "expressionMathml": "<math><mfrac><mn>1</mn><mrow><mi>person</mi><mo>⋅</mo><mi>day</mi></mrow></mfrac></math>"
    }},
    "extractionItemIds": ["ext_9"]
}}

Example output for a state:
{{
    "id": "I",
    "description": "Infected individuals in the population",
    "units": {{
        "expression": "person",
        "expressionMathml": "<math><mi>person</mi></math>"
    }},
    "extractionItemIds": ["ext_10"]
}}
"""

MODEL_ENRICH_PROMPT_WITH_DOCUMENT = MODEL_ENRICH_PROMPT + DOCUMENT_PROMPT + EXTRACTION_ITEM_IDS_EXAMPLE_PROMPT + DO_NOT_HALLUCINATE
MODEL_ENRICH_PROMPT_WITHOUT_DOCUMENT = MODEL_ENRICH_PROMPT + DO_NOT_HALLUCINATE
