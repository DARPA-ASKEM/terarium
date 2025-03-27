## Optimize
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
3. Create a workflow named `Optimize`

### 2. Upload assets
1. Use/upload the [_SEIRHD_](https://drive.google.com/file/d/1wCZl0NCjN6jZeoEy5UNVbElW8a4xQHXw/view?usp=drive_link) model from [google drive](https://drive.google.com/drive/folders/1XknwhuvLMPKC68LK1HQp_n77fHaIuLGt?usp=drive_link)
2. Use/upload the [_LA county_](https://drive.google.com/file/d/1gW-q-N_pLOAUp151NyPAuXNL1WTBRDak/view?usp=drive_link) dataset from [google drive](https://drive.google.com/drive/folders/1XknwhuvLMPKC68LK1HQp_n77fHaIuLGt?usp=drive_link)

### 3. Model setup
1. Create a default configuration with the `Configure model` operator
2. Calibrate the model with the dataset with the `Calibrate` operator.
   
   Mapping the model variable:
    - `Culmative_cases` with the dataset variable `cases`
    - `deceased` with `deaths`
    - `timestamp` with `Timestamp`

### 4. Masking start time optimization
1. Create a Masking Policy operation with the `Intervention Policy` operator.
2. Set _NPI mult_ to `0.5` starting at _time_ `61` 

#### 4.a Static intervention
1. Connect the calibrated configuration from step 3 and the intervention from step 4 to an optimize node.
2. Optimize intervention: set _H_ to `< 20 000` in all time points in `95%` of simulated outcomes. 
3. Find a `new start time` for _NPI mult_ `upper bound` (how long we can delay masking). Start time `60`, end time `150`, initial guess `61`. 
4. Optimization settings: end time `150`, maxiter `3` max eval `30`
5. This should succeed with a new start time.

### 5. Hospitalizations optimization
1. Create a Hospitalizations Policy operation with the `Intervention Policy` operator.

#### 5.a Static intervention
1. Create an intervention for _NPI mult_, set its value to `0.5`, and starting time to `118`
2. Optimize intervention: set _H_ to `< 20 000` in all time points in `95%` of simulated outcomes.
3. Find a `new value` for _NPI mult_ `upper bound` (minimal reduction in transmission). Using the following guidelines: Min value = `.0002` intial guess `.5` max = `.9996`
4. Optimization settings: end time `150`, maxiter `3` max eval `30`
5. Confirm results: 
  -It should be clear that the optimize did not meet the success criteria. 
  -A value of ~0.0003 is what we expect to see as our result. This will be trending towards 0.0002 if we provide it more iterations. (lowest allowed)
  -The success criteria chart should clearly show the average of the worst 5% is higher than 20,000
