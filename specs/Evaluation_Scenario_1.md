---
testspace:
title: 12-Month ASKEM Evaluation Scenario 1
model:
  name: SEIHRD
  paper: "[A SIR model assumption for the spread of COVID-19 in different communities](https://github.com/DARPA-ASKEM/knowledge-middleware/blob/main/tests/scenarios/basic/paper.pdf)"
  equations: "[equations](https://github.com/DARPA-ASKEM/knowledge-middleware/blob/main/tests/scenarios/basic/equations.txt)"
  data: "[csv](https://github.com/DARPA-ASKEM/knowledge-middleware/blob/main/tests/scenarios/basic/data.csv)"
---

# {{ spec.title }}

### Background
In April 2020, COVID-19 has spread from small clusters of imported cases to more
widespread community transmission. Preventive measures are limited to non-pharmaceutical
interventions such as social distancing, isolation of infected individuals, and other forms of
source control such as masking. We are interested in determining how masking efficacy and
compliance can help reduce cases, hospitalizations, and deaths in New York.
### Timepoint
April 3rd, 2020

### Location
New York State

## [setup]

### Model
| Model                 | Paper                 | Equations                 | Data                 |
|-----------------------|-----------------------|---------------------------|----------------------|
| {{ spec.model.name }} | {{ spec.model.paper}} | {{spec.model.equations }} | {{spec.model.data }} |

### Parameters

Base parameter values for the model (prior to modification tasks in the
scenario question)

* 𝛽 = 0.4 𝑛𝑒𝑤 𝑖𝑛𝑓𝑒𝑐𝑡𝑖𝑜𝑛𝑠 𝑝𝑒𝑟 𝑖𝑛𝑓𝑒𝑐𝑡𝑒𝑑 𝑝𝑒𝑟𝑠𝑜𝑛/day
* 𝑟(𝐼 → 𝑅) = 0.07/𝑑𝑎𝑦
* 𝑟(𝐼 → 𝐻) = 0.1/𝑑𝑎𝑦
* 𝑟(𝐸 → 𝐼) = 0.2/𝑑𝑎𝑦
* 𝑟(𝐻 → 𝑅) = 0.1/𝑑𝑎𝑦
* 𝑟(𝐻 → 𝐷)= 0.1/𝑑𝑎𝑦
* 𝑝(𝐼 → 𝑅)= 0.8
* 𝑝(𝐼 → 𝐻)= 0.2
* 𝑝(𝐻 → 𝑅)= 0.88
* 𝑝(𝐻 → 𝐷)= 0.12
* Use N = 19.34  million (approximate population size for NY state in 2020)
* For 𝐼(0) 𝑎𝑛𝑑 𝐷(0) please pull values from the gold standard cases and
deaths data from the Covid-19 ForecastHub. 
* For 𝐻(0) use HHS hospitalization data from
https://healthdata.gov/Hospital/COVID-19-Reported-Patient-Impact-and-Hospital-Capa/g62h-syeh
* Let 𝑅(0) = 𝑐𝑢𝑚𝑢𝑙𝑎𝑡𝑖𝑣𝑒 𝑖𝑛𝑓𝑒𝑐𝑡𝑖𝑜𝑛𝑠 – 𝑐𝑢𝑚𝑢𝑙𝑎𝑡𝑖𝑣𝑒 𝑑𝑒𝑎𝑡ℎ𝑠, as of April 3rd, 2020
* Let 𝐸(0) = 𝐼(0)/4
* Let 𝑆(0) = 𝑁 – 𝐸(0) − 𝐼(0)−𝑅(0)−𝐻(0)−𝐷(0)

### Steps to begin
* Login into Terarium and create a new project

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
