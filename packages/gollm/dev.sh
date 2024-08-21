#!/bin/bash

# ensure the volume mounted python code is using editable mode
echo "Installing python tasks"
cd /gollm_task
pip3 install -e .

# run it
echo "Running taskrunner"
cd /taskrunner
pip3 install -e .

BUILD_DIR=/taskrunner-build-gollm
mkdir -p $BUILD_DIR
./gradlew --project-cache-dir /tmp/.gradle/$$ -PcustomBuildDir=$BUILD_DIR bootRun
