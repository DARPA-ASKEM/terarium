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

### 2. Configure a Model**
1. Add the  SIR model to your workflow canvas. Add a `Configure Model` operator and connect this to your SIR model. Open the configuration editor.
2. Make sure the initial condition (e.g. SIR) and parameter (e.g. beta,gamma) tables appear as expected with data.
3. Update the configuration name and add a description.
4. Pick an initial condition and update it (e.g., change S to 1000; I to 2; R to 0).
5. Pick an initial condition and add a source (e.g., NY Times data).
6. Pick a parameter and update it (e.g. change beta from uniform to constant and set to .9).
7. Pick an initial condition and add a source (e.g., https://www.mdpi.com/1099-4300/23/1/59 )
8. Save your configuration and make sure the changes you have made persist.

### 3. Add Uncertainty to Parameter**
1. Update the configuration name and add a description (e.g., SIR with uncertainty).
2. In the parameter table click `add uncertainty.`Apply this to beta. Make sure that this has been updated from `constant` to `uniform.` and has values set to +/- 10% of the intial constant.
3. Save as a new configuration and make sure the changes you've made persist.


### 4. Configure a Stratified Model**
1. Add the SIR model to your workflow canvas. Add a `Stratify Model` operator and connect this to your SIR Model. Open the stratification editor.
2. In the stratification editor create a new strata for `age` with two levels: old, young. Stratify for the initial conditions SIR and parameter gamma.  Uncheck create new transition between strata (we are assuming people stay in their age category). Run stratification and save as a new model. Name this `Stratified SIR`
3. Return to your workflow canvas and add the `Stratifed SIR ` to your canvas. Add the `Configure Model` operator and connect this to your stratified model.
4. Make sure the initial conditions (SIR) and parameter tables appear as expected with data. Note that the initial condition R should have two levels: old and young. 5
5. Update the configuration name and add a description.
6. Ensure you have matrices for young and old for SIR, and gamma. Update the configuration values to the following:
   1. S_young: 1000
   2. S_old: 500
   3. I_young: 1
   4. I_old: 2
   5. R_young:0
   6. R_old:0
   7. Gamma_young: 14
   8. Gamma_old: 21
7. Add a source to one of the initial conditions or parameters: e.g., NY Times data
8. Change beta to constant: .9
9. Save as a new configuration and make sure that your changes persisted.
10. Create a new configuration name and new description.
11. Add uncertainty to beta:  In the parameter table click `add uncertainty.`Apply this to beta. Make sure that this has been updated from `constant` to `uniform.` and has values set to +/- 10% of the initial constant (should be min .81  and max 99).
12. Save as a new configuration and make sure the changes you've made persist.
