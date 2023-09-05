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
1. Login into Terarium and create a new project

| Scenario | Paper | Equations | Data |
|----------|-------|-----------|------|
{%- for state in spec.scenarios %}
| {{ state.name }} | {{ state.paper}} | {{state.equations }} | {{state.data }} |
{%- endfor %}

## [test]
1. Create a new blank model from Overview page
2. Enter equations (copy and paste laTeX)

{%- for state in spec.scenarios %}
   * {{ state.name }}: {{state.equations }}
{%- endfor %}

4. See new model diagram (should be a valid AMR)

5. Select image(s) of equations extracted from a paper

{%- for state in spec.scenarios %}
   * {{ state.name }}: {{state.paper }}
{%- endfor %}

6. Send to ‘Recreate model’ operator (image to AMR)
7. See equation image with interpretation in livemath editor to allow curation
8. Create model

4. See new model diagram (should be a valid AMR)
5. compare model diagrams to ensure they are the same

## [teardown]
1. Log any bugs or issues in [Github](https://github.com/DARPA-ASKEM/Terarium/issues)
2. Remove any projects and resources created during the test
