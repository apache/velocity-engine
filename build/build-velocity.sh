#!/bin/sh
#-----------------------------
# Script for building Velocity
#-----------------------------

TARGET=${1}

#-------------------------------------------------------------------

LIB=lib

# Libs needed for build.
ANT=${LIB}/ant.jar
ANTXML=${LIB}/xml.jar
XERCES=${LIB}/xerces-1.1.3.jar

# Libs needed for runtime.
LOG=${LIB}/log.jar
JAVACLASS=${LIB}/JavaClass.jar

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

if test -f ${JAVA_HOME}/lib/tools.jar ; then
    CLASSPATH="${CLASSPATH}:${JAVA_HOME}/lib/tools.jar"
fi

CP=${CLASSPATH}:${ANT}:${ANTXML}:${ANTLRALL}:${XERCES}:${XALAN}:${SB}:${LOG}:${FOP}:${LIB}:${W3C}:${JAVACLASS}

echo "Now building ${TARGET}..."

echo "Classpath: ${CP}"

BUILDFILE=build-velocity.xml

${JAVA_HOME}/bin/java -classpath ${CP} \
                       org.apache.tools.ant.Main \
                       -buildfile ${BUILDFILE} ${TARGET}









