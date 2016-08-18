<a href="https://travis-ci.org/alltonp/reprobate" target="_blank"><img src="https://travis-ci.org/alltonp/reprobate.png?branch=master"></a>

### Reprobate - continuous assertion monitoring

![](https://media.giphy.com/media/l2SpRVOmmrHj7jlcc/giphy.gif)

### Quick start (if you already know what this is)

**Install:**

1. download and unzip <a href="https://github.com/alltonp/reprobate/releases/download/current/reprobate.zip">current binary</a>
2. chmod +x reprobate.sh
3. ./reprobate.sh start
4. browse to http://localhost:8473
5. that's it!

**Configure your own checks:**

1. edit checks.csv
2. ./reprobate.sh restart
3. that's it!

**Migrating between versions:**

1. install new version
2. copy checks.csv and state.json from previous version
3. that's it!

### What is it?
- A simple monitor that loops through a supplied list of "checks", alerting you in suitably annoying fashion should any checks fail. Ideally displayed on a huge monitor, for all to see.

### Why would I use it?
- You have a stack of applications/services and need to know they are working **all** the time.
- You want to be notified of production problems **before** your users report them
- You need to "keep it up without touching it" (tm) - you probably work for an investment bank and enjoy segregation of duties etc
- Generally we check our assertions only run at dev/build time - why wouldn't you want to run them all the time?

### What is a check?
- An assertion or invariant of your system, exposed as a JSON endpoint - adhering to the following contract:

	`GET {host}/check/xxx e.g. GET http://localhost:8080/check/alive`
	
- check successful, return an empty list of failures:

	`{"failures":[]}`

- check unsuccessful, return a list of (one or more) failures:

	`{"failures":["I am not alive"]}`

### What constitutes a good check?
- One expressed in business terms e.g. `check/reports/valid/today` or `check/can/price/trade`
- Calling it frequently should not negatively impact the application/service. If your check is expensive, abstract the expensive bit into another thing and have your check query that other thing.

### How do I configure it?
- see the examples in checks.csv in your reprobate installation (it is created on first run)

### Can I make is more/less shouty?
- see instructions for 'defcon level' in checks.csv

### All my checks fail when I deploy new versions of my stack...
- have your deployment scripts send a broadcast message to reprobate
- supply a message, environment and duration (in seconds) for the deploment
- reprobate will disable any probes running against that environment for the duration of the release
- e.g.

    ```
	APP="app"
	VERSION="version"
	MACHINE_NAME="machine"
	DEPLOYER=`id -u -n`
	MESSAGE="(${APP}) ${VERSION} deployed to ${MACHINE_NAME} by ${DEPLOYER}"

	wget --timeout=15 --no-proxy -O- --header=Content-Type:application/json
		--post-data="{\"messages\":[\"${MESSAGE}\"] \"env\":\"UAT\" \"durationSeconds\": 30}" "http://localhost:8473/broadcast"

	Or

	curl --connect-timeout 15 -H "Content-Type: application/json"
		-d "{\"messages\":[\"${MESSAGE}\"] \"env\":\"UAT\" \"durationSeconds\": 30}" http://localhost:8473/broadcast

    ```

### Does reprobate have an API?
- Reprobate hosts a 'check' for that - `http://{hostname}:8473/check/probes/ok/{query}`

	e.g.

	`http://localhost:8473/check/probes/ok/prod`

- all checks successful:

	`{"failures":[]}`

- some checks unsuccessful:

	```
	{
      "failures":[
      	"PROD Failure active between 10am and 1pm (Demo): I always let myself down",
      	"PROD Failure @ defcon 3 (Demo): I always let myself down"
      ]
    }
	```
- This is useful if you have multiple stacks monitored by reprobate you can aggregate them all into one uber reprobate, or you want to integrate with other monitoring tools.

-----

Copyright © 2015-2016 Spabloshi Ltd
