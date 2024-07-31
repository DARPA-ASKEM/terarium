## Model Stratification
Please go through __every__ step of the test scenario.\
Report any issues into GitHub: [open an issue](https://github.com/DARPA-ASKEM/terarium/issues/new?assignees=&labels=bug%2C+Q%26A&template=qa-issue.md&title=%5BBUG%5D%3A+).

### 1. Begin test
1. Login to https://app.staging.terarium.ai using the test account
    ```
    email: qa@test.io
    password: askem-quality-assurance
    ```
2. Create, or open, project named `QA [Your Name]`

### 2. Upload asset
1. Add the [SIR model](https://drive.google.com/file/d/1vtIwevIXR4DkEROcR7KdKrCrEHGfo7s0/view?usp=sharing) to the project.

### 3. Create simple stratified model
1. Create a new workflow and name it `SIR stratification`
2. Add the model to the workflow and connect it to a `Configure model` component
3. __Expected Result:__ The Configure model component shows accurate values
   1. Appropriate Diagram
   2. Three Model equations
   3. Initial variables (1000, 2, 0)
   4. Parameters (0.6, 0.2)
4. Name and Save and close the Configure model component
5. Add a `Stratify model` component to the workflow
6. Link the saved model configuration to the stratify component and press the `Edit` button to bring up the component details
7. Press the `Stratify` button
8. __Expected Result:__ The original model should appear in the right panel
9. Add a new stratification variable named `Age`
   1. select all variables to stratify
   2. add `young`, `middle`, and `old` age groups
   3. ensure that `Create new transitions between strata` and `Allow existing interactions to involve multiple strata` are NOT checked
10. Press the `Stratify` button
11. __Expected Result:__ The stratified model should appear in the right panel with the following changes
    1. The graph should contain stratified state and transitions
    2. `Initial variables` should be stratified
    3. `Parameters` should be stratified
    4. `Transitions` should be stratified
12. Save the stratified model as a new model and connect it to a new configure model component
13. Open the new configure model component
14. __Expected Result:__ The Configure model component shows accurate values
    1. Appropriate Diagram
    2. Matrix of Initial variables
       * (S = 1000/3 each)
       * (I = 2/3 each)
       * (R = 0 each)
    3. Matrix of Parameters
       * (β = 0.6/3 each)
       * (γ = 0.2/3 each)
    4. Stratification variable `Age`

### 4. Edit Stratified Model
1. Open up the new stratified model configuration created in the previous step
2. Modify `Initial variables` and `Parameters` using various methods
3. Save changes and close the model
4. __Expected Result:__ The model should show the updated values

### 5. Chain Stratified Models
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
