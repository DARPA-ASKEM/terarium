## Model extraction
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

### 2. Upload a PDF
1. Upload _the Document_ [SIR.pdf](data/SIR.pdf)
2. Wait for final notification of document extraction
3. Open _the Document_ and check that extractions are visible

### 3. Extract model from document
1. Create a new workflow named `Extract model from document`
2. Drag and drop _the Document_ onto the workflow
3. Add the operator `Create model from equations`
4. Link _the Document_ and open the operator
5. Select the equations `S`, `I` and `R` and create model
6. Save model and name as `SIR`

### 4. Check extractions from document
1. Open model `SIR`
2. Is the TA1 _model card_, extracted with `mit-tr` available?
3. Is the TA4 _model card_, extracted from `gollm-taskrunner` available?
4. Are the _variables extractions_ available in the `Other` accordion?

### 5. End test
1. logout of the application 
