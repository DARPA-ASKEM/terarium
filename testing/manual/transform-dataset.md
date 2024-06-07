## Transform Dataset Operator
Please go through __every__ step of the test scenario.\
When blocked, an error, or a UI/UX anomaly occurs, please report which scenario and step to [\#askem-testing](https://unchartedsoftware.slack.com/archives/C06FGLXB2CE).

Estimated time to completion: [X] minutes

### 1. Begin test 
1. Login to https://app.staging.terarium.ai using the test account
    ```
    email: qa@test.io
    password: askem-quality-assurance
    ```
2. Create, or open, project named `QA <Your Name> <Date>`

### 2. Join datasets
1. Upload [`sample.csv`](https://drive.google.com/file/d/1ZmDgj4EPcO0I9SQR5LwPl4pvPln5iq-0/view?usp=drive_link).
2. Upload [`sample2.csv`](https://drive.google.com/file/d/1FG-lvQKNtASmTXwIOmLbTtuVtoxr6-1R/view?usp=drive_link).
3. Add `sample` to workflow.
4. Add `sample2` to workflow.
5. Add Transform Dataset operator.
6. Attach `sample` and `sample2` as inputs to the new dataset operator.
7. Click "Edit" button on operator.
8. Query the agent with "Join the two datasets along the `id` column", press "paper airplane" icon.
10. After the question has been answered, select the new dataframe in dropdown at the bottom of the page beside the "Save As" button, the new dataframe name is described as the result of the Answer.
11. Save as `result`.
12. __Expected Result__: New dataset `result` should exist under "Datasets" and contain 5 columns.

## End test
1. logout of the application 
