#!/bin/sh

APP="app"
VERSION="version"
MACHINE_NAME="machine"
DEPLOYER=`id -u -n`
MESSAGE="(${APP}) ${VERSION} deployed to ${MACHINE_NAME} by ${DEPLOYER}"

#send using ...
#wget --timeout=15 --no-proxy -O- --post-data="{\"messages\":[\"${MESSAGE}\"]}" --header=Content-Type:application/json "http://localhost:8473/broadcast"
#curl --connect-timeout 15 -H "Content-Type: application/json" -d "{\"messages\":[\"${MESSAGE}\"]}" http://localhost:8473/broadcast
