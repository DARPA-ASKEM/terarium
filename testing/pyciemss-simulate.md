## PyCIEMSS Simulate
Please go through __every__ step of the test scenario.\
When blocked, an error, or a UI/UX anomaly occurs, please report which scenario and step to [\#askem-testing](https://unchartedsoftware.slack.com/archives/C06FGLXB2CE).

### 1. Begin test
1. Login to https://app.staging.terarium.ai using the test account
    ```
    email: qa@test.io
    password: askem-quality-assurance
    ```
2. Create, or open, project named `Q&A [Your Name] [YYMMDD]`

### 2. Model setup
1. Find a Petrinet model (e.g. SIR) and bring it onto a workflow
2. Connect the model to a Model Configuration operation, create a configuration
3. Ensure that the model parameters (e.g. beta, gamma) have distribution ranges
5. Connect the model-configuration to a Simulate-PyCIEMSS operation

### 3. Run with small samples
1. Set number of samples to 10, run the simulation
2. Verify that results are returned and you can select variables in the chart
3. Verify that you can add a chart and repeat step-2
4. Verify that you can remove the chart added

### 4. Run with larger samples
1. Set number of samples to 100, run the simulation
2. Verify that results are returned and you can select variables in the chart
3. Verify that there are a lot more lines on the chart indicating higher number of samples
4. Verify that you can add a chart and repeat step-2 and step-3
5. Verify that you can remove the chart added

### 5. Misc
1. Verify that you can go back and forth between the two outputs created
2. Switch from the default solver to something else, rerun the simulation
3. Verify that there are now three outputs associated with the operator and you can switch between these outputs

### 6. End test
1. logout of the application 
