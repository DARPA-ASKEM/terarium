#!/bin/bash

# ensure the volume mounted python code is using editable mode
echo "Installing python tasks"
cd /mira_task
pip install --no-cache-dir -e .

# run it
echo "Running taskrunner"
cd /taskrunner
pip install --no-cache-dir -e .

BUILD_DIR=/taskrunner-build-mira
mkdir -p $BUILD_DIR
./gradlew --project-cache-dir /tmp/.gradle/$$ -PcustomBuildDir=$BUILD_DIR bootRun
