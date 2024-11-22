## Scenario Templates  

Follow __every__ step in the test scenario carefully.  
If you encounter any issues, please report them on GitHub: [Open an Issue](https://github.com/DARPA-ASKEM/terarium/issues/new?assignees=&labels=bug%2C+Q%26A&template=qa-issue.md&title=%5BBUG%5D%3A+).  

---

### 1. Begin Test  

1. Log in to [Terarium Staging](https://app.staging.terarium.ai) using the test account:  
    ```  
    email: qa@test.io  
    password: askem-quality-assurance  
    ```  
2. Create or open a project named `QA [Your Name]`.  
3. Use or upload a model and dataset from [Google Drive](https://drive.google.com/drive/folders/1hjxiggCkBCofjCQgf9gXZEHBLkBqaVwe).  

---

### 2. Blank Canvas Template  

1. Click the **"+ New"** button next to the workflows in the sidebar.  
2. A modal should appear with a list of scenario templates presented as radio buttons.  
3. Ensure the default template selected is **Blank Canvas**.  
4. Enter a name for your workflow in the autofocused name field and press **Enter**.  
5. Verify that a workflow is created with the name you entered.  

---

### 3. Situational Awareness Template  

#### 3.a. Set Up Data  

1. Drag a model onto the blank canvas to create a new workflow.  
2. Attach the model to a **Configure Model** node and open the configuration.  
3. Make changes to the configuration as desired and save it as a new model configuration with a unique name.  

#### 3.b. Create Situational Awareness Template  

1. Click the **"+ New"** button next to the workflows in the sidebar.  
2. Select the **Situational Awareness** radio button.  
3. A new template form should appear.  
4. Fill out the form:  
   - Select your model, dataset, and the new model configuration you created.  
   - Choose output metrics: **I** and **R**.  
5. Click **Create** to generate the template.  
6. Verify the template includes the following connected nodes:  
   - **Model**  
   - **Configure Model**  
   - **Dataset**  
   - **Calibrate**  
7. Confirm the selected data from the form is reflected in the respective nodes.  

#### 3.c. Run Calibration and Verify Output Metrics  

1. Open the **Calibrate** node.  
2. Set the **Timepoint Variable** to `t` and map **I** to `I_obs`.  
3. Press **Run**.  
4. Verify that the **Variables Over Time** section displays the selected output metrics (**I** and **R**).  

---
