#!/bin/bash
export IMG=us.gcr.io/platform-solutions-dev/beyond-blast:1.2
mvn clean compile package && docker build -t $IMG . && docker push $IMG
