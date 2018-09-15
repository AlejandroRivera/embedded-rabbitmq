#!/usr/bin/env bash
export JAVA_HOME=`/usr/libexec/java_home -v 1.7`
mvn release:clean release:prepare -Prelease
mvn release:perform
