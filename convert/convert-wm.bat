@echo off
rem Licensed to the Apache Software Foundation (ASF) under one
rem or more contributor license agreements.  See the NOTICE file
rem distributed with this work for additional information
rem regarding copyright ownership.  The ASF licenses this file
rem to you under the Apache License, Version 2.0 (the
rem "License"); you may not use this file except in compliance
rem with the License.  You may obtain a copy of the License at
rem
rem   http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing,
rem software distributed under the License is distributed on an
rem "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
rem KIND, either express or implied.  See the License for the
rem specific language governing permissions and limitations
rem under the License.    

if "%JAVA_HOME%" == "" goto javahomeerror
if "%1" == "" goto noargserror

set _CLASSPATH=%CLASSPATH%

for %%f in (..\bin\vel*.jar) do call buildcp %%f
for %%f in (..\build\lib\ant*.jar) do call buildcp %%f

echo %CLASSPATH%
%JAVA_HOME%\bin\java.exe org.apache.velocity.convert.WebMacro %1 %2 %3 %4 %5 %6 %7 %8 %9
goto end

REM -----------ERROR-------------
:javahomeerror
echo "ERROR: JAVA_HOME not found in your environment."
echo "Please, set the JAVA_HOME variable in your environment to match the"
echo "location of the Java Virtual Machine you want to use."
goto end

:noargserror
echo "ERROR: Need a template or a directory of templates to convert!"
goto end

:end
set CLASSPATH=%_CLASSPATH%
set _CLASSPATH=

pause
