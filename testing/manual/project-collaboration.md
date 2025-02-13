# Project Collaboration by Multiple Users

Please go through __every__ step of the test scenario.\
Report any issues into GitHub: [open an issue](https://github.com/DARPA-ASKEM/terarium/issues/new?assignees=&labels=bug%2C+Q%26A&template=qa-issue.md&title=%5BBUG%5D%3A+).

This manual test requires at least two testers or one tester with two tabs or accounts.

### 1. Begin test

1. Login to https://app.staging.terarium.ai using the test account
    ```
    email: qa@test.io
    password: askem-quality-assurance
    ```
2. Create new project named `QA [Your Name]` or open an existing one wherein only you (and _ASKEM Admins_) has access.
3. Click on `Overview`, `...` next to the project name, and `Share+` to see the **Share project** window
4. Ensure that 
    a. only your name is in the list **People and groups with access**
    b. **General access** is set to `Restricted`
    c. **Sample project** is not checked

### 2. Add collaborator

1. Open the **Share project** window as in Step 1.3
2. Type the name of the other tester in the field `Add people and groups` and select it
3. Check that their name and email address appear in the list below with `Edit` permission
4. Set **General access** to `Public`.
5. Go to the [homepage](https://app.staging.terarium.ai) and click on the _Public projects_ tab
    a. Check that this project appears there
    b. Check that the number of contributors is correct, i.e. `> 2`

### 2. Setup project

1. Return to the project page
2. Populate the project with these assets:
    a. [paper](https://onlinelibrary.wiley.com/doi/epdf/10.1155/2024/7589509)
    b. [dataset](https://drive.google.com/file/d/19L-QTh_mUACxhGSFfXt1qv3jM5w_jizK/view?usp=drive_link)

## 3. Parallel workflow test

Have each tester complete the following steps at more or less the same time.

1. Click on `+ New` in **Workflowss** to a create a workflow with a _Blank Canvas_ template and some unique name
2. Create a simple workflow graph
    1. Drag and drop the paper document onto the workflow canvas
    2. Connect it to a `Create model from equations` operator
    3. Select the **Page 3** equations and Click `Run`
    4. Click `Save for re-use` to save the output model with some unique name
    5. Connect the output port to a `Configure model` operator
    6. Type in these values for the different quantities
        | `s_{e}` | 0.999 |
        | --- | --- |
        | `i_e{}` | 0.001 |
        | `β_{e}` | 1.049 |
        | `γ` | 0.855 |
    8. Click `Save as` to save this as a new configuration with some unique name
    9. Connect the output to a `Simulate model` operator
    10. Select `Fast` in the _Preset_ dropdown (with _Number of samples_ `1`) and click `Run`
    11. Open the **Output Settings** side panel on the right
    12. Select `s_{e}` and click `+ Add comparison chart`
    13. Click `Save for re-use` to save the simulation result as a dataset with some unique name
3. Return to the workflow canvas and refresh the webpage
    1. Ensure that the other tester's assets appear in the **Resources** panel (their workflow, model, and dataset)
    2. Visit the page of their model and dataset
    3. Ensure that the model diagram and tables load as expected
4. Visit the other tester's workflow and make some edits
    1. Move all the operators around in the canvas
    2. Drag your previously saved model from Step 3.2.4 onto this canvas
    3. Connect its output port to a new `Configure model` operator
    4. Select your previously saved configuration from Step 3.2.8
    5. Repeat Steps 3.2.9-13
    6. Remove the other tester's `Configure model` and `Simulatemo model` operators using `...`
5. Refresh the webpage
6. Ensure that there is no _weirdness_ such as removed operators reappearing, operators becoming "yellow", operator ports becoming blank even though there is a connected link, links going nowhere
7. Ensure that the operators have not lost their state by opening them
8. Repeat Steps 5-7 in your workflow after the other tester has finished their steps

