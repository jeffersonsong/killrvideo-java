#!/bin/bash

# setting environment variable to the IP address of the host
#export KILLRVIDEO_BACKEND=`ipconfig getifaddr en0`
#export KILLRVIDEO_DOCKER_IP=`ipconfig getifaddr en0`
export KILLRVIDEO_BACKEND=$(ip addr show docker0 | grep -Po 'inet \K[\d.]+')
export KILLRVIDEO_DOCKER_IP=$(ip addr show docker0 | grep -Po 'inet \K[\d.]+')

# the compose file swaps in the value of `KILLRVIDEO_BACKEND` in several places
docker-compose -p killrvideo-java -f docker-compose-backend-external.yaml up -d

