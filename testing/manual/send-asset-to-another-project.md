## Send asset to another project
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
1. Upload _the Document_ [SIR.pdf](../data/SIR.pdf) using `Upload Resources` button in the bottom left.
2. Wait for final notification of document extraction
3. Open _the Document_ and check that extractions are visible

### 3. Send the PDF to another project
1. Click _the Document_.
2. In the top middle `...` menu select `Add to project`
3. Choose a project to add the file to.
4. Select the other project.
5. Check that the file has been added successfully.
