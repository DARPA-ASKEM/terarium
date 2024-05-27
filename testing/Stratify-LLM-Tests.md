## [Name of the Test Scenario]
Please go through __every__ step of the test scenario.\
When blocked, an error, or a UI/UX anomaly occurs, please report which scenario and step to [\#askem-testing](https://unchartedsoftware.slack.com/archives/C06FGLXB2CE).

### 1. Begin test
1. Login to https://app.staging.terarium.ai using the test account
    ```
    email: qa@test.io
    password: askem-quality-assurance
    ```
2. Create, or open, project named `QA [Your Name] [YYMMDD]`

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
    modify_names=True
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
    modify_names=True
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
    modify_names=True
)

Q) Stratify my model by the locations Toronto and Montreal where Toronto and Montreal can interact
A) 
model = stratify(
    template_model=model,
    key= "Location",
    strata=['Toronto', 'Montreal'],
    structure= [['Toronto', 'Montreal'], ['Montreal', 'Toronto']],
    directed=False,
    cartesian_control=True,
    modify_names=True
)

OR
model = stratify(
    template_model=model,
    key= "Location",
    strata=['Toronto', 'Montreal'],
    structure= [['Toronto', 'Montreal']],
    directed=True,
    cartesian_control=True,
    modify_names=True
)

Q) What is cartesian_control in stratify?
A)
No code response, instead just a message in the thought section.


### 4. End test
1. logout of the application 
