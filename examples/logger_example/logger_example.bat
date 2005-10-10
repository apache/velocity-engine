@echo off

set VELCP=.

for %%i in (..\..\bin\*.jar) do call appendVELCP %%i
for %%i in (..\..\build\lib\*.jar) do call appendVELCP %%i

echo Using classpath:  %VELCP%

java -cp %VELCP% LoggerExample

