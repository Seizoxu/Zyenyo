#!/bin/bash

cd $BOT_DIR

#git pull origin stable

rm target/*.jar
mvn package
pkill -f "java -cp $jarfile zyenyo.Zyenyo $BOT_TOKEN $MONGO_URI $ZYENYO_ENVIRONMENT"

jarfile=$(find target/ -name zyenyo-*.*.*.jar)

echo "[TERM] Starting Zyenyo..."
java -cp $jarfile zyenyo.Zyenyo $BOT_TOKEN $MONGO_URI $ZYENYO_ENVIRONMENT

while true; do
    if [ -e "zbflag-restart" ]; then
        echo "[TERM] Restarting Zyenyo..."
        rm "zbflag-restart"
        pkill -f "java -cp $jarfile zyenyo.Zyenyo $BOT_TOKEN $MONGO_URI $ZYENYO_ENVIRONMENT"
        java -cp $jarfile zyenyo.Zyenyo $BOT_TOKEN $MONGO_URI $ZYENYO_ENVIRONMENT
    else
        echo "[TERM] Shutting Down..."
        break
    fi
done
