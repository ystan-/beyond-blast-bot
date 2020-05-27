#!/bin/bash
mvn clean compile package && \
  docker build -t ds:32768/bb:latest . && \
  docker push ds:32768/bb:latest
