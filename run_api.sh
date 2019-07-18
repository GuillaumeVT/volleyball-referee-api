#!/bin/bash
java -jar -Djasypt.encryptor.password=${JASYPT_KEY} -Dspring.profiles.active=production --enable-preview /opt/volleyball-referee-api-3.1.jar
