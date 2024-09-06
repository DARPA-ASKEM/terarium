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
1. Model [SIR.json](https://drive.google.com/file/d/1eXlvpBfMmhrfC0xUXfuz0s_19gi-Rird/view?usp=drive_link)

### 3. Create an intervention policy
1. Create a new workflow named `Intervention Policy`
2. Drag and drop _the Model_ onto the workflow
3. Add the operator `Create intervention policy` and connect the model to it

### 4. Create a static parameter criteria
1. Click `Open` on the `Create intervention policy` operator node to enter its drill down
2. Edit the default intervention card, name it `Static Parameter` and leave it as _Static_.
3. Set Parameter `beta` to value `3` starting at timestep `20`.
4. Click `+ Add`.
5. In the new fields, set value to `4` starting at timestep `30`.

### 5. Create a dynamic parameter criteria
1. Click `+ Add intervention`
2. Name it `Dynamic Parameter` and change it to _Dynamic_.
3. Set Parameter `beta` to `2.5` when `Susceptible` `decreases to below` the threshold of `1000`.

### 6. Create a static state criteria
1. Click `+ Add intervention`
2. Name it `Static State` and leave it as _Static_.
3. Set State `R` to value `1000` starting at timestep 50`.

### 7. Create a dynamic state criteria
1. Click `+ Add intervention`
2. Name it `Dynamic State` and change it to _Dynamic_.
3. Set State `I` to `500` when `R` `increases to above` the threshold of `1000`.

### 8. Save the intervention policy
1. Save the intervention policy
2. Check the preview of the intervention policy
   1. Does the AI assisted _description_ match the criteria?
   2. Are the _criteria_ visible in the chart?

### 9. Simulate with the intervention policy
1. Add the operator `Configure model` to the workflow
2. Connect the model to the operator
3. Add the operator `Simulate` to the workflow
4. Connect the _intervention policy_ and _model configuration_ to the Simulation operator
5. Run the simulation
6. Check the results
   1. Are the _intervention policy_ and _model configuration_ visible in the results?
   2. Are the _criteria_ visible in the chart?
