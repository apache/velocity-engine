@echo off

set VELCP=.
for %%i in (..\..\bin\*.jar) do set VELCP=%VELCP%;%%i

for %%i in (..\..\build\lib\*.jar) do set VELCP=%VELCP%;%%i

echo Using classpath:  %VELCP%

java -cp %VELCP% XMLTest xml.vm

