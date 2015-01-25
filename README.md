<a href="https://travis-ci.org/alltonp/reprobate" target="_blank"><img src="https://travis-ci.org/alltonp/reprobate.png?branch=master"></a>

## Welcome to Reprobate - continuous system monitoring

### Quick start (if you already know what this is)

**Install:**

1. download and unzip <a href="https://github.com/alltonp/reprobate/releases/download/current/reprobate.zip">current version</a>
2. chmod +x reprobate.sh
3. ./reprobate.sh start
4. browse to http://localhost:8473
5. that's it!

**Configure your own checks:**

1. edit checks.csv
2. ./reprobate.sh restart
3. that's it!

### What is it?
- A simple monitor that loops through a supplied list of checks, alerting you in suitably annoying fashion should any checks fail. Ideally displayed on a huge monitor, for all to see.

### Why would I use it?
- You have a stack of applications/services and need to know they are working **all** the time.
- You want to be notified of production problems **before** your users report them
- You need to "keep it up without touching it" (tm) - you probably work for an investment bank and enjoy segregation of duties etc

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

### Can I configure how shouty it is etc?
- see instructions in checks.csv

-----
Copyright © 2014-2015 Flatmap Ltd