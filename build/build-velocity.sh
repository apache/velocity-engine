#!/bin/sh
#-----------------------------
# Script for building Velocity
#-----------------------------

#-------------------------------------------------------------------

LIB=lib

# Libs needed for build.
ANT=${LIB}/ant.jar
ANTXML=${LIB}/xml.jar
XERCES=${LIB}/xerces-1.1.3.jar
SERVLET=${LIB}/servlet.jar

# Libs needed for runtime.
LOG=${LIB}/log.jar
JAVACLASS=${LIB}/JavaClass.jar

# Lib needed for testing.
JUNIT=${LIB}/junit-3.2.jar

# Libs needed for docs.
XALAN=${LIB}/xalan_1_1_D01.jar
FOP=${LIB}/fop.jar
W3C=${LIB}/w3c.jar
SB=${LIB}/stylebook-1.0-b2.jar

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

CLASSPATH=${CLASSPATH}:${ANT}:${ANTXML}:${ANTLRALL}:${XERCES}:${XALAN}:${SB}:${LOG}:${FOP}:${LIB}:${W3C}:${JAVACLASS}:${SERVLET}:${JUNIT}

# convert the unix path to windows
if [ "$OSTYPE" = "cygwin32" ] || [ "$OSTYPE" = "cygwin" ] ; then
   CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
fi

BUILDFILE=build-velocity.xml

${JAVA_HOME}/bin/java -classpath ${CLASSPATH} \
                       org.apache.tools.ant.Main \
                       -buildfile ${BUILDFILE} "$@"









