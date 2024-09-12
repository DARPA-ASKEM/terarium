## Compare Models
Please go through __every__ step of the test scenario.\
Report any issues into GitHub: [open an issue](https://github.com/DARPA-ASKEM/terarium/issues/new?assignees=&labels=bug%2C+Q%26A&template=qa-issue.md&title=%5BBUG%5D%3A+).

### 1. Begin test
1. Login to https://app.staging.terarium.ai using the test account
    ```
    email: qa@test.io
    password: askem-quality-assurance
    ```
2. Create, or open, project named `QA [Your Name]`

### 2. Upload assets
1. Add [Bertozzi2020 Document](https://drive.google.com/file/d/1PcqIf75e9llNshCDZDt3L6mIdhUmLMvE/view?usp=sharing) to your project
2. Add [Fang2020 Document](https://drive.google.com/file/d/1YPyRKA3pmjNpVH5egpds4Vm3RnVyP2LI/view?usp=sharing) to your project
3. Add [Tang2020 Document](https://drive.google.com/file/d/1njXxZ9ZoMqnZqobW4eVDVVfKvyLbfLRh/view?usp=sharing) to your project
4. Add [Bertozzi2020 Model](https://drive.google.com/file/d/1_YL1861KSkq08aEUOoh8Z2MknfLd4cmc/view?usp=sharing) to your project
5. Add [Fang2020 Model](https://drive.google.com/file/d/13YXIS58MSGfMJzgTid3MaX0vwwgmV2X7/view?usp=sharing) to your project
6. Add [Tang2020 Model](https://drive.google.com/file/d/1Terd--gDArLMqCaG5Cn-_2-6do145tWv/view?usp=sharing) to your project
7. Wait for extractions to finish for uploaded document

### 3. Compare models without model cards
1. Create a new workflow
2. Add the Bertozzi2020, Fang2020, and Tang2020 models to the workflow
3. Create a new "Compare models" operator
4. Attach the Bertozzi2020, Fang2020, and Tang2020 models to the "Compare models" operator
5. Open the compare models drilldown
6. Ensure that when the drill-down starts:
   - The overview begins to generate
   - The model graphs are displayed
7. When the overview is generated, it should compare the structure of the AMR models.
   - It should have an overview of the models
   - It should look at the semantic information of all the models
   - It should state that there is a lack of metadata information to make other comparisons
   - It should have a meaningful conclusion

### 3. Compare models with partial model cards
1. Enrich the Bertozzi2020 and Fang2020 models with their respective documents 
2. Create a new "Compare models" operator
4. Attach the Bertozzi2020, Fang2020, and Tang2020 models to the new "Compare models" operator
5. Open the drilldown
6. Ensure that when the drill-down starts:
   - The overview begins to generate
   - The model graphs are displayed
   - Model card information is displayed for Bertozzi2020 and Fang2020
7. When the overview is generated, it should compare both the structure of the AMR models and the metadata information.
  - It should have an overview of the models
  - It should look at semantic information of all the models
  - It should compare the metadata information for the Bertozzi2020 and Fang2020 models
  - It should have a meaningful conclusion


### 4. Compare models all with model cards
1. Finally, enrich the Tang2020 models with its respective document
2. Create a new "Compare models" operator
4. Attach the Bertozzi2020, Fang2020, and Tang2020 models to the new "Compare models" operator
5. Open the drilldown
6. Ensure that when the drill-down starts:
  - The overview begins to generate
  - The model graphs are displayed
  - Model card information is displayed for all models
7. When the overview is generated, it should only compare the metadata information from each model and not look at the structure.
   - It should have an overview of the models
   - It should compare the metadata and semantic information for all models
   - It should have a meaningful conclusion

### 5. Working with the Compare models drilldown: NOTEBOOK
1. Switch to the **Notebook** tab.
2. Type "Compare models" into the AI assistant and press the **Send** icon (or enter)
3. It should generate some Python code.
4. Click **Run**
5. It should generate three graphs that compare each model against each other:
   - Bertozzi2020 vs. Fang2020
   - Bertozzi2020 vs. Tang2020
   - Fang2020 vs. Tang2020
