## [Name of the Test Scenario]
Please go through __every__ step of the test scenario.\
When blocked, an error, or a UI/UX anomaly occurs, please report which scenario and step to [\#askem-testing](https://unchartedsoftware.slack.com/archives/C06FGLXB2CE).

Estimated time to completion: [X] minutes

### 1. Begin test
1. Login to https://app.staging.terarium.ai using the test account
    ```
    email: qa@test.io
    password: askem-quality-assurance
    ```
2. Create, or open, project named `QA [Your Name]`

### 2. [Create simple stratified model]
1. Add the [SIR model](https://drive.google.com/file/d/1vtIwevIXR4DkEROcR7KdKrCrEHGfo7s0/view?usp=sharing) to the project.
2. Create a new workflow and name it `SIR stratification`
3. Add the SIR model to the workflow and connect it to a `Configure model` component
4. open the Configure model component
5. __Expected Result:__ The Configure model component shows accurate values
   1. Appropriate Diagram
   2. Three Model equations
   3. Initial variables (1000, 2, 0)
   4. Parameters (0.6, 0.2)
6. Name and Save and close the Configure model component
7. Add a `Stratify model` component to the workflow
8. Link the saved model configuration to the stratify component and press the `Edit` button to bring up the component details
9. Press the `Stratify` button
10. __Expected Result:__ The original model should appear in the right panel
11. Add a new stratification variable named `Age`
   1. select all parameters to stratify
   2. add `young`, `middle`, and `old` age groups
   3. ensure that `Create new transitions between strata` and `Allow existing interactions to involve multiple strata` are NOT checked
12. Press the `Stratify` button
13. __Expected Result:__ The stratified model should appear in the right panel with the following changes
    1. The graph should contain stratified state and transitions
    2. `Initial variables` should be stratified
    3. `Parameters` should be stratified
    4. `Transitions` should be stratified
14. Save the stratified model as a new model and connect it to a new configure model component
15. Open the new configure model component
16. __Expected Result:__ The Configure model component shows accurate values
   1. Appropriate Diagram
   3. Matrix of Initial variables
      * (S = 1000/3 each)
      * (I = 2/3 each)
      * (R = 0 each)
   4. Matrix of Parameters
      * (β = 0.6/3 each)
      * (γ = 0.2/3 each)
   5. Stratification variable `Age`

### 3. [Edit Stratified Model]
1. Open up the new stratified model configuration created in the previous step
2. Modify `Initial variables` and `Parameters` using various methods
3. Save changes and close the model
4. __Expected Result:__ The model should show the updated values

### 4. [Chain Stratified Models]
1. Add a new `Stratify model` component to the workflow
2. Link the model created in the previous step to the stratify component and press the `Edit` button to bring up the component details
3. Add a new stratification variable named `Intelligence`
  1. select `S_middle`, `I_middle`, and `R_middle` to stratify
  2. add `smart` and `dumb` groups
  3. ensure that `Create new transitions between strata` and `Allow existing interactions to involve multiple strata` are NOT checked
4. Press the `Stratify` button
5. __Expected Result:__ The stratified model should appear in the right panel with the following changes
    1. The graph should contain new stratified states
    2. `Initial variables` should have new stratifications
    3. `Parameters` should not have new stratifications
    4. `Transitions` should have new stratifications

### 4. End test
1. logout of the application 
