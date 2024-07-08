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
3. Upload model `input_model.json` to project

### 2. Configure a model
1. Make sure the initial and parameter tables appear with expected data
2. Add a name and a description under the context accordian
3. In the initial table pick any variable and change the expression and add a source.
4. In the parameter table pick any variable and make some changes, such as changing between constant and uniform and adding value, min, and max to the respective parameter type
6. Click `Save` and ensure all values that you have filled are still persisting

### 2a. Add an uncertainty
1. In the parameters table click the "Add Uncertainty" button table to add uncertainties to any constant parameter
2. Click the check to apply the uncertainty and make sure that all selected constant values have a uniform distribution applied with the correct uncertainty (i.e an uncertainty of 10% on a constant of 5 will yield a uniform distribution of min: 4.5 max 5.5).

### 3. Configure the model again
1. Change the name and description of the model config
2. Make some changes (whatever you feel) to the initial and parameters table as well as interventions
3. Click the Run button and make sure the changes persist
4. In the top dropdown, select between each configuration and when selected, ensure that the values in name, description, tables, and interventions change accordingly.

### 4. Configure a stratified model
1. Repeat #2, #2a, and #3 for a stratified model
