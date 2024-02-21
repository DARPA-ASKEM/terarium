# Terarium Task Runner

This is the async task runner for the Terarium Application

# How it works:

![](diagram.svg)

The taskrunner uses RabbitMQ as it's sole interface. It consumes on requests and produces responses through RabbitMQ.

Each request to the taskrunner represents the execution of work. This may be a python program or a standalone executable. *The work that the taskrunner is capable of doing must be built into the taskrunner image.*

When the taskrunner receives a request, it will execute the provided script as a new process. It will then write any provided input bytes into a [named pipe](https://en.wikipedia.org/wiki/Named_pipe) created specifically for the child process. The child process will be provided two arguments: `--input-pipe` and `--output-pipe` and is responsible for reading input from the `--input-pipe` and writing output to the `--output-pipe`.

The taskrunner will communcate the output back through RabbitMQ to be consumed by another service (ex. `hmi-server`).

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
