#!/bin/sh

[ -z $1 ] && echo \
          && echo 'Need a template or a directory of templates to convert!' \
          && echo \
          && exit

for jar in ../bin/vel*.jar
do
    CLASSPATH="${jar}:${CLASSPATH}"
done

java -classpath "${CLASSPATH}" org.apache.velocity.convert.WebMacro $*
