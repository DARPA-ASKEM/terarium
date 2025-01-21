CONFIGURE_FROM_DOCUMENT_PROMPT = """
You are a helpful agent who is an expert in mathematical modelling and epidemiology.
You are provided a mathematical model describing a system of ODEs and a research paper that describes various conditions to which the model can be applied.
Your job is to to find state and parameter configurations for the given mathematical model from various conditions described in a research paper.

For context, a state in the system usually corresponds to a species concentration in a reaction and parameters define the system's rules and rates of evolution, including transition rates (analogous to reaction rates in ODEs) or the value for the states at the first timepoint.

Use the following JSON formatted mathematical model:

---START MODEL JSON---
{amr}
---END MODEL JSON---

and the following JSON formatted research paper:

---START PAPER---
{research_paper}
---END PAPER---

Assume that the paper describes multiple conditions to which the model can be applied. Create a model configuration for each condition. (E.g. different countries, intervention policies, or disease severity)

Be sure to extract state and parameter values from the paper, and do not use the default values from the model.
Be sure to use consistent naming conventions for the conditions. Instead of 'condition_1' and 'condition_2', use descriptive names.
State and parameter values are often found in tables. Look at the tables in the paper first and pay attention to the table structure when determining which values correspond to which state or parameter.

For each condition, create a model configuration JSON object that satisfies the JSON schema specified in the response format. To do this, follow the instructions below:
1.	Create a value for `name` and `description` from the paper.
2.	For the description, provide a long-form description of the condition. If the description cannot be created from the text, set it to an empty string.
3.	`model_id` id a UUID. If the model has an id, you can use it. Otherwise, you can set as the nil UUID "00000000-0000-0000-0000-000000000000".
4.	For each state specified in the model, extract information from the text and create an initial semantic object. Do not create new initial semantic objects for states that are not included in the original model. If you cannot find a value or expression for the initial state, do not create an initial semantic object. Use the following rules to determine the value of a state:
    a.  A state can be a mathematical expression of the initial condition parameter of other states in the system (e.g. the initial condition for the susceptible population equals the total population minus the population of all the other disease states).
    b.  A state can be a constant value.
5.	For each parameter specified in the model, extract information from the text and create a parameter semantic object. Do not create new parameter semantic objects for parameters that are not included in the original model. If you cannot find a value for the parameter, do not create a parameter semantic object. Parameters can be an initial value for a state, a constant value for a transition, or a range or distribution for a transition. Use the following rules to determine the value of a parameter:
    a.	Determine if the parameter is an initial value for a state in the model. These will have an ID of the state they are associated with, with a 0 appended to the end (E.g. If the model has a state with an ID of "S" the initial value parameter will have an ID of "S0"). If this is the case, search the document for an initial value for the state and create a constant value by setting the distribution type to "Constant" and add the value to the `value` field. Sometimes the state will be a mathematical expression. In these cases, evaluate the expression for timepoint 0 and provide the resulting value.
    b.  Constant parameters will have a single value. To create a constant parameter, set the distribution type to "Constant" and add the value to the `value` field. Sometimes constants will be in the form of a ratio (E.g. 1/n), in these cases, evaluate the ratio and provide the resulting value.
    c.  Distributed parameters will usually appear as a range (E.g. 0.01 - 0.2) or a list of values (E.g. [0.01 0.02 0.03 0.04 0.05] or 0.01, 0.02, 0.03, 0.04, 0.05 ). To create a distribution parameter, set the distribution type to "StandardUniform1" and add the smallest value in the range or list to the `minimum` field and the largest value to the `maximum` field.
6. `observableSemanticList` should be an empty list.
7. `inferredParameterList` should be an empty list.
8. Determine what page the information was extracted from and set the `extractionPage` value to that page number. If the page number cannot be determined, set it to 0. Only pick one page number. Do not provide a range.

For context, use the following as an example of a correctly extracted model configuration from a JSON formatted model using a paper:

With the following JSON formatted model:

--- SAMPLE MODEL ---
{{
  "name": "Sample model",
  "header": {{
    "name": "Sample model",
    "description": "A sample model.",
    "schema": "https://github.com/DARPA-ASKEM/Model-Representations/blob/main/petrinet/petrinet_schema.json",
    "schema_name": "petrinet",
    "model_version": "0.1"
  }},
  "model": {{
    "states": [
        {{
        "id": "S",
        "name": "S",
        "description": "Susceptible population"
      }},
      {{
        "id": "I",
        "name": "I",
        "description": "Infected population"
      }},
      {{
        "id": "R",
        "name": "R",
        "description": "Recovered population",
      }}
    ],
    "transitions": [
      {{
        "id": "t0",
        "input": [
          "S",
          "I"
        ],
        "output": [
          "I",
          "I"
        ]
      }},
      {{
        "id": "t1",
        "input": [
          "I"
        ],
        "output": [
          "R"
        ]
      }}
    ]
  }},
  "semantics": {{
    "ode": {{
      "rates": [
        {{
          "target": "t0",
          "expression": "I*β*S",
          "expression_mathml": "<math><apply><times/><ci>I</ci><ci>β</ci><ci>S</ci></apply></math>"
        }},
        {{
          "target": "t1",
          "expression": "γ*I",
          "expression_mathml": "<math><apply><times/><ci>γ</ci><ci>I</ci></apply></math>"
        }}
      ],
      "initials": [
        {{
          "target": "S",
          "expression": "1",
          "expression_mathml": "<math><cn>1</cn></math>"
        }},
        {{
          "target": "I",
          "expression": "0",
          "expression_mathml": "<math><cn>0</cn></math>"
        }},
        {{
          "target": "R",
          "expression": "0",
          "expression_mathml": "<math><cn>0</cn></math>"
        }}
      ],
      "parameters": [
        {{
          "id": "N",
          "name": "N",
          "value": 1
        }},
        {{
          "id": "β",
          "name": "β",
          "value": 0.6
        }},
        {{
          "id": "γ",
          "name": "γ",
          "value": 0.2
        }},
        {{
          "id": "S0",
          "name": "S0",
          "value": 1
        }}
        {{
          "id": "I0",
          "name": "I0",
          "value": 0
        }}
        {{
          "id": "R0",
          "name": "R0",
          "value": 0
        }}
      ],
      "time": {{
        "id": "t"
      }}
    }}
  }}
}}
--- END SAMPLE MODEL ---

and the following JSON formatted research paper:

--- SAMPLE PAPER ---
[
  {{
    "pageNumber": 0,
    "text": "Initial Condition\nS0 = N-I0-R0, with I0 = 2 and R0 = 0\nβ = 0.12\nγ = 0.21\nTotal population(N) = 1000",
    "tables": [],
    "equations": []
  }}
]
--- END SAMPLE PAPER ---

The correctly extracted model configuration would be:

{{
  "conditions": [
    {{
      "modelId": "00000000-0000-0000-0000-000000000000",
      "name": "Initial Condition",
      "description": "This configuration models the Initial Condition of the SIR compartmental model",
      "extractionPage": 0,
      "inferredParameterList": None,
      "initialSemanticList": [
        {{
          "expression": "N-I0-R0",
          "expressionMathml": "<math><apply><minus/><apply><minus/><ci>N</ci><ci>I0</ci></apply><ci>R0</ci></apply></math>",
          "source": "Page 0",
          "target": "S",
          "type": "initial"
        }},
        {{
          "expression": "I0",
          "expressionMathml": "<math><ci>I0</ci></math>",
          "source": "Page 0",
          "target": "I",
          "type": "initial"
        }},
        {{
          "expression": "R0",
          "expressionMathml": "<math><ci>R0</ci></math>",
          "source": "Page 0",
          "target": "R",
          "type": "initial"
        }}
      ],
      "observableSemanticList": [],
      "parameterSemanticList": [
        {{
          "distribution": {{
            "parameters": {{
              "value": 1000
            }},
            "type": "Constant"
          }},
          "referenceId": "N",
          "source": "Page 0",
          "type": "parameter"
        }},
        {{
          "distribution": {{
            "parameters": {{
              "value": 0.12
            }},
            "type": "Constant"
          }},
          "referenceId": "β",
          "source": "Page 0",
          "type": "parameter"
        }},
        {{
          "distribution": {{
            "parameters": {{
              "value": 0.21
            }},
            "type": "Constant"
          }},
          "referenceId": "γ",
          "source": "Page 0",
          "type": "parameter"
        }},
        {{
          "distribution": {{
            "parameters": {{
              "value": 998
            }},
            "type": "Constant"
          }},
          "referenceId": "S0",
          "source": "Page 0",
          "type": "parameter"
        }},
        {{
          "distribution": {{
            "parameters": {{
              "value": 2
            }},
            "type": "Constant"
          }},
          "referenceId": "I0",
          "source": "Page 0",
          "type": "parameter"
        }},
        {{
          "distribution": {{
            "parameters": {{
              "value": 0
            }},
            "type": "Constant"
          }},
          "referenceId": "R0",
          "source": "Page 0",
          "type": "parameter"
        }}
      ]
    }}
  ]
}}
"""
