#!/bin/bash

# ensure the volume mounted python code is using editable mode
echo "Installing python tasks"
cd /funman_task
pip install -e .

# run it
echo "Installing taskrunner"
cd /taskrunner
pip install -e .

BUILD_DIR=/taskrunner-build-funman
mkdir -p $BUILD_DIR
./gradlew --project-cache-dir /tmp/.gradle/$$ -PcustomBuildDir=$BUILD_DIR bootRun
