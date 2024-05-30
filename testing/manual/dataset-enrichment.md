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
4. __Expected Result__: The process go through without any error (TODO: add more details on expected results)

### 3. Test dataset enrichment with a document
1. Upload _the dataset_ and _the document_ ([SIR_Dataset](https://drive.google.com/file/d/1wdCLKKznHaoCg1gWI7q7OO8W4F7zOjpc/view?usp=drive_link), [SIR_Document](https://drive.google.com/file/d/1GYyRrxs2Nd8BsU0fGzYW8hJ8CulK5AIY/view?usp=drive_link)). (Note: you might want to wait until the pdf extraction from the document upload is done before proceeding, otherwise next steps might throw errors. See [Bug](https://github.com/DARPA-ASKEM/terarium/issues/3523))
2. Go to the newly uploaded and created SIR Dataset from the resource panel
3. Click `Enrich description` -> Select the uploaded document (`SIR.pdf`) -> Click `Enrich description`.
4. __Expected Result__: Column information is updated with more details (description, concept, unit, etc) and the link to the document (`SIR.pdf`) is added to the `Provenance` section.

### 5. End test
1. logout of the application 
