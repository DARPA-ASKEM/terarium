## Simulate
Please go through __every__ step of the test scenario.\
Report any issues into GitHub: [open an issue](https://github.com/DARPA-ASKEM/terarium/issues/new?assignees=&labels=bug%2C+Q%26A&template=qa-issue.md&title=%5BBUG%5D%3A+).

### Note
Note: sampling combinations in PyCIEMSS can result in numerical instability, when this happens, you can:
- Retry the simulation, or
- Fiddle with the parameter distribution ranges, make the intervals larger or smaller

### 1. Begin test
1. Login to https://app.staging.terarium.ai using the test account
    ```
    email: qa@test.io
    password: askem-quality-assurance
    ```
2. Create, or open, project named `QA [Your Name]`

### 2. Model setup
1. Use/upload a model from [google drive](https://drive.google.com/drive/folders/1bllvuKt6ZA1vc36AW3Xet4y6ZAnwnaVN)
2. Drag and drop the model from the `Resources` panel onto the canvas
3. Connect the output port of the `Model` operator to a `Configure model` and open it
4. Ensure that the model parameters (`\beta`, `\gamma`) have distribution ranges
5. Connect the output port of the `Configure model` operator to the input port of a `Simulate` operator

### 3. Add interventions
1. Add a `Create Intervention Policy` operator by right-clicking on the canvas or using the `+ Add component` button on the top right corner of the canvas
2. Connect the output port of the `Model` operator to the input port of the `Create Intervention Policy` operator
3. Click `Open` on this operator
4. Create a new intervention policy by specifying an intervention
5. Select `Static` to create a static intervention
6. Edit the name from `New Intervention` to `Int0`
7. Select the parameter `\beta` from the dropdown, an intervention value of `0.1` starting at timepoint `50`
8. Click `+ Add intervention` to add another intervention with name `Int1`
9. Select `Static`, the parameter `\gamma`, an intervention value of `0.6` and timepoint `60`
10. Click `+ Add intervention` again with name `Int2`
11. Select `Dynamic`, the state variable `S`, an intervention value of `0.80`, the trigger variable `I`, `increases to above` and a threshold value of `0.10`
12. Click `Save`
13. Inspect the charts and captions on the right and check that they are consistent with the interventions
14. Connect the output port of this operator to the input port of the `Simulate` operator

### 3. Run with small samples
1. Set number of samples to 10, run the simulation
2. Verify that results are returned and you can select variables in the chart
3. Verify that you can add a chart and repeat step-2
4. Verify that you can remove the chart added

### 4. Run with larger samples
1. Set number of samples to 100, run the simulation
2. Verify that results are returned and you can select variables in the chart
3. Verify that there are a lot more lines on the chart indicating higher number of samples
4. Verify that you can add a chart and repeat step-2 and step-3
5. Verify that you can remove the chart added

### 5. Check simulation results
1. Inspect the `\beta, \gamma` charts and make sure that the intervention policy has been applied
2. Inspect the `S, I` charts and check that the intervention on `S` triggered by `I` does occur

### 5. Misc
1. Verify that you can go back and forth between the two outputs created
2. Switch from the default solver to something else, rerun the simulation
3. Verify that there are now three outputs associated with the operator and you can switch between these outputs
