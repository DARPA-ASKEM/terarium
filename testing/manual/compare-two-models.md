## Compare Two Models
Please go through __every__ step of the test scenario.\
When blocked, an error, or a UI/UX anomaly occurs, please report which scenario and step to [\#askem-testing](https://unchartedsoftware.slack.com/archives/C06FGLXB2CE).

Estimated time to completion: [X] minutes

### 1. Begin test
1. Login to https://app.staging.terarium.ai using the test account
    ```
    email: qa@test.io
    password: askem-quality-assurance
    ```
2. Create, or open, project named `QA [Your Name] [YYMMDD]`

### 2. Load some models to compare
1. Switch to the Explorer 
2. Set the search mode to **Models**
3. Search for **covid masking**
4. __There is currently a bug where each model is duplicated and the first of each duplicate is broken. The second Bertozzi2020 model also returns an error message, but oddly it loads anyway__
   Add the first two models to your project.
   - Wan2020 - risk estimation and prediction of the transmission of COVID-19 in maninland China excluding Hubei province
   - Bertozzi2020 - SIR model of scenarios of COVID-19 spread in CA and NY

### 3. Create a new workflow to compare them
1. Switch back to your project `QA [Your Name] [YYMMDD]`
2. Create a new workflow and rename it to **Compare models test**
3. Drag the Wan2020 and Bertozzi2020 models onto the canvas.
4. Right-click or use the `Add components` dropdown and select **Work with model > Compare models**
5. Pipe them both into the `Compare models` operator and click **Open**


### 4. Working with the Compare models drilldown: WIZARD
1. The first thing you'll see is the **Wizard** tab.
2. The first section `Overview` will probably say: Analyzing models metadata to generate a detailed comparison analysis.... 
3. It should change to a full paragraph within ten seconds. If it doesn't there's a problem.
4. Scroll down the page and confirm there are two columns, one for each model. Do the diagrams display correctly? Is there content in each row? 

### 5. Working with the Compare models drilldown: NOTEBOOK
1. Switch to the **Notebook** tab.
2. Type "Compare models" into the AI assistant (or try some of the other suggestions in the dropdown) and press the **Send** icon (or enter)
3. It should generate some Python code. 
4. Click **Run**
5. It should genearte a crazy looking hairball of a graph. Click on it to view it in a full screen image viewer, zoom in and out as required.


