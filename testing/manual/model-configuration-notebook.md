## Model Configuration Notebook
Please go through __every__ step of the test scenario.\
Report any issues into GitHub: [open an issue](https://github.com/DARPA-ASKEM/terarium/issues/new?assignees=&labels=bug%2C+Q%26A&template=qa-issue.md&title=%5BBUG%5D%3A+).

### 1. Begin test
1. Login to https://app.staging.terarium.ai using the test account
    ```
    email: qa@test.io
    password: askem-quality-assurance
    ```
2. Create, or open, project named `QA [Your Name]`

### 2. Workflow
1. Upload the SIR [model](https://drive.google.com/file/d/1eXlvpBfMmhrfC0xUXfuz0s_19gi-Rird/view?usp=drive_link)
3. Create a new workflow named `Model Configuration Notebook`
4. Add the model to it
5. Add a `Configure model` operator to the workflow and attach the model to it

### 3. Operator setup
1. Open the `Configure model` operator
2. Select the notebook view
3. At the top, use the AI assistant to ask a question: `What are the model parameters?`
4. Ensure that code is generated (a `get_params`) function.
5. Execute the code and validate the output prints the various parameters.
6. Ask the AI: `Update R0 to 0.2`
7. Run the code

### 4. Validation
1. Go back to the wizard view
2. Verify that the value of `R0` is 0.2
