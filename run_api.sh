#!/bin/bash
java -jar -Djasypt.encryptor.password=? -Dspring.profiles.active=production --enable-preview /opt/volleyball-referee-api-3.1.jar
