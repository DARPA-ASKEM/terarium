## Ensemble Calibrate
model and dataset found here: https://drive.google.com/drive/u/0/folders/1sU3-CG9g9GBuTda_30lahiHAXu7U2-Xt

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
1. Create a new workflow named `Ensemble Calibrate`
2. Upload `Ensemble Calibrate Test Dataset.csv` and `Giordano2020 - SIDARTHE model of COVID-19 spread in Italy.json` and `SIR.json` into your project
3. Drag and drop the models into your workflow
4. Link each model to their own _Configure Model_ operators
6. Use the `default configurations` for these models.
7. Drag and drop the dataset into the workflow

### 3. Ensemble Calibrate Operator setup
1. Create a Calibrate Ensemble operator by right-clicking on the workflow canvas and clicking `Work with multiple models/Calibrate ensemble`
2. Link the dataset to the "Dataset" port of the operator configuration
3. Link the Giordano model configuration into the "Model configuration" port of the operator
4. Repeat for the SIR model configuration
5. Open the `Calibrate ensemble` node by clicking on edit
6. Set the mapping `Dataset timestamp column` to the `timepoint_id` column
7. Set the other mappings as follows:
    Find the `Susceptible_state` in the ensemble variables dropdown, select it and hit add mapping.
      Link the `Susceptible` for both of your identical configurations
8. Click "Run" to start the calibration.

### Misc:
You should be able to add mapping as well as delete mapping. close and open the drilldown and see saved changes.
You should be able to set weights to whatever you like as well as hit the `Set weights to be equal` to reset them.
You should be able to utilize the `Stop` button to stop the run. In order to do this is may help to increase the
defaults to make a longer lasting run so you dont need to rely on reaction time

Once complete:
You should be able to add and delete charts
You should see changes to the select on the charts duplicated in the node as well as the drilldown views
You should be able to save the dataset
