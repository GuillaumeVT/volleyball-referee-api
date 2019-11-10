#!/bin/bash
export JASYPT_KEY=`cat /opt/jasypt_key`
java -jar --enable-preview /opt/volleyball-referee-api-3.2.jar --spring.profiles.active=production
