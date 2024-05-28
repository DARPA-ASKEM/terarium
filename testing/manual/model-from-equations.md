## Create model from equations
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

### 2. Create model
1. Open a new _Workflow_ and name it `Create model from equations`.
2. Add a _Create model from equations_ node.
3. Edit the node, add the following equations and create the model.
```latex
\frac{dS}{dt} = - \beta S I - \beta S I_v - v_r S
\frac{dV}{dt} = v_r S - \beta(1 - v_e) V I - \beta(1 - v_e) V I_v
\frac{dI}{dt} = \beta S I + \beta S I_v - \gamma I
\frac{dI_v}{dt} = \beta (1 - v_e) V I + \beta (1 - v_e) V I_v - \gamma I_v
\frac{dR}{dt} = \gamma I_v + \gamma I
```
4. Save the model.

### 3. End test
1. logout of the application
