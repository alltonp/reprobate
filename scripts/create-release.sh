#!/bin/sh

#TIP: https://raw.githubusercontent.com/terryburton/travis-github-release/master/github-release.sh

REPO="alltonp/reprobate"

JSON=$(cat <<EOF
{
  "tag_name":         "$TRAVIS_BUILD_NUMBER",
  "target_commitish": "master",
  "name":             "$TRAVIS_BUILD_NUMBER release",
  "draft":            false,
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
echo $RESULT
RELEASEID=`echo "$RESULT" | sed -ne 's/^  "id": \(.*\),$/\1/p'`
echo $RELEASEID

if [[ -z "$RELEASEID" ]]; then
  echo FAILED
  echo "$RESULT"
  exit 1
fi
echo DONE

'curl -XPOST -s -H "Authorization: token ${GITHUBTOKEN}" -H "Content-Type: application/zip" --data-binary @./target/dist.zip "https://uploads.github.com/repos/alltonp/reprobate/releases/$RELEASEID/assets?name=Reprobate_0.0.$TRAVIS_BUILD_NUMBER.zip"'
