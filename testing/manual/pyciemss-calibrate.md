## [Name of the Test Scenario]
Please go through __every__ step of the test scenario.\
When blocked, an error, or a UI/UX anomaly occurs, please report which scenario and step to [\#askem-testing](https://unchartedsoftware.slack.com/archives/C06FGLXB2CE).

Estimated time to completion: [X] minutes

Note: sampling combinations in PyCIEMSS can result in numerical instability, when this happens, you can:
- Retry the simulation, or
- Fiddle with the parameter distribution ranges, make the intervals larger or smaller

### 1. Begin test
1. Login to https://app.staging.terarium.ai using the test account
    ```
    email: qa@test.io
    password: askem-quality-assurance
    ```
2. Create, or open, project named `QA [Your Name] [YYMMDD]`

### 2. Model setup
1. Use/upload a model from [google drive](https://drive.google.com/drive/folders/1hjxiggCkBCofjCQgf9gXZEHBLkBqaVwe)
2. Connect the model to a Model Configuration operation, create a configuration
3. Ensure that the model parameters (e.g. beta, gamma) have distribution ranges
4. Connect the model-configuration to a Calibrate-PyCIEMSS operation
5. Use/upload a dataset from [gogole drive](https://drive.google.com/drive/folders/1hjxiggCkBCofjCQgf9gXZEHBLkBqaVwe)
6. Connect the dataset to Calibrate-PyCIEMSS


### 3. Configure calibration mappings
1. Map dataset `t` to model `timestamp`
2. Map dataset `S_obs` to model `S`
3. Map dataset `I_obs` to model `I`
4. Map dataset `R_obs` to model `R`
5. Change number of samples to 10
6. Change solver iterations to 10
7. Run calibrate


### 4. Check results
1. Verify that there is a loss chart available showing the loss value over time
2. Verify that there is a simulate chart avaible showing sample projections from the calibrated parameters
3. Should be able to add/remoe simulate charts


### 5. End test
1. logout of the application 
