#!/bin/bash

APP="Reprobate"

function start() {
    echo "### Starting $APP"
    nohup java -cp $(echo lib/*.jar | tr ' ' ':') server.$APP > app.log 2>&1 &
}

function stop() {
    echo "### Stopping $APP"
    PID=`currentpid`
    if [[ -n $PID ]]; then
        echo "### Killing PID ${PID}...."
        kill $PID
    else
        echo "### $APP wasn't running..."
    fi
}

function status() {
    PID=`currentpid`
    if [[ -n $PID ]]; then
        echo "### $APP is running..."
    else
        echo "### $APP is NOT running..."
    fi
}

function currentpid() {
    ps -ef | awk '/[R]eprobate/{print $2}'
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
