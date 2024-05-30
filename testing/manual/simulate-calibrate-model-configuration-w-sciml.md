## [Name of the Test Scenario]
Please go through __every__ step of the test scenario.\
When blocked, an error, or a UI/UX anomaly occurs, please report which scenario and step to [\#askem-testing](https://unchartedsoftware.slack.com/archives/C06FGLXB2CE).

Estimated time to completion: [X] minutes

### 1. Begin test

1. Login to [https://app.staging.terarium.ai](https://app.staging.terarium.ai) using the test account

    ```bash
    email: qa@test.io
    password: askem-quality-assurance
    ```

2. Create, or open, project named `QA [Your Name]`

### 2. Upload test model

1. Click `Upload resources`.
2. Drag and drop `Configured SIR.json` file over the text "Drop resources here".
3. Click `Upload` and wait for the file upload to finish.
4. Repeat with the `data-1.csv` file.
5. A model named "Configured SIR" and a dataset named "data-1" should appear in the "Resources" panel.

### 3. Create a new workflow

1. Click `+ New` under the "Workflow" section of "Resources".

### 4. Use the SciML Simulate operator

1. Right-click on the workflow canvas and select `Add Resources/Model`.
2. Select "Configured SIR" in the dropdown of the `Model` operator.
3. Right-click on the workflow canvas and select `Work with model/Configure model`.
4. Connect the `Configure SIR` output port of the `Model` operator to the `Model` input port of the `Configure model` operator.
5. Click `Open` on the `Configure model` operator.
6. There should be a configuration named "Default config" in the "Suggested configurations" section.
7. Click `Apply configuration values`.
8. The "Value" fields of the "Initial variable values" and "Parameters" tables should be auto-filled with some values.
9. Click `Run` to generate a model configuration.
10. There should be a configuration named "Default config" at the output port.
11. Right-click on the workflow canvas and select `Run model/Simulate with SciML`.
12. Connect the `Default config` output port of the `Configure model` operator to the `Model` input port of the `Simulate with SciML` operator.
13. Click `Edit` on the `Simulate with SciML` operator to set up the simulation.
14. Verify that the simulation parameters are sensible (`Start time = 1, End time = 100`).
15. Click `Run` and wait for the simulation to finish running.
16. The right panel should show time series data, one for each model variable.

### 5. Use the SciML Calibrate operator

1. Right-click on the workflow canvas and select `Add Resources/Dataset`.
2. Select "data-1" in the dropdown of the `Dataset` operator.
3. Right-click on the workflow canvas and select `Run model/Calibrate model with SciML`.
4. Connect the `data-1` output port of the `Dataset` operator to the `Dataset` input port of the `Calibrate model with SciML` operator.
5. Connect the `Default config` output port of the `Configure model` operator (see 4.12) to the `Model` input port of the `Calibrate model with SciML` operator.
6. Click `Edit` on the `Calibrate model with SciML` operator.
7. Provide a mapping between the model's variables and the dataset's columns by clicking `+ Add mapping` and making selections in the dropdown as follows:
| Model variable | Dataset variable |
| -------------- | ---------------- |
| S | S_obs |
| I | I_obs |
| R | R_obs |
| timestamp | t |

8. Click `Run`.
9. As the calibration runs, the "Loss function" section on the right panel should have a chart showing a history of the loss. Expect it to be a timeseries that decreases quickly then slowly.
10. When the calibration is completed successfully, there should be a chart showing the predicted values for each model variable and the dataset values to which it has been mapped.

### 4. End test

1. Click on "QA" on the top right of the screen.
2. Click "Logout" to log out of the application.
