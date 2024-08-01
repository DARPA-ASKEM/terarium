## Intervention Policy
Please go through __every__ step of the test scenario.\
Report any issues into GitHub: [open an issue](https://github.com/DARPA-ASKEM/terarium/issues/new?assignees=&labels=bug%2C+Q%26A&template=qa-issue.md&title=%5BBUG%5D%3A+).

### 1. Begin test
1. Login to https://app.staging.terarium.ai using the test account
    ```
    email: qa@test.io
    password: askem-quality-assurance
    ```
2. Create, or open, project named `QA [Your Name]`

### 2. Upload assets
1. Model [SIR.json](https://drive.google.com/file/d/1eXlvpBfMmhrfC0xUXfuz0s_19gi-Rird/view?usp=drive_link)
2. Dataset [SIR_Dataset](https://drive.google.com/file/d/1wdCLKKznHaoCg1gWI7q7OO8W4F7zOjpc/view?usp=drive_link)

### 3. Create an intervention policy
1. Create a new workflow named `Intervention Policy`
2. Drag and drop _the Model_ onto the workflow
3. Add the operator `Create intervention policy` and connect the model to it

### 4. Create a static criteria
1. Create a _static_ criteria named `Start masking`
2. Set parameter `beta` to `0.5` starting at `50`

### 5. Create a dynamic criteria
1. Create a _dynamic_ criteria named `Vaccination`
2. Set parameter `gamma` to `0.7` starting at `80`

### 6. Save the intervention policy
1. Save the intervention policy
2. Check the preview of the intervention policy
   1. Does the AI assisted _description_ match the criteria?
   2. Are the _criteria_ visible in the chart?

### 7. Simulate with the intervention policy
1. Add the operator `Configure model` to the workflow
2. Connect the model to the operator
3. Add the operator `Simulate` to the workflow
4. Connect the _intervention policy_ and _model configuration_ to the operator
5. Run the simulation
6. Check the results
   1. Are the _intervention policy_ and _model configuration_ visible in the results?
   2. Are the _criteria_ visible in the chart?
