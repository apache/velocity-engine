#!/bin/sh

for i in ../../build/lib/*.jar
do
    _VELCP=$_VELCP:"$i"
done

for i in ../../bin/*.jar
do
    _VELCP=$_VELCP:"$i"
done


java -cp $_VELCP XMLTest xml.vm

