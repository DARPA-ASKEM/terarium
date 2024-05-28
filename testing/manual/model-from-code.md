## Create model from code
Please go through __every__ step of the test scenario.\
When blocked, an error, or a UI/UX anomaly occurs, please report which scenario and step to [\#askem-testing](https://unchartedsoftware.slack.com/archives/C06FGLXB2CE).

Estimated time to completion: [X] minutes

### 1. Begin test
1. Login to https://app.staging.terarium.ai using the test account
    ```
    email: qa@test.io
    password: askem-quality-assurance
    ```
2. Create, or open, project named `QA [Your Name] [YYMMDD]`

### 2. Upload code
1. Upload the [code file](https://drive.google.com/file/d/1k3tmV4Az5blrso2BXqYN1rqL3S-knmSm/view?usp=drive_link) to the project.
2. Wait for the notification that the code file has been added to the project.

### 3. Create model
1. Open a new _Workflow_ and name it `Create model from code`.
2. Add the code object to the workflow.
3. Edit the node and select the whole code and create a new _code block_.
4. Add a _Create model from code_ node to the workflow.
5. Edit the node, select the code block and create the model.
6. Save the model.

### 3. End test
1. logout of the application
