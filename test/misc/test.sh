#!/bin/sh

CLASSPATH=.:../../bin/classes

for jar in ../../build/lib/*.jar
do
    CLASSPATH=${CLASSPATH}:${jar}
done

java -cp ${CLASSPATH} org.apache.velocity.test.misc.Test $1 $2 > output 2>&1
