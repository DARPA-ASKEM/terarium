## Ensemble Simulate
Models found here: https://drive.google.com/drive/u/0/folders/1sU3-CG9g9GBuTda_30lahiHAXu7U2-Xt

Please go through __every__ step of the test scenario.\
Report any issues into GitHub: [open an issue](https://github.com/DARPA-ASKEM/terarium/issues/new?assignees=&labels=bug%2C+Q%26A&template=qa-issue.md&title=%5BBUG%5D%3A+).

### 1. Begin test
1. Login to https://app.staging.terarium.ai using the test account
    ```
    email: qa@test.io
    password: askem-quality-assurance
    ```
2. Create, or open, project named `QA [Your Name]`

### 2. Workflow
1. Create a new workflow named `Ensemble Simulate`
2. Upload `Giordano2020 - SIDARTHE model of COVID-19 spread in Italy` and `Test SIR` into your project
3. Drag and drop both models into your workflow
4. Link both models to individual  _Configure Model_ operators
6. Save each configuration by giving it a name and clicking `Run`

### 3. Ensemble Simulate Operator setup
1. Link both configurations to a new `Simulate ensemble` node
2. Open the `Simulate ensemble` node by clicking on edit
3. Add the following mappings titling the ensemble variable
  EnsembleVariableOne  `SIR's` S with `Giordano2020`'s Susceptible
  EnsembleVariableTwo  `SIR's` I with `Giordano2020`'s Cases
  EnsembleVariableThree  `SIR's` R with `Giordano2020`'s Healed
4. Hit `Run`

### Validation:
Run should complete (Very quickly)
you should see the following options in your chart's dropdown:
  EnsembleVariableOne
  EnsembleVariableTwo
  EnsembleVariableThree
  (or whatever arbitrary name you gave your mappings)

### Misc:
You should be able to add mapping as well as delete mapping. close and open the drilldown and see saved changes.
You should be able to set weights to whatever you like as well as hit the `Set weights to be equal` to reset them.
You should be able to utilize the `Stop` button to stop the run. In order to do this is may help to increase the
defaults to make a longer lasting run so you dont need to rely on reaction time

Once complete:
You should be able to add and delete charts
You should see changes to the select on the charts duplicated in the node as well as the drilldown views
You should be able to save the dataset
