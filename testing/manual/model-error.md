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

