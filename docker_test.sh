#!/bin/sh
#./autobuild.sh first ...
./sbt assembly
docker build --no-cache  -t reprobate-1 .
docker stop reprobate-current ||:
docker rm reprobate-current ||:
rm -rf /Users/pall/dev/paulos/reprobate/test-data
docker run --name reprobate-current -t -d -h=`hostname` \
-v /Users/pall/dev/paulos/reprobate/test-data:/data \
-p 8473:8473 \
reprobate-1

#issues:
#do data dir when -v
#need to do non root thing
