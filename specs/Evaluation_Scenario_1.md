---
testspace:
title: 12-Month ASKEM Evaluation Scenario 1 Part 1
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

### Scenario
For this question, assume that a masking policy will go into place on April 15th, 2020

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

### Setup Steps
* Login into Terarium and create a new project

## [Part A]

### Scenario
For this question, assume that a masking policy will go into place on
April 15th, 2020. Starting from April 3rd, 2020, forecast the next four weeks of the pandemic (for cases,
hospitalizations, and deaths) assuming the following constant levels of masking compliance:
40%, 60%, and 80%. Assume that any person who complies with the masking policy is
wearing a surgical mask. How does compliance affect forecasted cases, hospitalizations, and
deaths?

### Task 1 - TA1 Search and Discovery Workflow

Find estimates on the
efficacy of surgical masks in preventing onward transmission of SARS-CoV-2 (preferred)
or comparable viral respiratory pathogens (e.g., MERS-CoV, SARS), including any
information about uncertainty in these estimates. The term surgical mask here refers to
the commonly available, disposable procedure mask, not an N95-type respirator. Find 3
credible documents that provide estimates and use your judgment to determine what
value (in the deterministic case) or distribution (in the probabilistic case) to use in your
forecasts

### Task 2 - TA2 Model Modification Workflow

Begin with an SEIRHD model with parameter
settings as described in the scenario description. Modify the model to include the ability
to implement a masking policy intervention with different compliance levels. Implement
this in the following three ways:
1. Introduce a modification term to β as described in Srivastav et. al. (DOI: 10.3934/mbe.2021010): (1−𝜖𝑚𝑐𝑚), where 𝑐𝑚 is the fraction of the population
that wear face masks correctly and consistently (i.e. comply with masking
policies), and 𝜖𝑚 is the efficacy of the masks themselves.  
2. Adjust the transmission rate following an intervention period and create a time-
varying β function, as shown in https://doi.org/10.3390/ijerph18179027  
3. Add compartments and transitions to represent mask wearing/non-mask
wearing populations. See examples for several similar options of representing
this addition; you may implement the update in a way that represents one or a
combination of these approaches, or has an otherwise analogous effect:
https://doi.org/10.1098/rspa.2020.0376,
https://doi.org/10.1016/j.idm.2020.04.001,
https://doi.org/10.1016/j.chaos.2020.110599,
https://doi.org/10.1177/0272989X211019029

### Task 3 - TA3 Simulation Workflow

For each of the mask model modifications in 1.a.ii,
create forecasts to show how changes in compliance affect cases, hospitalizations, and
deaths.  

## [teardown]

* Log any bugs or issues in [Github](https://github.com/DARPA-ASKEM/Terarium/issues)
* Remove any projects and resources created during the test 
* Update test to reflect any new intended behaviour 
