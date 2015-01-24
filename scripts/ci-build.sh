#!/bin/sh

sbt test dist "+ publishSigned" sonatypeReleaseAll
