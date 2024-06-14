#!/bin/bash

# ensure the volume mounted python code is using editable mode
echo "Installing python tasks"
cd /gollm_task
pip install -e .

# run it
echo "Running taskrunner"
cd /taskrunner
pip install -e .
./gradlew --project-cache-dir /tmp/.gradle/$$ bootRun
