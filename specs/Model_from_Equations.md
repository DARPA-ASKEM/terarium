---
testspace:
title: Model from Equations
scenarios: # test different scenarios
  - name: basic
    paper: "[A SIR model assumption for the spread of COVID-19 in different communities](https://github.com/DARPA-ASKEM/knowledge-middleware/blob/main/tests/scenarios/basic/paper.pdf)"
    equations: "[equations](https://github.com/DARPA-ASKEM/knowledge-middleware/blob/main/tests/scenarios/basic/equations.txt)"
    data: "[csv](https://github.com/DARPA-ASKEM/knowledge-middleware/blob/main/tests/scenarios/basic/data.csv)"
  - name: sidarthe
    paper: "[Lack of practical identifiability may hamper reliable predictions in COVID-19 epidemic models](https://github.com/DARPA-ASKEM/knowledge-middleware/blob/main/tests/scenarios/sidarthe/paper.pdf)"
    equations: 
    data: 
---

# {{ spec.title }}
This manual test is designed to exercise the model-from-equations workflow. Users should aim to assess the speed, 
effectiveness and accuracy of building AMR models from various equations. This test involves the input of a diverse 
range of mathematical equations, spanning various domains such as epidemiology and climate change. Testers manually 
input these equations, ensuring that they cover a wide spectrum of complexity and mathematical expressions. The workflow
is evaluated on its ability to accurately parse, interpret, and generate meaningful outputs from these equations. 
Testers should also validate against real-world scenarios and edge cases.

## [setup]

* Login into Terarium and create a new project

| Scenario | Paper | Equations | Data |
|----------|-------|-----------|------|
{%- for state in spec.scenarios %}
| {{ state.name }} | {{ state.paper}} | {{state.equations }} | {{state.data }} |
{%- endfor %}

## [test manual equations]
1. Pick a scenario from the table above
2. Create a new blank model from Overview page and name it after the scenario
3. switch to the model editor tab and begin entering equations from the scenario (copy and paste laTeX)

{%- for state in spec.scenarios %}
   * {{ state.name }}: {{state.equations }}
{%- endfor %}

4. See new model diagram (should be a valid AMR)


## [test extracted equations]
1. Select image(s) of equations extracted from a paper

{%- for state in spec.scenarios %}
   * {{ state.name }}: {{state.paper }}
{%- endfor %}

2. Send to ‘Recreate model’ operator (image to AMR)
3. See equation image with interpretation in livemath editor to allow curation
4. Create model

5. See new model diagram (should be a valid AMR)
6. compare model diagrams with manually added equations and ensure they are the same

## [teardown]

* Log any bugs or issues in [Github](https://github.com/DARPA-ASKEM/Terarium/issues)
* Remove any projects and resources created during the test 
* Update test to reflect any new intended behaviour 
