#!/bin/sh
#-----------------------------
# Script for building Velocity
#-----------------------------

#-------------------------------------------------------------------

LIB=./lib

# Libs needed for build.
ANT=${LIB}/ant-1.2.jar
ANTOPTIONAL=${LIB}/ant-1.2-optional.jar
XERCES=${LIB}/xerces-1.2.1.jar
FOP=${LIB}/Fop.class
XSLT=${LIB}/Xslt.class

#--------------------------------------------
# No need to edit anything past here
#--------------------------------------------
if test -z "${JAVA_HOME}" ; then
    echo "ERROR: JAVA_HOME not found in your environment."
    echo "Please, set the JAVA_HOME variable in your environment to match the"
    echo "location of the Java Virtual Machine you want to use."
    exit
fi

# convert the existing path to unix
if [ "$OSTYPE" = "cygwin32" ] || [ "$OSTYPE" = "cygwin" ] ; then
   CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
fi

if test -f ${JAVA_HOME}/lib/tools.jar ; then
    CLASSPATH="${CLASSPATH}:${JAVA_HOME}/lib/tools.jar"
fi

CLASSPATH=${CLASSPATH}:${ANT}:${ANTOPTIONAL}:${XERCES}:${XALAN}
CLASSPATH=${CLASSPATH}:${FOP}:${XSLT}

# convert the unix path to windows
if [ "$OSTYPE" = "cygwin32" ] || [ "$OSTYPE" = "cygwin" ] ; then
   CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
fi

BUILDFILE=build-velocity.xml

echo $CLASSPATH

${JAVA_HOME}/bin/java -classpath ${CLASSPATH} \
                       org.apache.tools.ant.Main \
                       -buildfile ${BUILDFILE} "$@"
