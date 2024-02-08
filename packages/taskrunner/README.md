# Terarium Task Runner

This is the async task runner for the Terarium Application

# Adding tasks to the `GoLLM Taskrunner` from [GoLLM](https://github.com/DARPA-ASKEM/GoLLM/tree/main/tasks):

- The `TaskRunnerInterface` class provides everything necessary for getting input into the task and output back through
the `taskrunner` to the `hmi-server`. Use existing tasks as a tempalte for how this is done.
- Add the `task_name.py` to the `tasks` directory in [GoLLM](https://github.com/DARPA-ASKEM/GoLLM/tree/main/tasks)
- Merge the changes into the `main` branch of [GoLLM](https://github.com/DARPA-ASKEM/GoLLM/tree/main/tasks)
- Publish a new `gollm-taskrunner` docker image.

# Building docker image:

From `terarium` root:

```sh
docker build --tag=ghcr.io/darpa-askem/gollm-taskrunner:latest --file=./packages/taskrunner/docker/Dockerfile.GoLLM .
```
