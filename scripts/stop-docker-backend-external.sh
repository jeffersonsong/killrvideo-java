#!/bin/bash

# command to bring down supporting KillrVideo infrastructure started via
# `run-docker-backend-external.sh`

docker-compose -p killrvideo-java -f docker-compose-backend-external.yaml down