#!/bin/bash
#INSTALLATION:
#- alias rim='{path to}/rim.sh'
#- that's it!

RIM_HOST="http://Pauls-MacBook-Pro.local:8473"
OPTIONS="--timeout=15 --no-proxy -qO-"
WHO=`id -u -n`
BASE="rim/$WHO"
REQUEST="$OPTIONS $RIM_HOST/$BASE"
MESSAGE="${@:1}"
RESPONSE=`wget $REQUEST --post-data="{\"value\":\"${MESSAGE}\"}" --header=Content-Type:application/json`

printf "\n$RESPONSE\n\n"
