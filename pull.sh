#!/bin/bash

cd $BOT_DIR

git pull origin stable

mvn package
pkill java

jarfile=$(find target/ -name zyenyo-*.*.*.jar)

java -cp $jarfile zyenyo.Zyenyo $BOT_TOKEN $MONGO_URI &
