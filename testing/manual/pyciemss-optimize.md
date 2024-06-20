## PyCIEMSS Optimize
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
2. Connect the model to a Model Configuration operation, create a configuration
3. Ensure that the model parameters (e.g. beta, gamma) have distribution ranges
4. Connect the model-configuration to a Optimize with Pyciemss operator

### 3. Run with minimal effort:
1. Select a state in the constraint box. This will be done via a dropdown. You should be able to select any that you wish. 
    An example is `Infected`
2. Select a parameter in the Intervention policy box. This is also done in a via a dropdown. You should be able to select any. To make the most sense this should be one that impacts the state selected in the first step.
    An example is `beta`
3. Leaving everything else as defaulted values hit the `Run` button.
4. Ensure that the run finishes and that you can see some charts. 

### 5. Misc - pick a few to try:
1. Run a second time time with at least one input differet. Verify that you can go back and forth between the two outputs created and that the input box you updated changes with the output change
2. Change intervention type to `start time` (instead of `parameter value`) 
3. change the constraint from `max` to `Day average`
4. change the constraint from `less than` to `greater than`
5. create multiple policy bounds
6. create multiple contraints ** Will be meaningless at the moment.
7. utilize the `active` toggle in constraint and/or policy boxes.
8. plug in a calibration into this box and rerun
