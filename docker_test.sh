#!/bin/sh
#./autobuild.sh first ...
./sbt assembly
docker build --no-cache  -t spabloshi/reprobate-1 .
docker stop reprobate-current ||:
docker rm reprobate-current ||:
rm -rf /Users/pall/dev/paulos/reprobate/test-data
docker run --name reprobate-current -t -d -h=`hostname` \
-v /Users/pall/dev/paulos/reprobate/test-data:/data \
-p 8473:8473 \
reprobate-1

#issues:
#need to do non root thing
#checks executed resetting to 0 on dynamic config change, not right ... state.json

#docker login
#docker tag reprobate-1 spabloshi/reprobate
#docker push spabloshi/reprobate