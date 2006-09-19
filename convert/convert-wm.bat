@echo off
rem Copyright 2000-2006 The Apache Software Foundation.
rem
rem Licensed under the Apache License, Version 2.0 (the "License")
rem you may not use this file except in compliance with the License.
rem You may obtain a copy of the License at
rem
rem     http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing, software
rem distributed under the License is distributed on an "AS IS" BASIS,
rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem See the License for the specific language governing permissions and
rem limitations under the License.

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
