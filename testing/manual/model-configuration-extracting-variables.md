## Extract configurations from document and dataset
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
1. From the manual testing [data folder](https://drive.google.com/drive/folders/14z6WAldQky0xgOAM69f6hakXhyHobYMe)
2. Add [SIR Document 1](https://drive.google.com/file/d/1ZQXSGhesif_G0bjQiB9nx0B_BJ1xV1Wx/view?usp=sharing) to your project
3. Add [SIR Document 2](https://drive.google.com/file/d/1U1h5YCBxom9k475QSOzkiIwyi5V159dV/view?usp=sharing) to your project
4. Add [SIR Model](https://drive.google.com/file/d/1F9UWuvwJPZY_XAGflsOIYy4Qrl56KS91/view?usp=sharing) to your project
5. Add [Stratified SIR model](https://drive.google.com/file/d/1r-zfKmHrOub7QT9BfdQEZabfZqveLhIq/view?usp=sharing) to your project
6. Add [SIDARTHE model](https://drive.google.com/drive/folders/14z6WAldQky0xgOAM69f6hakXhyHobYMe) to your project
7. Add [SIR timeseries Dataset 1](https://drive.google.com/file/d/1QM2TL6XrBHIlFOVSavugKAQErls7XtY8/view?usp=sharing) to your project
8. Add [SIR timeseries Dataset 2](https://drive.google.com/file/d/1zKySIpyPN_aa-icxy1e8NHmVtkZs51Fn/view?usp=sharing) to your project
9. Add [SIR contact matrix dataset](https://drive.google.com/file/d/17TByirv-LwunB0gDq46-Ww-SyJZYaHKD/view?usp=sharing) to your project
10. Wait for extractions to finish for uploaded document

### 3. Extracting configurations from multiple documents and timeseries datasets
1. Create a new workflow
2. Add the SIR model, both SIR documents, and both SIR timeseries datasets to the workflow
3. Create a new "Configure model" operator
4. Attach the SIR model to the "Configure model" operator
5. Attach the SIR documents and timeseries datasets to the "Configure model" operator
6. Extract configurations from the inputs
7. Ensure that the following configurations are extracted:
    - There are exactly 2 time-series configurations extracted from the SIR timeseries datasets
    - One should have the following values:
        - S = 10000
        - I = 100
        - R = 1
        - The source should be "SIR timeseries 1"
        - The parameters should be the same as the default configuration
    - The other should have the following values:
        - S = 1000
        - I = 1
        - R = 0
        - The source should be "SIR timeseries 2"
        - The parameters should be the same as the default configuration
    - There are several model configurations from the SIR Document 1
        - These will include countries such as Moldova, France, Spain, and Italy
        - There are several model configurations from the SIR Document 2
        - These will include countries such as China, South Korea, Australia, USA, and Italy
8. Ensure that the configuration sources are correctly populated and that they have reasonable titles and descriptions

### 4. Extracting configurations from model-mapping datasets
1. Add the Stratified SIR model and the SIR contact matrix dataset to the workflow
2. Create a new "Configure model" operator
3. Attach the Stratified SIR model to the "Configure model" operator
4. Attach the SIR contact matrix dataset to the "Configure model" operator
5. Extract configurations from the inputs
6. Ensure that the following configurations are extracted:
    - There is exactly 1 model-mapping configuration extracted from the contact matrix dataset
    - The configuration should have the following values:
        - Beta should have the following parameters:
            - β_0_18_0_18 = 0.4
            - β_0_18_19_45 = 0.09
            - β_0_18_46_89 = 0.08
            - β_19_45_0_18 = 0.07
            - β_19_45_19_45 = 0.3
            - β_19_45_46_89 = 0.06
            - β_46_89_0_18 = 0.05
            - β_46_89_19_45 = 0.04
            - β_46_89_46_89 = 0.2
        - Gamma and P should be 0s
        - The source should be "SIR contact matrix"
        - The initials should be the same as the default configuration

### 5. The gold standard - SIDARTHE model
1. Add the SIDARTHE model to the workflow 
2. Create a new "Configure model" operator 
3. Attach the SIDARTHE model and SIR Document 1 to the "Configure model" operator 
4. Extract configurations from the inputs 
5. Ensure that the following configurations are extracted:
    - There are 3 model configurations from the Document
        - These will include phases of the covid-19 epidemic in Italy
    - Initial values for S, I, D, A, R, T, H, and E should be extracted for all configurations
    - Parameter values for alpha, beta, delta, epsilon, eta, gamma, kappa, lambda, mu, nu, rho, theta, xi, zeta, tau, and sigma should be extracted for all configurations
