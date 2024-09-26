#!/bin/bash

# ensure the volume mounted python code is using editable mode
echo "Installing python tasks"
cd /table_extraction_task
pip3 install -e .

# run it
echo "Installing taskrunner"
cd /taskrunner
pip3 install -e .

# make the log directory
mkdir /var/log/supervisor

echo "Starting supervisord"
supervisord -c /table_extraction_task/supervisord.conf
