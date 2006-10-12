# !/bin/sh 

# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.    

if [ "$JAVA_HOME" = "" ] ; then
  echo You must set JAVA_HOME to point at your Java Development Kit directory
  exit 1
fi

# convert the existing path to unix
if [ "$OSTYPE" = "cygwin32" ] || [ "$OSTYPE" = "cygwin" ] ; then
   CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
fi

CLASSPATH=${JAVA_HOME}/lib/tools.jar

for i in ../../../build/lib/*.jar
do
    CLASSPATH=$CLASSPATH:"$i"
done

for i in ../../../bin/*.jar
do
    CLASSPATH=$CLASSPATH:"$i"
done

# convert the unix path to windows
if [ "$OSTYPE" = "cygwin32" ] || [ "$OSTYPE" = "cygwin" ] ; then
   CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
fi

CLASSPATH=${CLASSPATH}:../../../build/lib

BUILDFILE=build.xml

echo $CLASSPATH

java $ANT_OPTS -classpath "$CLASSPATH" org.apache.tools.ant.Main \
                -Dant.home=$ANT_HOME \
                -buildfile ${BUILDFILE} \
                 "$@"
