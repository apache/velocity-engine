#! /bin/sh

if [ "$JAVA_HOME" = "" ] ; then
  echo You must set JAVA_HOME to point at your Java Development Kit directory
  exit 1
fi

# convert the existing path to unix
if [ "$OSTYPE" = "cygwin32" ] || [ "$OSTYPE" = "cygwin" ] ; then
   CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
fi

CLASSPATH=$CLASSPATH:../../../build/lib/ant-1.2.jar
CLASSPATH=$CLASSPATH:../../../build/lib/ant-1.2-optional.jar
CLASSPATH=$CLASSPATH:../../../build/lib/xerces-1.2.1.jar
CLASSPATH=$CLASSPATH:../../../build/lib/jdom-b5.jar
CLASSPATH=$CLASSPATH:../../../bin/velocity-0.5.jar

# convert the unix path to windows
if [ "$OSTYPE" = "cygwin32" ] || [ "$OSTYPE" = "cygwin" ] ; then
   CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
fi

BUILDFILE=build.xml

echo $CLASSPATH

java $ANT_OPTS -classpath "$CLASSPATH" org.apache.tools.ant.Main \
                -Dant.home=$ANT_HOME \
                -buildfile ${BUILDFILE} \
                 "$@"
