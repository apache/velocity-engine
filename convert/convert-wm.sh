#!/bin/sh

[ -z $1 ] && echo \
          && echo "Need a template or a directory of templates to convert!" \
          && echo \
          && exit

CLASSPATH=../bin/velocity-0.4.jar

java -cp ${CLASSPATH} org.apache.velocity.convert.WebMacro $1
