#!/bin/sh

echo "Running JJTree ..."
jjtree Parser.jjt

echo "Running JavaCC ..."
javacc Parser.jj

# Remove the generated nodes as they are now
# in a package of their own.
rm -f AST*
rm -f Node.java
rm -f SimpleNode.java
rm -f ParserVisitor.java
