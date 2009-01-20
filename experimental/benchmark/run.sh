#!/bin/bash

# Front end for running the benchmark.  This both compiles and runs the Benchmark class.
# Before running make sure you have run ant so that it creates the bin directory to
# access the necessary libraries.

# If you place a velocity.jar file in this directory, then it will benchmark against
# that jar, otherwise it will use the compiled classes in the bin/classes directory.

# This script assumes that javac and java are available at the command line

ROOT=../..
LIBDIR=$ROOT/bin/lib

[[ ! -d $LIBDIR ]] && echo Oops, directory $LIBDIR does not exit, make sure you run ant && exit 1

COMMON_COLL=$LIBDIR/commons-collections-3.2.1.jar
[[ ! -f $COMMON_COLL ]] && echo Oops, $COMMON_COLL does not exist && exit 1

COMMON_LANG=$LIBDIR/commons-lang-2.4.jar
[[ ! -f $COMMON_LANG ]] && echo Oops, $COMMON_LANG does not exit && exit 1

VELOCITY_PATH=velocity.jar
if [[ ! -f $VELOCITY_PATH ]]; then
  VELOCITY_PATH=$ROOT/bin/classes
  echo velocity.jar was not found in this directory, so we are using classes in:
  echo $VELOCITY_PATH
else
  echo Found $VELOCITY_PATH in the current directory, so we are going to use it!  
fi

JRAT_JAR=shiftone-jrat.jar
if [[ -f $JRAT_JAR ]]; then
  echo Found $JRAT_JAR, We are going to use it!
  echo Adding -javaagent:shiftone-jrat.jar to the java command line
  JRAT_SWITCH=-javaagent:shiftone-jrat.jar
fi

CP=.:$COMMON_LANG:$COMMON_COLL:$VELOCITY_PATH

COMPILE="javac -cp $CP Benchmark.java"

if ! $COMPILE ; then
  echo Ooops, failed to compile, comand line:
  echo $COMPILE
  exit 1
fi

time java -server -Xmx50M $JRAT_SWITCH -cp $CP Benchmark
