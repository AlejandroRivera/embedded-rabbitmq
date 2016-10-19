#!/usr/bin/env bash

echo "Any RabbitMQ process found?"
pids=`pgrep beam.smp`        # Erlang process that runs RabbitMQ
if [ "$?" == "1" ]
then
    echo "No process found."
else
    echo "RabbitMQ is already running! pid: $pids"
    exit 1
fi

echo "$!"

echo "Executing test that will exit without stopping the Embedded RabbitMQ process..."
mvn -f ../../../../pom.xml clean test -Dtest=RabbitMqProcessWontDieTest > execution.log &
mvnPid=$!
sleep 10        # in ~10 seconds, the embedded rabbitmq process should have started. Adjust if download isn't cached

echo "RabbitMQ processes:"
pid=`pgrep beam.smp`        # Erlang process that runs RabbitMQ
if [ "$?" == "1" ]
then
    echo "No process found. Did the rabbitmq broker start? Does the sleep need adjustment? Check the execution.log file."
    exit 1
else
    echo "RabbitMQ process found with pid: $pid"
    ps -p $pid
fi

echo "Waiting for mvn to finish..."
wait $mvnPid
echo "Maven exit status: $?"

sleep 5
echo "Is RabbitMQ still running?"

pids=`pgrep beam.smp`        # Erlang process that runs RabbitMQ
if [ "$?" == "1" ]
then
    echo "No process found."
else
    echo "Uh-oh! RabbitMQ is still running! $pids"
    exit 1
fi