#!/bin/sh

(
  cd jar-contents
  rm -f ../test.jar
  rm *~
  jar cvf ../test.jar *
)  
