## Model extraction
Please go through __every__ steps of the test scenario. 
When blocked, or an error, an UI/UX anomaly occurs, please report which scenario and step to [ #askem-testing](https://unchartedsoftware.slack.com/archives/C06FGLXB2CE).


### 1. Begin test 
- [ ] a. login to https://app.staging.terarium.ai using the test account

```
email: qa@test.io
password: askem-quality-assurance
```

- [ ] b. Create project named `Q&A [Your Name]`

### 2. Upload a PDF
- [ ] a. Upload _the Document_ [SIR](https://drive.google.com/file/d/1vN4sNR7IrRi5GsOQ9r_LgV4S5wwinXnG/view?usp=drive_link)
- [ ] b. Wait for final notification of document extraction
- [ ] c. Open _the Document_ and check that extractions are visible

### 3. Extract model from document
- [ ] a. Create a new workflow named `Extract model from document`
- [ ] b. Drag and drop _the Document_ onto the workflow
- [ ] c. Add the operator `Create model from equations`
- [ ] d. Link _the Document_ and open the operator
- [ ] e. Select the equations `S`, `I` and `R` and create model
- [ ] f. Save model and name as `SIR`

### 4. Check extractions from document
- [ ] a. Open model `SIR`
- [ ] b. Is the TA1 _model card_, extracted with `mit-tr` available?
- [ ] c. Is the TA4 _model card_, extracted from `gollm-taskrunner` available?
- [ ] d. Are the _variables extractions_ available in the `Other` accordion?

### 5. End test
- [ ] a. logout of the application 