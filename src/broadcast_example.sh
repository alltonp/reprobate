APP="app"
VERSION="version"
MACHINE_NAME="machine"
DEPLOYER=`id -u -n`
MESSAGE="(${APP}) ${VERSION} deployed to ${MACHINE_NAME} by ${DEPLOYER}"
wget --timeout=15 --no-proxy -O- --post-data="{\"messages\":[\"${MESSAGE}\", \"Thats it\"]}" --header=Content-Type:application/json "http://localhost:8473/broadcast"
