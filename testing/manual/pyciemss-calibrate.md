## [Name of the Test Scenario]
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
5. Connect the model-configuration to a Calibrate-PyCIEMSS operation
6. Use/upload a dataset from [Google Drive](https://drive.google.com/drive/folders/1hjxiggCkBCofjCQgf9gXZEHBLkBqaVwe)
7. Connect the dataset to Calibrate-PyCIEMSS


### 3. Configure calibration mappings
1. Map dataset `t` to model `timestamp`
2. Map dataset `S_obs` to model `S`
3. Map dataset `I_obs` to model `I`
4. Map dataset `R_obs` to model `R`
5. Change number of samples to 10
6. Change solver iterations to 10
7. Run calibrate


### 4. Check results
1. Verify that there is a loss chart available, showing the loss value as a function of solver iterations
2. Verify that there is a Variables chart available, showing time-series plots for each model variable based on a Simulate run with the calibrate parameter values.
3. Should be able to add/remove Variables charts
