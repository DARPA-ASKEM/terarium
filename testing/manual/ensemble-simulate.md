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
5. Ensure that both configurations are the same scale. AKA both should have Susceptible and S ~= 1.

### 3. Ensemble Simulate Operator setup
1. Link both configurations to a new `Simulate ensemble` node
2. Open the `Simulate ensemble` node by clicking on edit
3. Add the following mappings titling the ensemble variable
  `Sus`  SIR's `S` with Giordano2020's `Susceptible`
  `Inf`  SIR's `I` with Giordano2020's `Cases`
4. Hit `Run`

### Validation:
Run should complete 
you should see the following options in your chart's dropdown:
  `Sus`
  `Inf`
  (or whatever arbitrary name you gave your mappings)

### Misc:
You should be able to add mapping as well as delete mapping. close and open the drilldown and see saved changes.
You should be able to set weights to a number between 1 and 10. The signal bars should line up with your selection.
You should be able to utilize the `Stop` button to stop the run. In order to do this is may help to increase the
defaults to make a longer lasting run so you dont need to rely on reaction time

# Once complete: (Old charts)
You should be able to add and delete charts
You should be able to plot your mapped variables `Sus` along with model_#/`Susceptible` and `model_#/Cases` (and whatever other vars you want to investigate)
You should be able to save the dataset using the `Save for reuse` button in the output panel.
You should be able to plug this node into a `tranform dataset` node
