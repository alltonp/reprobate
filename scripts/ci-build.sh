#!/bin/sh
sbt dist assembly

#docker build --no-cache  -t reprobate-`date + "%T"` .
docker build --no-cache  -t reprobate-1 .

#docker stop reprobate-current ||:
#docker rm reprobate-current ||:

docker run --name reprobate-current -t -d \
-p 8473:8473 \
reprobate-1