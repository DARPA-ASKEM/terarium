## Configure a model configuration
Please go through __every__ step of the test scenario.\
When blocked, an error, or a UI/UX anomaly occurs, please report which scenario and step to [\#askem-testing](https://unchartedsoftware.slack.com/archives/C06FGLXB2CE).

Estimated time to completion: [X] minutes

### 1. Begin test
1. Login to https://app.staging.terarium.ai using the test account
    ```
    email: qa@test.io
    password: askem-quality-assurance
    ```
2. Create, or open, project named `QA [Your Name] [YYMMDD]`
3. Upload model `input_model.json` to project

### 2. Configure a model
1. Make sure the initial and parameter tables appear with expected data
2. Add a name and a description under the context accordian
3. In the initial table pick any variable and add a 'unit', 'value', and 'source'
4. In the parameter table pick any variable and do the same
5. Under the Interventions accordian select any other parameter and add an intervention
6. Click Run and ensure all values that you have filled are still persisting

### 3. Configure the model again
1. Change the name and description of the model config
2. Make some changes (whatever you feel) to the initial and parameters table as well as interventions
3. Click the Run button and make sure the changes persist
4. In the top dropdown, select between each configuration and when selected, ensure that the values in name, description, tables, and interventions change accordingly.

### 4. Configure a stratified model
1. Repeat #2 and #3 for a stratified model

### 5. End test
1. logout of the application 
