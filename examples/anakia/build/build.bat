REM
REM  Thanks to Peter Donald <donaldp@apache.org>
REM  for providing this and lcp.bat, so we don't 
REM  have to hunt down a win32 box :)
REM 

if "%JAVA_HOME%" == "" goto javahomeerror

set ANT_HOME=..\..\..\bin

set LOCALCLASSPATH=
for %%i in (..\..\..\build\lib\*.jar) do call lcp.bat %%i
for %%i in (..\..\..\bin\*.jar) do call lcp.bat %%i

echo %LOCALCLASSPATH%

%JAVA_HOME%\bin\java.exe -classpath "%LOCALCLASSPATH%" org.apache.tools.ant.Main -Dant.home=%ANT_HOME% %1 %2 %3 %4 %5 %6 %7 %8 %9

goto end

:javahomeerror
echo "ERROR: JAVA_HOME not found in your environment."
echo "Please, set the JAVA_HOME variable in your environment to match the"
echo "location of the Java Virtual Machine you want to use."

:end
