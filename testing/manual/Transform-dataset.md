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

### 2. Upload datasets
1. Upload [`sample.csv`](https://drive.google.com/file/d/1ZmDgj4EPcO0I9SQR5LwPl4pvPln5iq-0/view?usp=drive_link).
2. Upload [`sample2.csv`](https://drive.google.com/file/d/1FG-lvQKNtASmTXwIOmLbTtuVtoxr6-1R/view?usp=drive_link).

### 3. Join datasets
1. Create a workflow named `Transform Dataset` and add the datasets.
2. Add the datasets to a `Transform Dataset` operator.
3. Query the agent with "Join the two datasets along the id column".
4. After the question has been answered, select the new dataframe `merged_df`.
5. Save the dataframe and name it `result`.
6. __Expected Result__: New dataset `result` should exist under your project's Datasets and contain 4 columns (id, count, label, level).
