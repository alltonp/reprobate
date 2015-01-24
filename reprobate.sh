#!/bin/bash

function start() {
    nohup java -cp reprobate.jar:$(echo lib/*.jar | tr ' ' ':') server.ReprobateServer > app.log 2>&1 &
}

#TODO: don't try to kill missing PID, results in: ps -ef | awk '/[R]eprobateServer/{print $2}'
function stop() {
    PID=`currentpid`
    echo "### Killing PID ${PID}...."
    kill $PID
}

function status() {
    PID=`currentpid`
    if [[ -n $PID ]]; then
        echo "### Reprobate is running..."
    else
        echo "### Reprobate is NOT running..."
    fi
}

function currentpid() {
    ps -ef | awk '/[R]eprobateServer/{print $2}'
}

#TODO: default to status
case "$1" in
    'start')
    echo "### Starting Reprobate"
    start
;;
    'stop')
    echo "### Stopping Reprobate"
    stop
;;
    'status')
    status
;;
esac

