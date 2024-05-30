## Ensemble Calibrate
model and dataset found here: https://drive.google.com/drive/u/0/folders/1sU3-CG9g9GBuTda_30lahiHAXu7U2-Xt
Please go through __every__ step of the test scenario.\
When blocked, an error, or a UI/UX anomaly occurs, please report which scenario and step to [\#askem-testing](https://unchartedsoftware.slack.com/archives/C06FGLXB2CE).

Estimated time to completion: [X] minutes

### 1. Begin test
1. Login to https://app.staging.terarium.ai using the test account
    ```
    email: qa@test.io
    password: askem-quality-assurance
    ```
2. Create, or open, project named `QA [Your Name]`

### 2. Workflow
1. Create a new workflow named `Ensemble Calibrate`
2. Upload `Ensemble Calibrate Test Dataset` and `Giordano2020 - SIDARTHE model of COVID-19 spread in Italy` into your project
3. Drag and drop the model into your workflow
4. Link both models to individual  _Configure Model_ operators 
6. Save each configuration by giving it a name and clicking `Run`
7. drag and drop the dataset into the workflow.

### 3. Ensemble Calibrate Operator setup
1. Link the configuration into the node twice and add the dataset into a new `Calibrate ensemble` node
2. Open the `Calibrate ensemble` node by clicking on edit
3. Set the mapping `Dataset timestamp column` to the `timepoint_id` column
4. Set the other mappings as follows:
    Find the `Susceptible_state` in the ensemble variables dropdown, select it and hit add mapping.
      Link the `Susceptible` for both of your identical configurations

### 5. End test
1. logout of the application 

### Misc:
You should be able to add mapping as well as delete mapping. close and open the drilldown and see saved changes.
You should be able to set weights to whatever you like as well as hit the `Set weights to be equal` to reset them.
You should be able to utilize the `Stop` button to stop the run. In order to do this is may help to increase the 
defaults to make a longer lasting run so you dont need to rely on reaction time

Once complete:
You should be able to add and delete charts
You should see changes to the select on the charts duplicated in the node as well as the drilldown views
You should be able to save the dataset 

