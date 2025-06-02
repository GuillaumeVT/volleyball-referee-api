#!/bin/bash

docker build -t vbr-api -f .docker/app/Dockerfile .
#docker build -t vbr-nginx -f .docker/nginx/Dockerfile .docker/nginx