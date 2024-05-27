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
2. Create, or open, project named `Q&A Epi S1 [YYMMDD]`

### 2. Search and add model publication to project
1. Open the _data explorer_
2. Search and add to the project the following three model publications:
   - Model A: https://doi.org/10.1155/2024/7589509
   - Model B: https://doi.org/10.48550/arXiv.2401.10057
   - Model C: https://doi.org/10.1101/2023.08.30.555493
3. Wait for the notification that the publications have been added to the project.
4. Open the project and verify that the three publications are present as _Documents_.

### 3. [Another task name]
1. If a task might have multiple outcomes, please split them in two tasks.
2. No need to repeat all the step, just mention what needs to be done to be different.
3. Describe the expected outcome.

### 4. [About input file]
1. If the test requires input files, please provide them via links.
2. The files are stored in the `testing/data` folder.
3. Please keep the names of files simple.
4. If the files needs to be pre-made, provide it in full, do not write the steps to create it.
    - For example, to validate a model-configuration, do not write the steps to create an SIR model and configuring it.
    - Instead, provide the `SIR.json` file of the model pre-configured
    - Link the file to its path in the `testing/data` folder.
    - Please re-use existing files if possible.

### 4. End test
1. logout of the application 
