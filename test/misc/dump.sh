#!/bin/sh

CLASSPATH=.:../../bin/classes

for jar in ../../build/lib/*.jar
do
    CLASSPATH=${CLASSPATH}:${jar}
done

java -cp ${CLASSPATH} org.apache.velocity.test.view.TemplateNodeView $1 > output.dump
