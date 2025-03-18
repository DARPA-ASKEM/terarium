## Model errors
Check to see if Terarium has the capability to tease out potential problems with a given AMR model.

Report any issues into GitHub: [open an issue](https://github.com/DARPA-ASKEM/terarium/issues/new?assignees=&labels=bug%2C+Q%26A&template=qa-issue.md&title=%5BBUG%5D%3A+).


### 0. Initial setup
This [google drive](https://drive.google.com/drive/folders/1TWX96QmPHKkyGlYKX4lNsBnQCbsSKtwY) contains a list of syntatically valid but semantically incorrect models.

Each model can be imported into Terarium but will pose poential processing problem at various points.


### 1. Model with potential to underflow or overflow
Import `no-mass.json`, once done goto its model page. You should see warnings about mass not conserved in the transition section of the model page.


### 2. Model-level error/warning
Import `sir-bad-initials.json`, once done goto its model page. You should see a model-level error about mismatching states.


### 4. Model whose parameter is misinterpreted as a state
Impport `param-state.json`. Once this is compplete, goto the model page. Under the "State variables" section you should see a warning that `theta` may have been misclassified as a state, along with instructions to address this warning.

### 5. Model which contains grouped controllers can be simplified
Import `SIR Modified need simplification.json`. Once this is complete go to a model page (model asset, model drilldown, model edit, stratify) and ensure you see a warning bar suggesting that this model can be simplified. Click the link to create a new asset that is simplified and ensure that this new model has been added to your project.
