@echo off

set VELCP=.
for %%i in (..\..\bin\*.jar) do set VELCP=%VELCP%;%%i

echo Using classpath:  %VELCP%

java -cp %VELCP% LoggerExample

