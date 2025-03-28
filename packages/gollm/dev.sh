#!/bin/bash

# ensure the volume mounted python code is using editable mode
echo "Installing python tasks"
cd /gollm
pip install --no-cache-dir -e .

# run it
echo "Installing taskrunner"
cd /taskrunner
pip install --no-cache-dir -e .

BUILD_DIR=/taskrunner-build-gollm
mkdir -p $BUILD_DIR
./gradlew --project-cache-dir /tmp/.gradle/$$ -PcustomBuildDir=$BUILD_DIR bootRun
