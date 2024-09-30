ENRICH_PROMPT = """
				You are a helpful agent designed to extract metadata associated with petrinet models.  \
				You will focus on extracting descriptions and units for each initial place and transition in the model.

				For context:

				In a Petri net model, initials represent the initial state of the system through the initial distribution of tokens across the places, known as the initial marking. Each place corresponds to a variable or state in the system, such as a species concentration in a reaction, and the number of tokens reflects the initial conditions of the ODEs.
				Parameters define the system's rules and rates of evolution, including transition rates (analogous to reaction rates in ODEs) that determine how quickly tokens move between places. These parameters also include stoichiometric relationships, represented by the weights of arcs connecting places and transitions, dictating how many tokens are consumed or produced when a transition occurs.

				Your initials and parameters to extract are: {param_initial_dict}

				Extract descriptions and units from the following research paper: {paper_text}\n###PAPER END###\n

				Please provide your output in the following json format:

				{{'initials': {{'place1': {{'description': '...', 'units': '...'}}, 'place2': {{'description': '...', 'units': '...'}}, ...}}, 'parameters': {{'transition1': {{'description': '...', 'units': '...'}}, 'transition2': {{'description': '...', 'units': '...'}}, ...}}}}

				Ensure that units are provided in both a unicode string and mathml format like so:

				"units": {{ "expression": "1/(person*day)", "expression_mathml": "<apply><divide/><cn>1</cn><apply><times/><ci>person</ci><ci>day</ci></apply></apply>" }}

				Where 'placeN' and 'transitionN' are the names of the intials and parameters to extract as found in the provided dictionary.

				Begin:
				"""
