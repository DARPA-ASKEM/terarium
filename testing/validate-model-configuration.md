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
2. Upload _the Model_ [Configured SIR.json](data/Configured SIR.json)
3. Drag and drop _the Model_ onto the workflow
4. Link and open a _Configure Model_ operator
5. Verify that the model is configured correctly \
   `β: 0.6, γ: 0.2, S: 1000, I: 2, R: 0`
6. Verify the parameters β and γ have some ranges, e.g \
  `β min/max = [0.4, 0.8], γ min/max = [0.1, 0.3]`
7. Save the configuration by giving it a name and clicking `Run`

### 3. Operator setup
1. Link to a _Validate configuration_ operator
2. You should see an `additonal options` section, expand it
3. A tolerance/threshold input should be available 
    - set to `0.1`, this tells Funman to create smaller bounding boxes 
4. A dropdown `parameters of interest` should be available populated with model parameters and states
    - select `beta (β)` and `gamma (γ)`.

### 4. Compartmental constraint
It ensures non-negativity and mass conservation
1. The `Compartmental constraint` should be on by default
2. verify that the equation is correct `S + I + R = 1002`
  
### 5. Successful state constraint
1. Create a new constraint by clicking `Add constraint`
2. Set to `State constraint` and name it `Infected`
    - set the target to `I` from the dropdown 
    - set the upper bound to `750`
    - leave start/end time to `[0, 100]`
    - leave the weight to `1.0`
3. Run validation
4. Verify that service returns **true** boxes as infected should not exceed 750

### 6. Failed state constraint
1. Change the upper bound of `Infected` constraint `100`
2. Run validation
3. Verify that service returns **false** boxes as infected should exceed 100 between time `[0, 100]`
4. Delete the constraint

### 7. Successful monotonicity constraint
1. Create a new constraint set to `Monotonicity constraint` and name it `Suceptible`
    - set the target to `S` from the dropdown 
    - set the direction to `decreasing`
2. Run validation
3. Verify that service returns **true** boxes as susceptible should be **decreasing** over time

### 8. Failed monotonicity constraint
1. Change the direction of `Suceptible` constraint to `increasing` 
2. Run validation
3. Verify that service returns **false** boxes as susceptible should be **decreasing** over time

### 5. End test
1. logout of the application 
