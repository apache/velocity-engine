#!/bin/sh

CLASSPATH=.

for jar in *.jar
do
    CLASSPATH=${CLASSPATH}:${jar}
done

java -cp ${CLASSPATH} org.apache.velocity.test.misc.Test $1 > output 2>&1
