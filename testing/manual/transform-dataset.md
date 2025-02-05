Please go through **every** step of the test scenario.  
Report any issues into GitHub: [open an issue](https://github.com/DARPA-ASKEM/terarium/issues/new?assignees=&labels=bug%2C+Q%26A&template=qa-issue.md&title=%5BBUG%5D%3A+).

### 1. Begin test
1. Login to [https://app.staging.terarium.ai](https://app.staging.terarium.ai/) using the test account
    ```
    email: qa@test.io
    password: askem-quality-assurance
    ```
    
2. Create, or open, project named `QA <Your Name>`

### 2. Upload Datasets

1. Upload the [COVID cases dataset ](https://drive.google.com/file/d/1fNIMwEGm1vfMV2fP-mdaPYAp9MOpa_jn/view?usp=drive_link)
2. Upload the [COVID deaths dataset](https://drive.google.com/file/d/1MfRUykL6tjYoxVRU88um2nMwxFYCVhjk/view?usp=drive_link)

### 3. Join Two Datasets and Plot the data
1. Add the California Cases and Deaths datasets to the workflow canvas. 
2. Connect the two datasets to a transform data set operator.
3. View the datasets to see the columns and data types. Ensure both datasets have a date column.
4. Ask the agent to join the two datasets together on date. 
5. Add a code cell to view the newly joined dataset
6. Filter for LA county (i.e. county = `Los Angeles`) for January 2021 to March 2021. 
7. Ask the agent to add a new column named timestamp that is a timestamp representation of the date column. 
8. Save the filtered LA county dataset as a new dataset named 'LA_cases_deaths_data'
9. Ask the operator to plot for cases and deaths.
10. Select the plot to be displayed on the node thumbnail.
11. Close the drilldown and ensure that the plot appears in the node.
12. Add the recently saved dataset to the workflow canvas and preview the dataset. Ensure that it has saved correctly. 
