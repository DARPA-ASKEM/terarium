#!/bin/bash

# ensure the volume mounted python code is using editable mode
echo "Installing python tasks"
cd /mira_task
pip install -e .

# run it
echo "Installing taskrunner"
cd /taskrunner
pip install -e .

BUILD_DIR=/taskrunner-build-mira
mkdir -p $BUILD_DIR
./gradlew --project-cache-dir /tmp/.gradle/$$ -PcustomBuildDir=$BUILD_DIR bootRun
