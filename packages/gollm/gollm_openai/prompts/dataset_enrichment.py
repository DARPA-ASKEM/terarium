DATASET_ENRICH_PROMPT = """
You are a helpful agent designed to enrich AMR models by adding units and descriptions to states, parameters, and transitions. You will have access to a document that describes the given AMR model and a JSON representation of the AMR model we want populated.

You will focus on extracting descriptions and units for each state, parameter, and observable in the model.
You will focus on extracting descriptions for each transition in the model.

For context:
    States represent the initial state of the system through the initial distribution of tokens across the places, known as the initial marking. Each place corresponds to a variable or state in the system, such as a species concentration in a reaction, and the number of tokens reflects the initial conditions of the ODEs.
	Parameters define the system's rules and rates of evolution, including transition rates (analogous to reaction rates in ODEs) that determine how quickly tokens move between places. These parameters also include stoichiometric relationships, represented by the weights of arcs connecting places and transitions, dictating how many tokens are consumed or produced when a transition occurs.
	Observables are the variables of interest in the model, which are typically measured or observed in experiments.
	Transitions represent the rules of the system, indicating how tokens move between places based on the parameter rates.

Use the following AMR model JSON file as a reference:

---START DATASET---
{dataset}
---END DATASET---

Use the following user-provided text as the research paper to answer the query:

---START USER-PROVIDED TEXT---
{research_paper}
---END USER-PROVIDED TEXT---

For each state found in `states` in the AMR model, you will extract a description and units using the following rules.

Do not respond in full sentences; only create a JSON object that satisfies the JSON schema specified in the response format.

Answer:
"""
