## [Name of the Test Scenario]
Please go through __every__ step of the test scenario.\
Report any issues into GitHub: [open an issue](https://github.com/DARPA-ASKEM/terarium/issues/new?assignees=&labels=bug%2C+Q%26A&template=qa-issue.md&title=%5BBUG%5D%3A+).

### 1. Begin test
1. Login to https://app.staging.terarium.ai using the test account
    ```
    email: qa@test.io
    password: askem-quality-assurance
    ```
2. Create, or open, project named `QA [Your Name]`

### 2. Test Stratify LLM Responses
1. Make sure you have a model in your project.
2. Create a workflow
    Drop in your model in the workflow
    Create a Stratify node
    Connect the model to the stratify node
3. Drill down into the Stratify node and go to the notebook section.

Ask the follow questions one at a time, wait for the response, check the response matches
reset the code block move on to the next.

Q) "Stratify my model by the ages young and old",
A)
model = stratify(
    template_model=model,
    key= "Age",
    strata=['young', 'old'],
    structure= [],
    directed=False,
    cartesian_control=False,
    modify_names=True,
    concepts_to_stratify=None, #If none given, will stratify all concepts.
	  params_to_stratify= None #If none given, will stratify all parameters.
)

Q) Stratify my model by the ages young and old where young can transition to old
A)
model = stratify(
    template_model=model,
    key= "Age",
    strata=['young', 'old'],
    structure= [['young', 'old']],
    directed=True,
    cartesian_control=False,
    modify_names=True,
    concepts_to_stratify=None, #If none given, will stratify all concepts.
	  params_to_stratify= None #If none given, will stratify all parameters.
)

Q) Stratify my model by the ages young and old where young and old can become old, but old cannot become young
A)
model = stratify(
    template_model=model,
    key= "Age",
    strata=['young', 'old'],
    structure= [['young', 'old']],
    directed=False,
    cartesian_control=False,
    modify_names=True
)

Q) Stratify my model by the locations Toronto and Montreal where Toronto and Montreal cannot interact
A)
model = stratify(
    template_model=model,
    key= "Location",
    strata=['Toronto', 'Montreal'],
    structure= [],
    directed=False,
    cartesian_control=False,
    modify_names=True,
    concepts_to_stratify=None, #If none given, will stratify all concepts.
	  params_to_stratify= None #If none given, will stratify all parameters.
)

Q) Stratify my model by the locations Toronto and Montreal where any population can travel between Toronto and Montreal
A)
model = stratify(
    template_model=model,
    key= "Location",
    strata=['Toronto', 'Montreal'],
    structure= [['Toronto', 'Montreal'], ['Montreal', 'Toronto']],
    directed=True,
    cartesian_control=True,
    modify_names=True,
    concepts_to_stratify=None, #If none given, will stratify all concepts.
	  params_to_stratify= None #If none given, will stratify all parameters.
)

OR
model = stratify(
    template_model=model,
    key= "Location",
    strata=['Toronto', 'Montreal'],
    structure= [['Toronto', 'Montreal'], ['Montreal', 'Toronto']],
    directed=false,
    cartesian_control=True,
    modify_names=True,
    concepts_to_stratify=None, #If none given, will stratify all concepts.
	  params_to_stratify= None #If none given, will stratify all parameters.
)

Q) What is cartesian_control in stratify?
A)
No code response, instead just a message in the thought section.
