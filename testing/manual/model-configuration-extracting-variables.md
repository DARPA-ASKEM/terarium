## Extract configurations from document and dataset
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
2. Dataset [sirdata.csv](https://drive.google.com/file/d/1y1THGHIu8ebb9JdN3ZG12NwWcZtHPxzD/view?usp=drive_link)
3. Document [41591_2020_Article_883.pdf](https://drive.google.com/file/d/1RrCxj__qqFSRHk5uIi_JJsFo8iC8dgYk/view?usp=drive_link), if extraction are not working use [sidarthe_test.txt](https://drive.google.com/file/d/1IyWJmE-v4o5ebJonwMc_nqNj9j96C8BE/view?usp=drive_link)
4. Wait for extractions to finish for uploaded document

### 3. Extracting configurations
1. Attach the model, document, and dataset to the model configuration node
2. Extract configurations from the inputs
3. Make sure there are new configurations appearing under the Suggested Configurations table with the source either coming from the attached document and/or dataset

### 4. Apply configuration values
1. Apply the extracted configuration
2. Ensure that the configuration has been populated with the selected values
