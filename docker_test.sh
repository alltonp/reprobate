#!/bin/sh
./sbt assembly
docker build --no-cache  -t reprobate-1 .
docker stop reprobate-current ||:
docker rm reprobate-current ||:
docker run --name reprobate-current -t -d -h=`hostname` \
-p 8473:8473 \
reprobate-1