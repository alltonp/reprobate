package app.restlike.common

object Script {
  def install(app: String, token: String) =
    (s"""#!/bin/bash
      |#INSTALLATION:
      |#- alias $app='{path to}/$app.sh'
      |#- that's it!
      |
      |HOST="http://${java.net.InetAddress.getLocalHost.getHostName}:8473"
      |""" + """OPTIONS="--timeout=15 --no-proxy -qO-"
      |WHO=`id -u -n`
      |BASE="""" + app + """/$WHO"
      |TOKEN="""" + token + """"
      |REQUEST="$OPTIONS $HOST/$BASE/$TOKEN"
      |MESSAGE="${@:1}"
      |clear
      |RESPONSE=`wget $REQUEST --tries=1 --post-data="{\"value\":\"${MESSAGE}\"}" --header=Content-Type:application/json`
      |if [ $? -ne 0 ]; then
      |  echo "sorry, """ + app + """ seems to be unavailable right now, please try again later"
      |else
      |  echo "$RESPONSE"
      |fi
      |echo
      |`wget -qO.""" + app + """.bak $HOST/""" + app + """/state/$TOKEN`
      |
    """).stripMargin.split("\n").toList
}
