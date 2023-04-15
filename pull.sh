#!/bin/bash

cd $BOT_DIR

git pull origin stable

rm target/*.jar
mvn package
pkill java

jarfile=$(find target/ -name zyenyo-*.*.*.jar)

java -cp $jarfile zyenyo.Zyenyo $BOT_TOKEN $MONGO_URI $ZYENYO_ENVIRONMENT &
