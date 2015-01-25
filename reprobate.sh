#!/bin/bash

function start() {
    echo "### Starting Reprobate"
    nohup java -cp reprobate.jar:$(echo lib/*.jar | tr ' ' ':') server.ReprobateServer > app.log 2>&1 &
}

function stop() {
    echo "### Stopping Reprobate"
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
    echo 'Usage: reprobate.sh {status|start|stop|restart}'
else
    case "$1" in
        'start')
        start
    ;;
        'stop')
        stop
    ;;
        'restart')
        stop
        start
    ;;
        'status')
        status
    ;;
    esac
fi
