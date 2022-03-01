#!/bin/bash

java -jar -Dspring.profiles.active=dev target/health-rest2-2.0.jar --spring.config.location=file:config/ 
