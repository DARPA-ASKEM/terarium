## Dataset enrichment with/without a document
Please go through __every__ step of the test scenario.\
When blocked, an error, or a UI/UX anomaly occurs, please report which scenario and step to [\#askem-testing](https://unchartedsoftware.slack.com/archives/C06FGLXB2CE).

Estimated time to completion: [X] minutes

### 1. Begin test 
1. Login to https://app.staging.terarium.ai using the test account
    ```
    email: qa@test.io
    password: askem-quality-assurance
    ```
2. Create, or open, project named `QA [Your Name]`


### 2. Test dataset enrichment without a document
1. Upload _the dataset_ [SIR_Dataset](https://drive.google.com/file/d/1wdCLKKznHaoCg1gWI7q7OO8W4F7zOjpc/view?usp=drive_link)
2. Go to SIR dataset (uploaded and created dataset) from the resource panel
3. Click `Enrich description` -> Click `Enrich description` from the modal
4. __Expected Result__: The process go through without any error
5. __Expected Result__: the rows should be enriched as:

| ID | Name | Description                                                                                                                           | Concept                        | Unit     | Datatype | Stats                              |
|----|------|---------------------------------------------------------------------------------------------------------------------------------------|--------------------------------|----------|----------|------------------------------------|
| t  | T    | The time variable, likely representing the number of days since the beginning of an observation or experiment.                        | time                           | Days     | numeric  | 0 - 44.77 - 89.5477386934673       |
| s  | S    | The proportion of the population that is susceptible to an infection or condition, typically in the context of an SIR model.          | susceptible to infection of    | Unitless | numeric  | 0.143079518218444 - 0.68 - 0.99995 |
| i  | I    | The proportion of the population that is currently infected with an infection or condition, typically in the context of an SIR model. | infection incidence proportion | Unitless | numeric  | 0.00005 - 0.08 - 0.218551123932438 |
| r  | R    | The proportion of the population that has recovered from an infection or condition, typically in the context of an SIR model.         | recovered population           | Unitless | numeric  | 0 - 0.24 - 0.810127005400101       |


### 3. Test dataset enrichment with a document
1. Upload _the dataset_ and _the document_ ([SIR_Dataset](https://drive.google.com/file/d/1wdCLKKznHaoCg1gWI7q7OO8W4F7zOjpc/view?usp=drive_link), [SIR_Document](https://drive.google.com/file/d/1GYyRrxs2Nd8BsU0fGzYW8hJ8CulK5AIY/view?usp=drive_link)).
2. Open up the notification panel and wait for the document extractions to finish (Otherwise next steps might throw errors. See [Bug](https://github.com/DARPA-ASKEM/terarium/issues/3523))
3. Go to the newly uploaded and created SIR Dataset from the resource panel
4. Click `Enrich description` -> Select the uploaded document (`SIR.pdf`) -> Click `Enrich description`.
5. __Expected Result__: Column information is updated with more details (description, concept, unit, etc) and the link to the document (`SIR.pdf`) is added to the `Provenance` section.
6. __Expected Result__: the rows should be enriched as:

| ID | Name | Description                                                                                                                                            | Concept                        | Unit       | Datatype | Stats                              |
|----|------|--------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------|------------|----------|------------------------------------|
| t  | T    | The number of days since the beginning of the epidemic outbreak observation period                                                                     | observation period             | Days       | numeric  | 0 - 44.77 - 89.5477386934673       |
| s  | S    | The proportion of the population that is susceptible to the infection but not yet infected at time t                                                   | susceptible to infection of    | Proportion | numeric  | 0.143079518218444 - 0.68 - 0.99995 |
| i  | I    | The proportion of the population that is currently infected and can spread the infection at time t                                                     | infection incidence proportion | Proportion | numeric  | 0.00005 - 0.08 - 0.218551123932438 |
| r  | R    | The proportion of the population that has been removed from the spread of infection, including recovered, deceased, and isolated individuals at time t | recovered population           | Proportion | numeric  | 0 - 0.24 - 0.810127005400101       |


### 5. End test
1. logout of the application 
