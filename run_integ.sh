#!/bin/bash
java -jar -Djasypt.encryptor.password=${JASYPT_KEY} -Dspring.profiles.active=integration --enable-preview build/libs/volleyball-referee-api-3.1.jar
