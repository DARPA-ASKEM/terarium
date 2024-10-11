## Stratify model via the notebook interface
Please go through __every__ step of the test scenario.\
Report any issues into GitHub: [open an issue](https://github.com/DARPA-ASKEM/terarium/issues/new?assignees=&labels=bug%2C+Q%26A&template=qa-issue.md&title=%5BBUG%5D%3A+).

### 1. Begin test
1. Login to https://app.staging.terarium.ai using the test account
    ```
    email: qa@test.io
    password: askem-quality-assurance
    ```
2. Create, or open, project named `QA [Your Name]`

### 2. Setup workflow
1. Make sure you have a model in your project.
2. Create a workflow
3. Drop in your model in the workflow
4. Create a Stratify node
5. Connect the model to the stratify node
6. Drill down into the Stratify node and go to the notebook section.

### 3. Test Stratify LLM Responses
Enter the following prompts one at a time, wait for the response, check if the response matches the code block move on to the next.

1. Stratify my model by the ages young and old
```
model = stratify(
    template_model=model,
    key= "Age",
    strata=['young', 'old'],
    structure= [],
    directed=False,
    cartesian_control=False,
    modify_names=True
)
```

2. Stratify my model by the ages young and old where young can transition to old
```
model = stratify(
    template_model=model,
    key= "Age",
    strata=['young', 'old'],
    structure= [['young', 'old']],
    directed=True,
    cartesian_control=False,
    modify_names=True
)
```

3. Stratify my model by the ages young and old where young and old can become old, but old cannot become young
```
model = stratify(
    template_model=model,
    key= "Age",
    strata=['young', 'old'],
    structure= [['young', 'old']],
    directed=False,
    cartesian_control=False,
    modify_names=True
)
```
4. Stratify my model by the locations Toronto and Montreal where Toronto and Montreal cannot interact
```
model = stratify(
    template_model=model,
    key= "Location",
    strata=['Toronto', 'Montreal'],
    structure= [],
    directed=False,
    cartesian_control=False,
    modify_names=True
)
```

5. 	Stratify my model by the locations Toronto and Montreal where populations within any state are able to travel between Toronto and Montreal
```
model = stratify(
    template_model=model,
    key= "Location",
    strata=['Toronto', 'Montreal'],
    structure= [['Toronto', 'Montreal'], ['Montreal', 'Toronto']],
    directed=False,
    cartesian_control=True,
    modify_names=True
)
```

OR
```
model = stratify(
    template_model=model,
    key= "Location",
    strata=['Toronto', 'Montreal'],
    structure= [['Toronto', 'Montreal']],
    directed=True,
    cartesian_control=True,
    modify_names=True
)
```

6. What is cartesian_control in stratify?
- This should have no code response, just a message in the thought section.
