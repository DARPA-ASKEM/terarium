## Epi S1 - Task 1 Model Extraction

Please go through __every__ step of the test scenario.\
When blocked, an error, or a UI/UX anomaly occurs, please report which scenario and step to [\#askem-testing](https://unchartedsoftware.slack.com/archives/C06FGLXB2CE).

Estimated time to completion: [X] minutes

Source for the scenario: [PDF](https://github.com/DARPA-ASKEM/program-milestones/blob/main/18-month-milestone/evaluation/Epi%20Use%20Case/ASKEM_18Month_Epi_Evaluation_Scenarios_03.22.2024_FINAL.pdf)

### 1. Begin test
1. Login to https://app.staging.terarium.ai using the test account
    ```
    email: qa@test.io
    password: askem-quality-assurance
    ```
2. Create, or open, project named `QA Epi S1`

### 2. Ingesting the model from source paper or code, into the workbench
1. Upload the three PDF documents to the project: [Model A](https://drive.google.com/file/d/1118EP5XH7xysXJueCPhtza03Z-WHkTYe/view?usp=drive_link), [Model B](https://drive.google.com/file/d/13lpv-ddAzg5Sjz43hX12TJ58GrN8cJwL/view?usp=drive_link), and [Model C](https://drive.google.com/file/d/1Ij0KxffUovIxRBjMFDP5PQQfVWLWbg3J/view?usp=drive_link)
2. Wait for the notification that the publications have been added to the project.
3. Open the project and verify that the three publications are present as _Documents_.

### 3. Capturing the set of equations describing the model in the workbench
1. Open a new _Workflow_ and name it `Task 1 Model Extraction`.
2. For each of the documents:
    1. Open the document.
    2. Select the equations from the text.
    3. Connect the equations to a _Create model from equations_ node.
    4. Edit the node, select the equations and creat the model.
    5. Save the model.

### 4. Gathering definitions of all variables and parameters, with units
1. Add a _Configure model_ node and link the _Model A_.
2. Link the _Document_ used to create the model. 
3. Open the _Configure model_ node and apply the default configuration.
4. Verify that the variables are extracted and have the correct units.
5. Verify that default values for parameters, initial values are present.
6. Repeat the process for _Model B_ and _Model C_.

### 5. Ensuring the model is executable in the workbench
1. Add a _Simulate with PyCIEMSS_ node and link the _Model A_.
2. Open the _Simulate with PyCIEMSS_ node and `Run` the simulation.
3. Verify that the simulation runs without errors.
4. Repeat the process for _Model B_ and _Model C_.

### 6. End test
1. logout of the application 
