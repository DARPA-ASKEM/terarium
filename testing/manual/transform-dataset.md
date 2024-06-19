## Transform Dataset Operator
Please go through __every__ step of the test scenario.\
Report any issues into GitHub: [open an issue](https://github.com/DARPA-ASKEM/terarium/issues/new?assignees=&labels=bug%2C+Q%26A&template=qa-issue.md&title=%5BBUG%5D%3A+).

### 1. Begin test
1. Login to https://app.staging.terarium.ai using the test account
    ```
    email: qa@test.io
    password: askem-quality-assurance
    ```
2. Create, or open, project named `QA <Your Name>`

### 2. Join datasets
1. Upload [`sample.csv`](https://drive.google.com/file/d/1ZmDgj4EPcO0I9SQR5LwPl4pvPln5iq-0/view?usp=drive_link).
2. Upload [`sample2.csv`](https://drive.google.com/file/d/1FG-lvQKNtASmTXwIOmLbTtuVtoxr6-1R/view?usp=drive_link).
3. Add `sample` to workflow.
4. Add `sample2` to workflow.
5. Add Transform Dataset operator.
6. Attach `sample` and `sample2` as inputs to the new dataset operator.
7. Click "Edit" button on operator.
8. Query the agent with "Join the two datasets along the id column".
9. After the question has been answered, select the new dataframe (merged_df) in the dropdown located beside the "Save as" button.
10. Click the "Save as" button to save the dataframe. Name it `result`.
11. __Expected Result__: New dataset `result` should exist under your project's Datasets and contain 4 columns (id, count, label, level).
