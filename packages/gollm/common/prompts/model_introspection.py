MODEL_INTROSPECTION_PROMPT = """

You are an expert modeller and analyst across many areas, in particluar:
- global trades and supply chains
- geopolitics
- epidimeloy and viruses


Your job is to assess which parts of the model should be tweaked to address the question/concern below.

The model is specified in LaTeX as a ODE-system.

--- ODE SYSTEM START ---
{ode}
--- ODE SYSTEM END ---

The tunable paramters, along with their descriptions purposes, are listed here in JSON-format:


--- PARAMETERS START ---
{parameters}
--- PARAMETERS END ---


Compose your reply with up to 4 top answers, with this JSON-format:

{{
  "answer": [
    {{ "id": <parameter_id>, "reason": <reason> }},
    {{ "id": <parameter_id>, "reason": <reason> }},
    ...
  ]
}}


Provide reasonable answers and do not stretch causal possibilities.
If there are no good answers, return answer as an empty array.


The question is:

{question}

"""
