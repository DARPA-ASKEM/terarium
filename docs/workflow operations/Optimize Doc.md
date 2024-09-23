# Optimize:
## Goal
This is used to find the minimum or maximum value to a given parameter value or time that a QoI (Quality of Interest) constraint does not exceed the provided threshold.

We can ask questions to try to find out a good start time for an intervention. 
For example:
  "I want to turn on some masking intervention such that the parameter that impacts infected growth `beta` is set to the relatively smaller value of 0.1.
  I want to know how late I can start this masking while making the number of infected never exceed a specified number."

We can also ask questions to try to determine parameter values
For example:
  "I want to turn on a quarantine intervention in day 30 for 90 days. What will the value for the parameter that impacts infected growth `beta` need to be in order to keep the number of infected lower than a (specified threshold) number?"

## Knobs:
`End time` (Settings)
    The end time of the forecast run that is ran post optimization.
    The point of this forecast call is to give the user a clear visual to see their new policy in effect

`Number samples` (Settings)
    This is the number of samples for the forecast run that is ran post optimization.
    The point of this forecast call is to give the user a clear visual to see their new policy in effect

`Solver method` (Settings)
    This is disabled. This value will always be "dopri5" until more solver methods prove to be a useful thing to add.
    The solver method for the forecast run that is ran post optimization.

`Maxfeval` (Settings):
    Maxfeval is the number of times that the objective function is evaluated in the search for a minima/maxima.
    Note that this along with maxiter are easy ways to tweak an optimization to pass or fail
    Lowering it would lower our precision and chance of success, to the benefit of a short run time.
    This must be greater than 0.

`Maxiter` (Settings):
    Maxiter is the number of basinhopping iterations.
    This can be thought of as: when we find a local minima/maxima (with maxfeval), how many times do we try to escape the local minima/maxima before determining it is either the global minimum/maximum or good enough? 
    The higher this number is the less likely we will end at a local minima/maxima and more likely that we will find either a better answer or even the global minimum or maximum.
    This also means the higher the number the longer our optimization will take.
    Note that this along with maxfeval are easy ways to tweak an optimization to pass or fail.
    Lowering maxiter would lower both our accuracy and chance of success, to the benefit of a short run time.
    The minium value for this is 0, in this case we will accept the first found minima/maxima.

`Step Size` (Not exposed):
    This is not currently exposed through the API.
    The step size for each iteration is 30% of the longest euclidean distance between the lower and upper bounds of a given parameter.
    For example if we provide some parameter `beta` is [0.5, 1.5] then our step/hop size is a uniform random float between 0.0 and 0.3.

`Minimized` (Settings)
    This is a boolean that is being used to determine if we are looking for a minima or a maxima.
    :minimized = true, when I went to smallest values for some parameter that satisfy my constraint(s)
    :minimized = false, when I want the largest values for some parameter that satisfy my constraint(s)

`Intervention type`
    How you want to intervene on the parameter(s) you have selected.
    If this is set to `start time` you will have access to set the parameter's value
    If this is set to `parmeter value` you will have access to set the start time
    `Start time` Intervention Type:
        intervention type when you know what value a parameter should take but not when; 
        so, you'll want to find the optimal start time for an intervention.
            Example: 
            I want to turn on some masking intervention such that the parameter that impacts infected growth `beta` is set to the relatively smaller value of 0.1.
            I want to know how late I can start this masking while making the number of infected never exceed a specified number.
    `Parameter Value` Intervention Type:
        intervention type when you know when an intervention should take place but 
        you want to find the optimal value for this intervention.
            Example:
            I want to turn on a quarantine intervention in day 30 for 90 days. What will the value for the parameter that impacts infected growth `beta` need to be in order to keep the number of infected lower than a (specified threshold) number?

`Qoi Method` (Constraint)
    The QoI method specifies how the Quality of Interest is defined with respect to the selected variable.
    "Max" = the maximum of this variable over the entire time period
    "Day average" = the average of the last N time points. Currently N is set to 1 and is not being exposed to the user. 

`Target variables` (Constraint)
    The target variable that should be checked while running the optimization.
	In the future we may want to have multiple variables as well as multiple thresholds but at the moment ciemms cannot support this.

`Threshold` AKA `risk bound` (Constraint)
    The maximum tolerable or permissible probability that the selected QoI have a value above the given threshold.
    Low value = conservative but lower chance of success
    High value = risky but higher chance of success
    Examples on this can be found in the `Intervention type` section

`Risk tolerance`:
    Acceptible risk on on the threshold. This is a percent and should be between 0 and 100.
    This means if we were to ask: 
    I want to turn on a quarantine intervention in day 30 for 90 days. What will the value for the parameter that impacts infected growth `beta` need to be in order to keep the number of infected lower than a (specified threshold) number?
    If I set my risk to 5 then our resulting policy would keep our infected lower than the (specified threshold) 95% of simulations.


## Output Files:
`policy.json` 
    The resulting parameter value or starting time 

`optimize_results.json`
    A json serialized version of `optimize_results.dill` for use within the HMI.
    This can be found here: https://github.com/DARPA-ASKEM/pyciemss-service/blob/main/service/utils/tds.py#L233-L245

  `maxcv` is the maximum constraint violation (includes constraint violation for the risk constraint and the bound constraints)
  The error message means that the objective function was minimized successfully, but did not find an answer that satisfies the constraint given (like hosp below 3000 or whatever)
  `success` This will be false if the answer did not converge. You can have a result that satisfies the constraints but could be better given more iterations or function evaluations.

`optimize_results.dill`
    This is a status report from pyciemss that is passed on.
    Some of the information this contains is 
    whether or not the optimization was successful.
    A quick message as to why it did or didnt.
    the highest/lowest optimization result
    how many minization failures it had
    We are throwing this into a json formatter and keeping it in the output section above the chart

