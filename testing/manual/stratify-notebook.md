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

1. Make sure you have a [model](https://github.com/DARPA-ASKEM/terarium/blob/main/testing/data/SIR%20(relative)%20+%20Obs.json) in your project.
2. Create a workflow.
3. Drop in your model in the workflow.
4. Create a `Stratify model` operator.
5. Connect the model to the input model port of the `Stratify model` operator.
6. Click on `Edit`.
7. Click on `Notebook` tab.

### 3. Test Stratify LLM Responses

Enter the following prompts one at a time, click on the "airplane", wait for the response, check if the response matches the code block, and move on to the next.

1. Stratify my model by the ages young, middle, and old
```python
model = stratify(
    template_model=model,
    key= "Age",
    strata=['young', 'middle', 'old'],
    structure= [],
    directed=False, # Should the reverse direction conversions be added based on the given structure?
    cartesian_control=False, # True if populations of different strata can interact, False otherwise
    modify_names=True,
    concepts_to_stratify=None,
    params_to_stratify= None,
    concepts_to_preserve = None,
    params_to_preserve = None,
    param_renaming_uses_strata_names = True
)
```

2. Stratify my model by the ages young, middle, and old. Assume that the age groups cannot interact.
```python
model = stratify(
    template_model=model,
    key= "Age",
    strata=['young', 'middle', 'old'],
    structure= [],
    directed=False, # Should the reverse direction conversions be added based on the given structure?
    cartesian_control=False, # True if populations of different strata can interact, False otherwise
    modify_names=True,
    concepts_to_stratify=None, 
    params_to_stratify= None, 
    concepts_to_preserve = None,
    params_to_preserve = None,
    param_renaming_uses_strata_names = True
)
```

3. Stratify my model by the ages young and old where young and old can become old but old cannot become young
```python
model = stratify(
    template_model=model,
    key= "Age",
    strata=['young', 'old'],
    structure= [['young', 'old']],
    directed=True, # Should the reverse direction conversions be added based on the given structure?
    cartesian_control=False, # True if populations of different strata can interact, False otherwise
    modify_names=True,
    concepts_to_stratify=None, 
    params_to_stratify= None, 
    concepts_to_preserve = None,
    params_to_preserve = None,
    param_renaming_uses_strata_names = True
)
```

4. Stratify my model by the locations Toronto, Montreal, Vancouver, Yellowknife where all populations can interact with each other
```python
model = stratify(
    template_model=model,
    key= "Location",
    strata=['Toronto', 'Montreal', 'Vancouver', 'Yellowknife'],
    structure= None,
    directed=False, # Should the reverse direction conversions be added based on the given structure?
    cartesian_control=True,
    modify_names=True,
    concepts_to_stratify=None, 
    params_to_stratify= None, 
    concepts_to_preserve = None,
    params_to_preserve = None,
    param_renaming_uses_strata_names = True
)
```

5. Stratify my model by the locations Toronto, Montreal, Vancouver, Yellowknife where Montreal population can interact with Toronto population but not vice versa
```python
model = stratify(
    template_model=model,
    key= "Location",
    strata=['Toronto', 'Montreal', 'Vancouver', 'Yellowknife'],
    structure= [['Montreal', 'Toronto']],
    directed=True, # Should the reverse direction conversions be added based on the given structure?
    cartesian_control=False,
    modify_names=True,
    concepts_to_stratify=None, 
    params_to_stratify= None, 
    concepts_to_preserve = None,
    params_to_preserve = None,
    param_renaming_uses_strata_names = True
)
```  

7. 	Stratify my model by the locations Toronto, Montreal, Vancouver, Yellowknife where Montreal population can interact with Toronto population but not vice versa. Only stratify the "S" state and "γ" parameter.
```python
model = stratify(
    template_model=model,
    key= "Location",
    strata=['Toronto', 'Montreal', 'Vancouver', 'Yellowknife'],
    structure= [['Montreal', 'Toronto']],
    directed=True, # Should the reverse direction conversions be added based on the given structure?
    cartesian_control=False,
    modify_names=True,
    concepts_to_stratify=['S'], 
    params_to_stratify=['γ'], 
    concepts_to_preserve = None,
    params_to_preserve = None,
    param_renaming_uses_strata_names = True
)
```

8. Stratify my model by the locations Toronto, Montreal, Vancouver, Yellowknife where Montreal population can interact with Toronto population but not vice versa. Do not stratify the "I" state and "β" parameter.
```python
model = stratify(
    template_model=model,
    key= "Location",
    strata=['Toronto', 'Montreal', 'Vancouver', 'Yellowknife'],
    structure= [['Montreal', 'Toronto']],
    directed=True, # Should the reverse direction conversions be added based on the given structure?
    cartesian_control=False,
    modify_names=True,
    concepts_to_stratify=None, 
    params_to_stratify=None, 
    concepts_to_preserve = ['I'],
    params_to_preserve = ['β'],
    param_renaming_uses_strata_names = True
)
```

9. What is cartesian_control in stratify?

This should have no code response, just a message in the thought section:
> What is cartesian_control in stratify?
In the context of stratifying a model, 'cartesian_control' refers to whether strata from different state variables can interact with each other. If set to true, it means that populations in different strata (e.g., different locations or age groups) can interact across those strata. For example, in an epidemiological model, if 'cartesian_control' is true, then the infected population in one stratum (like a city) can affect the susceptible population in another stratum. If set to false, populations in different strata do not interact directly through the model's transitions or controls.
