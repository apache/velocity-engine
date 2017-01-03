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

echo "DBContextTest : please ensure MySQL is set up and jdbc drivers are in classpath. See DBContextTest.java for clues"
echo "This is an unsupported demo."

_VELCP=.

for i in ../lib/*.jar
do
    _VELCP=$_VELCP:"$i"
done

for i in ../*.jar
do
    _VELCP=$_VELCP:"$i"
done

for i in ../../lib/*.jar
do
    _VELCP=$_VELCP:"$i"
done

for i in ../../*.jar
do
    _VELCP=$_VELCP:"$i"
done

java -cp $_VELCP org.apache.velocity.example.DBContextTest dbtest.vm

