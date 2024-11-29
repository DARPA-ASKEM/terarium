# Project export and import

Please go through __every__ step of the test scenario.\
Report any issues into GitHub: [open an issue](https://github.com/DARPA-ASKEM/terarium/issues/new?assignees=&labels=bug%2C+Q%26A&template=qa-issue.md&title=%5BBUG%5D%3A+).

### 1. Begin test

1. Login to https://app.staging.terarium.ai using the test account
    ```
    email: qa@test.io
    password: askem-quality-assurance
    ```

### 2. Scenario A: Simulate workflow
1. Create a new project
2. Add a simple model into the project, eg SIR
3. Create a simple simulation workflow: model => model-config => simulate
4. Run simulate, pick a few variables to plot once simulate completes
5. Export the project: Project-overview, download
6. Rename the project to [project-old]
7. Import the project you downloaded
8. Once import completes with no errors, go into thew newly imported project, verify what was created from before (model, workflow) exists, verify the results (eg: variable plots in simulate) are visible and same as the original
9. Pick different variables to plot, verify action can be completed successfully
10. Remove the newly imported project


### 3. Scenario B: Data transform workflow
1. Create a new project
2. Add a dataset into the project
3. Create a simple data-transform workflow: dataset => data-transform
4. Run a few cells in data-transform, eg: show rows/columns, plot a few charts
5. Export the project: Project-overview, download
6. Rename the project to [project-old]
7. Import the project you downloaded
8. Once import completes, go into the new project, verify that you can open the data-transform and see previous cells and cell-results
9. Remove the newly imported project

