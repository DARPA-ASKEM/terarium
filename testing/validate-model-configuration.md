## Validate Model Configuration
Please go through __every__ step of the test scenario.\
When blocked, an error, or a UI/UX anomaly occurs, please report which scenario and step to [\#askem-testing](https://unchartedsoftware.slack.com/archives/C06FGLXB2CE).

### 1. Begin test
1. Login to https://app.staging.terarium.ai using the test account
    ```
    email: qa@test.io
    password: askem-quality-assurance
    ```
2. Create, or open, project named `Q&A [Your Name] [YYMMDD]`

### 2. Workflow
1. Create a new workflow named `Validate Model Configuration`
2. Upload _the Model_ [Configured SIR.json](https://drive.google.com/file/d/144jN19MrMYbE_nwmOgk2VD-GpNzrcf9d/view?usp=drive_link)
3. Drag and drop _the Model_ onto the workflow
4. Link and open a _Configure Model_ operator
5. Verify that the model is configured correctly \
   `β: 0.6, γ: 0.2, S: 1000, I: 2, R: 0`
6. Save the configuration by giving it a name and clicking `Run`

### 3. Setup operator
1. Link to a _Validate configuration_ operator
This constraint should always be there `Compartmental constraint`

always have those three constraint:
- monotonicity of R always increasing
- monotonicity of S always decreasing

When you select multiple values in the state/parameter constraint are additional.
So I select I and S, do not touch the weight. and Set upper bound of 1000.


### 4. Run operator

### 5. End test
1. logout of the application 
