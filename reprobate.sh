#!/bin/bash

function start() {
    nohup java -cp reprobate.jar:$(echo lib/*.jar | tr ' ' ':') server.ReprobateServer > app.log 2>&1 &
}

function stop() {
    PID=`currentpid`
    if [[ -n $PID ]]; then
        echo "### Killing PID ${PID}...."
        kill $PID
    else
        echo "### Reprobate wasn't running..."
    fi
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

if [ $# -lt 1 ]; then
    echo 'Usage: reprobate.sh {status|start|stop}'
else
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
fi
