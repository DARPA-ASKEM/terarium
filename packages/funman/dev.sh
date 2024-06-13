#!/bin/bash

# ensure the volume mounted python code is using editable mode
echo "Installing python tasks"
cd /funman_task
pip install -e .

# run it
echo "Running taskrunner"
cd /taskrunner
./gradlew --project-cache-dir /tmp/.gradle/$$ bootRun
