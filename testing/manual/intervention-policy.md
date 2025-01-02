## Intervention Policy
Please go through __every__ step of the test scenario.\
Report any issues into GitHub: [open an issue](https://github.com/DARPA-ASKEM/terarium/issues/new?assignees=&labels=bug%2C+Q%26A&template=qa-issue.md&title=%5BBUG%5D%3A+).

### 1. Begin test
1. Login to https://app.staging.terarium.ai using the test account
    ```
    email: qa@test.io
    password: askem-quality-assurance
    ```
2. Create, or open, project named `QA [Your Name]`

### 2. Upload asset
1. Model [testing/data/Configured SIR.json](https://github.com/DARPA-ASKEM/terarium/blob/main/testing/data/Configured%20SIR.json)

### 3. Create an intervention policy
1. Create a new workflow named `Intervention Policy`
2. Drag and drop _the Model_ onto the workflow
3. Add the operator `Create intervention policy` and connect the model to it

### 4. Create a static parameter criteria
1. Click `Open` on the `Create intervention policy` operator node to enter its drill down
2. Edit the default intervention card, name it `Static Parameters` and leave it as _Static_.
3. Set Parameter `beta` to value `0.0001` starting at timestep `20`.
4. Click `+ Add`.
5. Set the Parameter gamma to `0.5`
6. Check that the charts reflect the intervention

### 5. Try to create an invalid intervention
1. Create an intervention that has a negative timestamp within a static intervention.
      Expect to see a toaster error explaining where you have an invalid intervention
2. Create an intervention that contains two static interventions each with the same parameter and time selected
      Expect to see a toaster error explaining you have duplicates.
3. Create two interventions each containing at least one static intervention that has the same parameter and time selected
      Expect to see a toaster error explaining you have duplicates.

### 6. Create a dynamic parameter criteria
1. Click `+ Add intervention`
2. Name it `Dynamic Parameter` and change it to _Dynamic_.
3. Set Parameter `beta` to `0.0009` when `Susceptible` crosses the threshold of `700`.

### 7. Create a static state criteria
1. Click `+ Add intervention`
2. Name it `Static State` and leave it as _Static_.
3. Set State `I` to value `600` starting at timestep `40`.

### 8. Create a dynamic state criteria
1. Click `+ Add intervention`
2. Name it `Dynamic State` and change it to _Dynamic_.
3. Set State `I` to `100` when `S` crosses the threshold of `800`.
4. Save the intervention policy
  
### 9. Create a New intervention policy
1. Click 'Create New' button. You should see a blank intervention card.
2. Click 'Save' or 'Save As' without filling any of the details. (Currently that is not allowed so you should see a msg that the save failed.)
3. Select the intervention policy created in step 8.

### 10. Simulate with the intervention policy
1. Add the operator `Configure model` to the workflow
2. Connect the model to the operator
3. Add the operator `Simulate` to the workflow
4. Connect the _intervention policy_ and _model configuration_ to the Simulation operator
5. Run the simulation
6. Check the results
   1. Are the _intervention policy_ and _model configuration_ visible in the results?
   2. Are the _criteria_ visible in the chart?
