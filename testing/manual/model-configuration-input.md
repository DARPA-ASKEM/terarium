## Configure a model configuration
Please go through __every__ step of the test scenario.\
Report any issues into GitHub: [open an issue](https://github.com/DARPA-ASKEM/terarium/issues/new?assignees=&labels=bug%2C+Q%26A&template=qa-issue.md&title=%5BBUG%5D%3A+).

### 1. Begin test
1. Login to https://app.staging.terarium.ai using the test account
    ```
    email: qa@test.io
    password: askem-quality-assurance
    ```
2. Create, or open, project named `QA [Your Name]`
3. Upload model [SIR.json](https://drive.google.com/file/d/1eXlvpBfMmhrfC0xUXfuz0s_19gi-Rird/view?usp=drive_link) to project

### 2. Configure a Model
1. Add the SIR model to your workflow canvas. Add a `Configure Model` operator and connect it to your SIR model. Open the configuration editor.
2. Verify that the initial condition (e.g., SIR) and parameter (e.g., β, γ) tables are populated with data.
3. Update the configuration name and add a description.
4. Modify an initial condition (e.g., change S to 1000; I to 2; R to 0).
5. Update a parameter (e.g., change β from uniform to constant and set it to 0.9).
6. Add a source to an initial condition or parameter (e.g., NY Times data).
7. Attempt to save the configuration:
   - Verify that the **Save** button is disabled and the **Save As** button is enabled since values have been modified.
8. Use the **Save As** button to create a new configuration. Confirm that the changes persist.

### 3. Update Metadata Only
1. Open an existing model configuration in the configuration editor.
2. Modify the configuration name, description, or initial/parameter sources without changing any values.
3. Attempt to save the configuration:
   - Verify that the **Save** button is enabled and that the configuration can be saved without creating a new one.
4. Confirm that the changes persist after saving.

### 4. Add Uncertainty to a Parameter
1. Open an existing model configuration in the configuration editor.
2. Add uncertainty to a parameter (e.g., apply uncertainty to β, changing it from constant to uniform with values set to +/- 10% of the initial constant). Note: uncertainties can **only** be applied to **constant** parameter types
3. Attempt to save the configuration:
   - Verify that the **Save** button is disabled and the **Save As** button is enabled due to the modification of the parameter values.
4. Use the **Save As** button to create a new configuration. Confirm that the changes persist.

### 5. Configure a Stratified Model
1. Add the SIR model to your workflow canvas. Add a `Stratify Model` operator and connect it to your SIR model. Open the stratification editor.
2. Create a new strata for `age` with two levels: old, young. Stratify the initial conditions (SIR) and parameter (γ). Uncheck `Create New Transition Between Strata` (assuming people remain in their age category). Run the stratification and save it as a new model named `Stratified SIR`.
3. Return to the workflow canvas and add the `Stratified SIR` to the canvas. Add the `Configure Model` operator and connect it to the stratified model. Open the configuration editor.
4. Verify that the initial condition (SIR) and parameter tables are correctly stratified with data. For example, ensure that the initial condition R has two levels: old and young.
5. Update the configuration name and add a description.
6. Update stratified values as follows:
   1. S_young: 1000
   2. S_old: 500
   3. I_young: 1
   4. I_old: 2
   5. R_young: 0
   6. R_old: 0
   7. γ_young: 14
   8. γ_old: 21
7. Add a source to an initial condition or parameter (e.g., NY Times data).
8. Modify a parameter (e.g., change β to constant: 0.9).
9. Attempt to save the configuration:
   - Verify that the **Save** button is disabled and the **Save As** button is enabled due to changes in values.
10. Use the **Save As** button to create a new configuration. Confirm that the changes persist.
11. Add uncertainty to a parameter (e.g., apply uncertainty to β, setting it from constant to uniform with min 0.81 and max 0.99).
12. Use the **Save As** button to create a new configuration. Confirm that the changes persist.

### 6. Reset Changes
1. For any configuration where changes have been made but not saved, click the **Reset** button.
2. Verify that all unsaved changes are discarded and the configuration reverts to its original state before any modifications were made.

### 7. Setting a date at timepoint 0 in model configuration
1. In the model node select a time unit if one hasn't already been selected (the options are days, months, years) and save the model.
2. In the model configuration node you should now have an option to select a date to start at timepoint 0, select a date of your choosing and save.
3. Connect the model configuration node to a simulate node and press run in the simulate node.
4. You should observe that the x-axis of the charts will use dates rather than integers starting at the selected date in the model configuration.

