#!/bin/bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-19.jdk/Contents/Home/
set -e
cd src/client;
quasar build;
cd ../../;
mvn clean package -DskipTests;
cp target/satgag-0.0.1-SNAPSHOT.jar .
