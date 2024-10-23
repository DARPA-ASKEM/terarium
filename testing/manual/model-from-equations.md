## Model from equations
Please go through __every__ step of the test scenario.\
Report any issues into GitHub: [open an issue](https://github.com/DARPA-ASKEM/terarium/issues/new?assignees=&labels=bug%2C+Q%26A&template=qa-issue.md&title=%5BBUG%5D%3A+).

### 1. Begin test
1. Login to https://app.staging.terarium.ai using the test account
    ```
    email: qa@test.io
    password: askem-quality-assurance
    ```
2. Create, or open, project named `QA [Your Name]`

### 2. Upload a PDF
1. Upload _the Document_ [SIR.pdf](../data/SIR.pdf)
2. Wait for final notification of document extraction

### 3. Extract model from document
1. Create a new workflow named `Model from equations`
2. Drag and drop _the Document_ onto the workflow
3. Add the operator `Create model from equations`
4. Link _the Document_ and open the operator
5. Select the equations `S`, `I` and `R` and create model
6. Save the model for re-use and name it `SIR`
7. Make sure that the name has been updated:
   a. Operator drilldown output dropdown
   b. Operator node output label
8. The model is available in the resource panel

### 4. Check extractions from document
1. Wait for the notification that the model enrichment has been completed
2. Is the model description been filled with relevant information from the document?
3. Are the variables description been filled with relevant information from the document?
