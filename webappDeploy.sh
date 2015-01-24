#!/bin/bash

function start() {
    nohup java -cp $(echo *.jar | tr ' ' ':') webserver.WebServer > app.log 2>&1 &
}

function stop() {
    PID=`currentpid`
    echo "Killing PID ${PID}...."
    kill $PID
}

function status() {
    PID=`currentpid`
    if [[ -n $PID ]]; then
        echo "WebServer is running..."
    else
        echo "WebServer is NOT running..."
    fi
}


function currentpid() {
    ps -ef | awk '/[w]ebserver/{print $2}'
}

case "$1" in
    'start')
    echo "Starting application"
    start
;;
    'stop')
    echo "Stopping application"
    stop
;;
    'status')
    status
;;
esac

