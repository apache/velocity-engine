#!/bin/sh

[ -z $1 ] && echo \
          && echo 'Need a template or a directory of templates to convert!' \
          && echo \
          && exit

CLASSPATH=.

for jar in ../bin/vel*.jar ../build/lib/ant*.jar
do
    CLASSPATH=${CLASSPATH}:${jar}
done

java -classpath ${CLASSPATH} org.apache.velocity.convert.WebMacro $1
