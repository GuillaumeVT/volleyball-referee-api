#!/bin/bash
java -jar -Dspring.profiles.active=integration --enable-preview build/libs/volleyball-referee-api-3.0.jar
