#!/bin/sh

CLASSPATH=.:../bin/classes

for jar in ../build/lib/*.jar; do
    CLASSPATH=${CLASSPATH}:${jar}
done

# Tests can be run in either GUI or CLI mode.
# values: junit.textui.TestRunner | junit.swingui.TestRunner
TEST_RUNNER=junit.swingui.TestRunner

java -cp ${CLASSPATH} \
    ${TEST_RUNNER} \
    org.apache.velocity.test.VelocityTest \
    > output 2>&1
