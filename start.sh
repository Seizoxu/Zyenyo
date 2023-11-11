#!/bin/bash

cd $BOT_DIR

logstamp=$(date +%Y%m%d-%H%M%S)
mkdir -p logs/

BOT_DIR=$BOT_DIR BOT_TOKEN=$BOT_TOKEN MONGO_URI=$MONGO_URI ZYENYO_ENVIRONMENT=$ZYENYO_ENVIRONMENT ./pull.sh > logs/$logstamp.log 2>&1 &
