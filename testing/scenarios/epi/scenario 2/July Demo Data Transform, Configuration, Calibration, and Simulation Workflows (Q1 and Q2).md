Some helpful resources to review:
1. [Scenarion 2 Hackathon](https://docs.google.com/document/d/1hT7dxMY1q6DxRx9IHAZ-ZjgJN5gwg2nwAqAScaYE1co/edit#heading=h.qxs5hxumjzep)
2. [Nelson Instructions](https://docs.google.com/document/d/1U6avvDw0LqogFPo48JNNuX4PH_mLE5Sm6bf_ra96DIY/edit)
3. [Sabina's Notebook](https://github.com/DARPA-ASKEM/program-milestones/blob/74-july-2024-scenario/Monthly_Scenarios_CIEMSS/July_2024_Scenario/July_2024_Notebook.ipynb)

### Begin Test
1. Login to https://app.staging.terarium.ai using the test account
    ```
    email: qa@test.io
    password: askem-quality-assurance
    ```
2. Create, or open, project named `Demo July 18`

### 1. Upload Resources
1. [SEIRHD Model](**[https://github.com/liunelson/mira/blob/nliu/experiment/notebooks/nliu/monthlydemo202407Q1.json](https://github.com/liunelson/mira/blob/nliu/experiment/notebooks/nliu/monthlydemo202407Q1.json)**)
2. [Cases and deaths dataset for LA county](https://github.com/DARPA-ASKEM/program-milestones/blob/74-july-2024-scenario/Monthly_Scenarios_CIEMSS/July_2024_Scenario/LA_county_covid_data.csv)
3. [US Cases and deaths by state and county](https://github.com/nytimes/covid-19-data/blob/master/us-counties-2021.csv)

> *Note*
> 
> You can either process the US Cases and deaths data showcasing the data transformation workflow or you can upload the Cases and deaths data for LA county that has already been processed. 

### 2. Process Dataset: Data Transformation Workflow
1. Create a new workflow named `Process Dataset`and add the `US Cases and Deaths` dataset.
2. Add the `Data Transformation` operator and connect it to the dataset.
3. Edit the dataset using the _Notebook_. 		
   1. Filter the data for Los Angeles County, California
       ```python
       filtered_df = df[(df['county'] == "Los Angeles") & (df['state'] == "California")]
       ```
   2. Convert the date column to `datetime`
   3. Print the row for `2021-10-23` to estimate `Recovered population` and `Deaths`
       ```python
       print(filtered_df[filtered_df['date'] == '2021-10-23'])
       ```
   4. Sort the data by date
   5. Filter the data between the specified dates:
      1. `start_date = '2021-10-28'`
      2. `end_date = '2021-12-28'`
   6. Drop unwanted columns
   ```python
      final_df.reset_index(drop=True, inplace=True)
      final_df.index.name = 'Timestamp'
      final_df = final_df[['cases', 'deaths']]
      final_df.head()
    ```
   7. Save the `final_df`as a new dataset and name this as `LA County Covid Data`
 
### 3. Configure the Model
1. Create a new workflow canvas and name this `Model Configuration`
2. Add the `SEIRHD Model`to the workflow canvas an connect this to the `model configuration` operator. Configure the model using estimated parameter values and initial conditions at the 2021-10-28 and save this as default configuration. Below are initial conditions and parameters 
	1. Set the initial conditions to the following (based on Sabina's Notebook):
		1. "S" Susciptable
			1. S_u = 2324633.64
			2. S_v = 5977629.36
		2. "I" Infected
			1. I_u = 3458.66666666667
			2. I_v = 1729.33333333333
		3. "E" Exposed
			1. H = 673
			2. R = 1484896
			3. D = 26604
		
	2. Set parameters with uncertainty
		1. N (unit: person)
			1. Constant
			2. Constant =  9830000
		2. NPI_mult
			1. Uniform
			2. Min = .999
			3. Max = 1.001
		3. beta
			1. Uniform
			2. Min = .09
			3. Max = .11
		4. vacc_mult
			2. Constant = .5
		5. r_EI
			1. Uniform
			2. Min = .1
			3. Max = .25
		6. r_IH_u
			1. Uniform
			2. Min = .005
			3. Max = .0055
		7. r_IH_v
			1. Uniform
			2. Min = .001
			3. Max = .0015
		8. r_HR
			1. Uniform
			2. Min = .007
			3. Max = .015
		9. r_HD
			1. Uniform
			2. Min = .007
			3. Max = .015
		10. r_IR_u
			1. Uniform
			2. Min = .1
			3. Max = .2
		11. r_IR_v
			1. Uniform
			2. Min = .15
			3. Max = .2
	3. Save as `Initial Config` and return to the workflow canvas.
	4. Add the `Simulate with PyCIEMSS`operator and connect with the `Initial Config.`Open the editor and set the simulation parameters to the following:
		1. Start time = 0
		2. End time = 151
		3. Number of samples = 100
		4. Method = dopri5
		
### 4. Calibrate and Forecast Model
1. Create a new workflow and name this `Calibrate and Forecast Model`
2. Add the `SEIRHD` model and the `LA County Covid Data`
3. Add the `Model Configuration` operator and connect this to the `SEIRHD model`.
4. Add  the `Calibrate with PyCIEMSS`operator and connect the `SEIRHD model` and the `LA County Covid Data.` Open the operator to set up the calibration.
4. Map the model variable to the dataset variable by selecting `Add mapping.`You can also select `automap` to do this automatically; however you will need to verify that the mappings are correct.
		1. Map dataset  to the appropriate model variables: C to cases and D to deaths
5. Set the number of samples to 100, iterations to 100, and end time to 100 and run calibration.
6. View the loss chart, showing the loss value as a function of the solver iterations. Save as new dataset `Calibrated Model Result`
7.  Add the `Simulate with PyCIEMSS` operator to the workflow canvas and connect this to the `SEIRHD Model` and the `Configured Result` from the previous step. Set the simulation parameters as following:
	1. Start time =  0 
	2. End time = 100 
	3. Number of samples =  100 .
	4. Method = dopri5
8. Run the simulation, and save as a new data set named `Simulate Model Result.`
9. Add the `Calibrated Model Result` and `Simulate Model Result` to the workflow canvas and connect the two datasets with the `transform dataset` operator.
	1. Interact with the LLM or write code to plot the calibrate and simulate outputs on the same chart. 
	2. Interact with the LLM or write code to plot the number of hospitalizations for the `Simulate Model Result`
	3. View plots from  the 90 day forecast for the previous task. Determine if any day is greater than or equal to 3000. 
	4. You should also view the `90 day forecast`data set to quickly see the min and max values for `H and C.`Make a note of these values as you will need to refer back to them in a later task. 

