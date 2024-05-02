## Template Test Scenario
Please go through __every__ step of the test scenario.\
When blocked, an error, or a UI/UX anomaly occurs, please report which scenario and step to [\#askem-testing](https://unchartedsoftware.slack.com/archives/C06FGLXB2CE).

### 1. Begin test
1. Login to https://app.staging.terarium.ai using the test account
    ```
    email: qa@test.io
    password: askem-quality-assurance
    ```
2. Create, or open, project named `Q&A [Your Name] [YYMMDD]`

### 2. Task
1. Describe what the user is to be done to complete a task.
2. The user is expected to know what to do to and look for to complete the task.
3. The goal is to ensure that **no steps are missed**.
4. Describe the expected outcome.

### 3. Another task
1. If a task might have multiple outcomes, please split them in two tasks.
2. No need to repeat all the step, just mention what needs to be done to be different.
3. Describe the expected outcome.

### 4. Input files
1. If the test requires input files, please provide them via links.
2. The files are stored in the `testing/assets/[name-of-test]` folder.
3. Please keep the names of files simple.
4. If the files needs to be pre-made, provide it in full, do not write the steps to create it.
    - For example, to validate a model-configuration, do not write the steps to create an SIR model and configuring it.
    - Instead, provide the `SIR.json` file of the model pre-configured
    - Link the file to its path in the `testing/assets/[name-of-test]` folder.

### 4. End test
1. logout of the application 
