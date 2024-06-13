#!/bin/bash

# ensure the volume mounted python code is using editable mode
echo "Installing python tasks"
cd /mira_task
pip install -e .

# kick of the build server
echo "Starting taskrunner build server"
cd /taskrunner
./gradlew build --continuous --quiet -x test &
pid=$!
sleep 1

# run it
echo "Running taskrunner"
./gradlew bootRun

kill $pid
sleep 1
