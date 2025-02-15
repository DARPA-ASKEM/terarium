## Calibrate
Please go through __every__ step of the test scenario.\
Report any issues into GitHub: [open an issue](https://github.com/DARPA-ASKEM/terarium/issues/new?assignees=&labels=bug%2C+Q%26A&template=qa-issue.md&title=%5BBUG%5D%3A+).

### Note
sampling combinations in PyCIEMSS can result in numerical instability, when this happens, you can:
- Retry the simulation, or
- Fiddle with the parameter distribution ranges, make the intervals larger or smaller

### 1. Begin test
1. Login to https://app.staging.terarium.ai using the test account
    ```
    email: qa@test.io
    password: askem-quality-assurance
    ```
2. Create, or open, project named `QA [Your Name]`

### 2. Model setup
1. Create a new workflow
2. Use/upload a model from [Google Drive](https://drive.google.com/drive/folders/1hjxiggCkBCofjCQgf9gXZEHBLkBqaVwe)
3. Connect the model to a Model Configuration operation, create a configuration
4. Ensure that the model parameters (e.g. beta, gamma) are assigned to an uniform distribution with valid lower and upper bounds
5. To get a reasonable result, change the initial condition values of `S, I, R` to `1000, 2, 0` and the value of `beta` to `0.0002, 0.0008` (1/1000th of the default value to accommodate the non-normalized values of the state variables).
6. Connect the model-configuration to a Calibrate operation
7. Use/upload a dataset from [Google Drive](https://drive.google.com/drive/folders/1hjxiggCkBCofjCQgf9gXZEHBLkBqaVwe)
8. Connect the dataset to Calibrate

### 3. Configure calibration mappings
1. Map dataset `t` to model `timestamp`
2. Map dataset `I_obs` to model `I` (it is not necessary to map `S_obs, R_obs`)
3. Optionally change the number of samples and solver iterations to both 10 if you want a faster but worst run.
4. Run calibrate


### 4. Check results
1. Verify that there is a loss chart available while the process is running, showing the loss value as a function of solver iterations
2. Verify that there is a `Parameters chart` available. This should allow the user to select different model parameters (Eg beta, gamma) and will display a bar chart showing before and after calibration values for the selected parameters.
3. Verify that there is a `Variables chart` available, showing time-series plots for each model variable based on a Simulate run with the calibrate parameter values.
4. Verify the dataset columm to which a given model variable is mapped appear as a dotted line in the Variables chart of the model variable. This should have a legend with the name Observations
5. Verify that an `Error chart` is available. This should allow the user to select different calibrated variables. It will display the mean absolute error between the post-forcast run and the incoming dataset.
6. Should be able to add/remove charts by selecting or deselecting options.

## 5. Add intervention(s)
1. Create a new intervention node and connect the model to it.
2. Create one or many interventions. These can be static or dynamic intervention on a parameter or state.
3. Connect this to the calibration node. 
4. Rerun the calibration.
5. When verifying, you should be able to see your intervention in the output charts.
