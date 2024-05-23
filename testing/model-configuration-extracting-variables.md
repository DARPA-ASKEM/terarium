## Extract configuations from document and dataset
Please go through __every__ step of the test scenario.\
When blocked, an error, or a UI/UX anomaly occurs, please report which scenario and step to [\#askem-testing](https://unchartedsoftware.slack.com/archives/C06FGLXB2CE).

Estimated time to completion: [X] minutes

### 1. Begin test
1. Login to https://app.staging.terarium.ai using the test account
    ```
    email: qa@test.io
    password: askem-quality-assurance
    ```
2. Create, or open, project named `Q&A [Your Name] [YYMMDD]`
3. Upload model `SIR.json`
4. Upload or reuse document `41591_2020_Article_883.pdf`, if extraction are not working use `sidarthe_test.txt`
5. Upload or resuse dataset `sirdata.csv`

### 2. Extracting configurations
1. Wait for extractions to finish for uploaded document
2. Attach the model, document, and dataset to the model configuration node
3. In the model configuration drilldown click the "Extract configurations from inputs" button.
4. Wait for the configurations to finish
5. Make sure there are new configurations appearing under the Suggested Configurations table with the source either coming from the attached document and/or dataset

### 3. Apply configuration values
1. Under the suggested configurations table click the "apply configurations values" button
2. Ensure that the configuration has been populated with the selected configuration's values
3. Lastly in the parameters table click the "Suggested configurations" button and apply and value and see if the value is populated accordingly

### 4. End test
1. logout of the application 
