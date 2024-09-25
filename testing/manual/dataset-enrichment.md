## Dataset enrichment with/without a document
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
1. Dataset [SIR_Dataset](https://drive.google.com/file/d/1wdCLKKznHaoCg1gWI7q7OO8W4F7zOjpc/view?usp=drive_link)
2. The same Dataset using a different name [SIR_Dataset](https://drive.google.com/file/d/1wdCLKKznHaoCg1gWI7q7OO8W4F7zOjpc/view?usp=drive_link)
3. Document [SIR_Document](https://drive.google.com/file/d/1GYyRrxs2Nd8BsU0fGzYW8hJ8CulK5AIY/view?usp=drive_link)
4. Wait for the document extractions to finish for the uploaded document

### 3. Test dataset enrichment without a document
1. Open the Dataset
2. Enrich the description without a document
3. __Expected Result__: The process go through without any error
4. __Expected Result__: the rows should be enriched as:

| ID | Name | Description                                                                                                                           | Concept                        | Unit     | Datatype | Stats                              |
|----|------|---------------------------------------------------------------------------------------------------------------------------------------|--------------------------------|----------|----------|------------------------------------|
| t  | T    | The time variable, likely representing the number of days since the beginning of an observation or experiment.                        | time                           | Days     | numeric  | 0 - 44.77 - 89.5477386934673       |
| s  | S    | The proportion of the population that is susceptible to an infection or condition, typically in the context of an SIR model.          | susceptible to infection of    | Unitless | numeric  | 0.143079518218444 - 0.68 - 0.99995 |
| i  | I    | The proportion of the population that is currently infected with an infection or condition, typically in the context of an SIR model. | infection incidence proportion | Unitless | numeric  | 0.00005 - 0.08 - 0.218551123932438 |
| r  | R    | The proportion of the population that has recovered from an infection or condition, typically in the context of an SIR model.         | recovered population           | Unitless | numeric  | 0 - 0.24 - 0.810127005400101       |


### 4. Test dataset enrichment with a document
1. Open the duplicated dataset
2. Enrich the description with a document
3. __Expected Result__: Column information is updated with more details (description, concept, unit, etc) and the link to the document (`SIR.pdf`) is added to the `Provenance` section.
4. __Expected Result__: the rows should be enriched as:

| ID | Name | Description                                                                                                                                            | Concept                        | Unit       | Datatype | Stats                              |
|----|------|--------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------|------------|----------|------------------------------------|
| t  | T    | The number of days since the beginning of the epidemic outbreak observation period                                                                     | observation period             | Days       | numeric  | 0 - 44.77 - 89.5477386934673       |
| s  | S    | The proportion of the population that is susceptible to the infection but not yet infected at time t                                                   | susceptible to infection of    | Proportion | numeric  | 0.143079518218444 - 0.68 - 0.99995 |
| i  | I    | The proportion of the population that is currently infected and can spread the infection at time t                                                     | infection incidence proportion | Proportion | numeric  | 0.00005 - 0.08 - 0.218551123932438 |
| r  | R    | The proportion of the population that has been removed from the spread of infection, including recovered, deceased, and isolated individuals at time t | recovered population           | Proportion | numeric  | 0 - 0.24 - 0.810127005400101       |
