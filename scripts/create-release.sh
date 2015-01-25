#!/bin/sh

$REPO="alltonp/reprobate"

JSON=$(cat <<EOF
{
  "tag_name":         "$TRAVIS_BUILD_NUMBER",
  "target_commitish": "master",
  "name":             "$TRAVIS_BUILD_NUMBER: New release",
  "draft":            true,
  "prerelease":       false
}
EOF
)
RESULT=`curl -s -w "\n%{http_code}\n"     \
  -H "Authorization: token $GITHUBTOKEN"  \
  -d "$JSON"                              \
  "https://api.github.com/repos/$REPO/releases"`
if [ "`echo "$RESULT" | tail -1`" != "201" ]; then
  echo FAILED
  echo "$RESULT"
  exit 1
fi
RELEASEID=`echo "$RESULT" | sed -ne 's/^  "id": \(.*\),$/\1/p'`
if [[ -z "$RELEASEID" ]]; then
  echo FAILED
  echo "$RESULT"
  exit 1
fi
echo DONE