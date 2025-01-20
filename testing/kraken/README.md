### kraken
A simple runner to stress test Terarium and related services.

Kraken works on the notion of `profiles`, where each profile performs some set of tasks against Terarium endpoints. Kraken will take a run definition and execute these profiles in separate processes, simulating a heavy load environment. For example, we can spin up 5 instances of doing forecast-simulations and 3 instances of creating model from equations.


#### Prereq
- Rename `env.example` to `.env` and fill in the fields
- You will need dotenv if not already installed `pip install python-dotenv`


#### Usage
```
python ./kraken.py <profile>:<# instances> <profile>:<# instances> ...
```

for example:

```
# Spin up 25 instances of running amr-to-mmt task, and 10 instances of fetching random assets in a project
python ./kraken.py mmtAMR:25 browser:10
```
