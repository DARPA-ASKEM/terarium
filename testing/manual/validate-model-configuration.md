## Validate Model Configuration
Please go through __every__ step of the test scenario.\
Report any issues into GitHub: [open an issue](https://github.com/DARPA-ASKEM/terarium/issues/new?assignees=&labels=bug%2C+Q%26A&template=qa-issue.md&title=%5BBUG%5D%3A+).

### 1. Begin test
1. Login to https://app.staging.terarium.ai using the test account
    ```
    email: qa@test.io
    password: askem-quality-assurance
    ```
2. Create, or open, project named `QA [Your Name]`

### 2. Setup the workflow
1. Create a new workflow named `Validate Model Configuration`
2. Upload the [Configured SIR.json](https://github.com/DARPA-ASKEM/terarium/tree/main/testing/data) model
3. Drag and drop the model from the Resources panel onto the workflow
4. Create a _Configure Model_ operator in the workflow by right-clicking anywhere in workflow canvas and selecting _Work with model/Validate configuration_
5. Link the output port of the _Model_ operator to the model port of the _Configure Model_ operator
6. Open the _Configure Model_ operator
7. Verify that the model is reasonably configured by inspecting the value of the initial conditions and parameters, e.g.\
   `S = 1000, I = 2, R = 0, β = 0.6, γ = 0.2`
8. Change the type of the parameters to a _Uniform_ distribution with some reasonable lower and upper bounds, e.g \
  `β ∈ [0.4, 0.8], γ ∈ [0.1, 0.3]`
9. Give a name and description to this configuration
10. Save this configuration by clicking _Run_
11. Note that this configuration is now available in the _Suggested configurations_ section and appears as the output port of the operator

### 3. Setup the Validate operator
1. Create a _Validate configuration_ operator by right-clicking anywhere in the workflow canvas and selecting _Work with model/validate configuration_
2. Link the output port of the _Configure model_ operator to the _Model configuration_ input port of the _Validate configuration_ operator
3. Open the _Validate configuration_ operator by clicking _Review checks_
4. Click _Show additional options_ to edit the settings of this operator
5. A _Tolerance_ input and slider should be available \
    - set the value to `0.05` (this determines how granular the operator segment the input space of the model for validation)
7. A _parameters of interest_ dropdown should be available, populated with the parameters and state variables of the model
    - select `β` and `γ` (this determines for which inputs the operator should run the validation)

### 4. Add sanity checks
1. Ensure that the _Compartmental constraint_ is present (it requires the operator to check that the sum of the state variables at all times is equal to their sum at time = 0 and that each state variable is positive) \
    `I + R + S = 1002.0, I >= 0, R >= 0, S >= 0`
2. Create a new sanity check that ensures the number of infected persons is less than `750.0` in the first `100` days by first clicking _Add another constraint_
3. Name it `Infected cap` and set its type to `State constraint` \
4. Set the _Target_ to `I` and _Upper bound_ to `750.0`, leaving all other options as default

### 5. Successful Validation
1. Click _Run_
2. Select `I` in _Trajectory State_ dropdown
3. Note that the time-series chart show the evolution of the model variables as a function of time, with one trajectory per sampled points in parameter space (coloured green when they satisfy all sanity checks, yellow otherwise)
4. Verify that the x-axis of this chart include the given _Start time_ and _End time_ (`0` and `100`) and that the trajectories have as many points as specified by _Number of steps_ (`10`)
5. Verify that there is a semi-transparent green rectangle (`width = [0, 100] days, height = [0, 750]`), representing the region in which a trajectory needs to be for satisfiability
6. Select the parameter space plot associated with the `γ` parameter under _Configuration parameters_
7. Verify that a _β:γ pairwise drilldown_ chart appears and selecting each of the rectangular regions (labelled `box<N>`) highlights a trajectory in the _Trajectory State_ chart (both the selected rectangle and highlighted trajectory should shared the same colour)
8. Verify that the x-axis is labelled `β` with the same range as configured (`[0.4, 0.8]`) and ditto for the y-axis (`γ` and `[0.1, 0.3]`)
9. Verify that every green rectangular region maps to a green trajectory which is always between `I = 0.0` and `I = 750.0`
10. Click _X_ on the _drilldown_ chart should close it

### 6. Tighten sanity check
1. Lower the upper bound of the `Infected cap` constraint from `750.0` to `100.0` (this should restrict the regions of parameter space that can satisfy this sanity check)
2. Click `Run` again
3. Verify that orange trajectories appear in the _Trajectory State_ chart and they violate the newly tightened sanity check (i.e. `I(t) > 100.0` at some time `t`)
4. Verify that orange rectangular regions appear in the parameter space plots and they map to the orange trajectories
5. Delete this constraint by clicking the _Bin_ icon

### 7. Create a monotonicity constraint
1. Create a new constraint named `Sus` with type `Monotonicity constraint` and target `S`
2. Set it to _Decreasing_ to check whether the state variable `S` always decrease at every time point
3. Click _Run_
4. Select `I` in the _Trajectory State_ dropdown
5. Verify that all parameter space regions are coloured green and map to trajectories that always decrease in value over time, within the simulated time range (i.e. `dS/dt <= 0`)

### 8. Create an impossible monotonicity constraint
1. Change the direction of the `Sus` constraint from `Decreasing` to `Increasing`
2. Click _Run_ again
3. Verify that all parameter space regions are coloured orange and all trajectories do not satisfy the new constraint, i.e. no combination of parameter values as input into the model produces outputs with `S` increasing over time
