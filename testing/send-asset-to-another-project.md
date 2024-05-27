## Send asset to another project
Please go through __every__ step of the test scenario.\
When blocked, an error, or a UI/UX anomaly occurs, please report which scenario and step to [\#askem-testing](https://unchartedsoftware.slack.com/archives/C06FGLXB2CE).

### 1. Begin test
1. Login to https://app.staging.terarium.ai using the test account
    ```
    email: qa@test.io
    password: askem-quality-assurance
    ```
2. Create, or open, project named `QA [Your Name] [YYMMDD]`

### 2. Upload a PDF
1. Upload _the Document_ [SIR.pdf](data/SIR.pdf) using `Upload Resources` button in the bottom left.
2. Wait for final notification of document extraction
3. Open _the Document_ and check that extractions are visible

### 3. Send the PDF to another project
1. Click _the Document_.
2. In the top middle `...` menu select `Add to project`
3. Choose a project to add the file to.
4. Select the other project.
5. Check that the file has been added successfully.

### 4. End test
1. logout of the application
