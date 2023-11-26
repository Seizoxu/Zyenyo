#!/bin/bash

cd $BOT_DIR

get_logstamp() {
    date +%Y%m%d-%H%M%S
}

# Pull from stable, package, and run, outputting into a distinct log file.
deploy() {
    logfile=logs/$(get_logstamp).log
    mkdir -p logs/

    echo "Logs written to: logs/$logfile"

    git pull origin stable >> "$logfile" 2>&1

    rm target/*.jar 2>&1
    mvn package >> "$logfile" 2>&1
    pkill -f "java -cp $jarfile zyenyo.Zyenyo $BOT_TOKEN $MONGO_URI $ZYENYO_ENVIRONMENT"

    jarfile=$(find target/ -name zyenyo-*.*.*.jar)
    echo "JAR file: $jarfile" >> "$logfile" 2>&1

    echo "[TERM] Starting Zyenyo..." >> "$logfile" 2>&1
    java -cp $jarfile zyenyo.Zyenyo $BOT_TOKEN $MONGO_URI $ZYENYO_ENVIRONMENT >> "$logfile" 2>&1
}

# Run once first.
deploy

# After shutdown of above, restarts if restart flag is present.
while true; do
    if [ -e "zbflag-restart" ]; then
        rm "zbflag-restart"
        deploy
    else
        break
    fi
done
