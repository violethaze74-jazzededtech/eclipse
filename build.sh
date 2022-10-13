#!/bin/bash

# Fail on any error.
set -e
# Display commands being run.
set -x

if [ "$1" == "release" ]; then
  mvn clean install
fi
