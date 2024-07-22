## Model enrichment with/without a document
Please go through __every__ step of the test scenario.\
Report any issues into GitHub: [open an issue](https://github.com/DARPA-ASKEM/terarium/issues/new?assignees=&labels=bug%2C+Q%26A&template=qa-issue.md&title=%5BBUG%5D%3A+).

### 1. Begin test
1. Login to https://app.staging.terarium.ai using the test account
    ```
    email: qa@test.io
    password: askem-quality-assurance
    ```
2. Create, or open, project named `QA [Your Name]`

### 2. Test Model enrichment without a document
1. Upload _the model_ [SIR_Model](https://drive.google.com/file/d/1OwKCQTKSflTW4oAbdCQ8xGXeUYmbCryG/view?usp=drive_link)
2. Go to SIR Model (uploaded and created model) from the resource panel
3. Click `Enrich description` -> Click `Enrich description` from the modal
4. __Expected Result__: The process go through without any error (TODO: add more details on expected results)

### 3. Test Model enrichment with a document
1. Upload _the model_ and _the document_ ([SIR_Model](https://drive.google.com/file/d/1OwKCQTKSflTW4oAbdCQ8xGXeUYmbCryG/view?usp=drive_link), [SIR_Document](https://drive.google.com/file/d/1GYyRrxs2Nd8BsU0fGzYW8hJ8CulK5AIY/view?usp=drive_link)). (Note: you might want to wait until the pdf extraction from the document upload is done before proceeding, otherwise next steps might throw errors. See [Bug](https://github.com/DARPA-ASKEM/terarium/issues/3521))
2. Go to the newly uploaded and created SIR Model from the resource panel
3. Click `Enrich description` -> Select the uploaded document (`SIR.pdf`) -> Click `Enrich description`.
4. __Expected Result__: Description is updated with more details (authors, provenance, etc) and the link to the document (`SIR.pdf`) is added to the `Provenance` section

### 4. Test Variable extraction with a document for the model
1. Re do steps 1-2 from the above test, **3**
2. Click `Extract variables` -> Select the uploaded document (`SIR.pdf`) -> Click `Use Document to extract variables`
4. __Expected Result__: The process go through without any error (TODO: add more details on expected results)
