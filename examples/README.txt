Welcome to Velocity!

This directory contains a few examples to help get you started.

As always, the if you have any questions :

1) Make sure you followed any directions :)  (did you build everything?)
2) Review documentation included in this package, or online at
      http://jakarta.apache.org/velocity/index.html
3) Ask on the velocity-user list.  This is a great source of support information.
   To join, read http://jakarta.apache.org/site/mail.html and then follow the 
   link at the bottom to join the lists.


To build these examples easily, you can just use the build script provided in the 
distribution.  In the build directory (contained in the root directory of the
distribution) :

Under unix    :   ./build.sh examples   
Under windows :   build.bat examples

Velocity Examples
=================

app_example1 
------------
This simple example shows how to use the Velocity Template Engine
in a standalone program.  It should be pre-compiled for you. Run it using the example
template provided (example.vm):

  ./example.sh

app_example2 
------------
Another simple example showing how to use Velocity in a standalone
program.  This examples uses the org.apache.velocity.util.Velocity application utility
class, which provides a few convenient methods for application programmers.  It also
should be precompiled for you.  To run :

  ./example2.sh

servlet_example1
---------------
This is a simple example of how Velocity can be used with
 your favorite servlet engine. To use this, you should have a basic understanding
how your servlet engine works, and how you deploy and access servlets to try this.
While we cannot supply this, we know of a good one :)
   http://jakarta.apache.org/tomcat

context_example
--------------
This is a demonstration of 2 different context implementations :
 - a context implementation that uses a database as the storage.
   Use the DBContextTest program to test.  See the DBContextTest.java file for the 
   db table info.  Unsupported demonstration code.  You may need to futz with 
   it to get it to work in your environment.  To run, use the dbcontexttest.sh 
   script.
 - a context implementation that uses a TreeMap for storage.  Very simple.

logger_example
--------------
This is a toy program to show how any class can be used as a logging
facility with Velocity.

xmlapp_example
--------------
This is simple example that demonstrates direct access of XML data via 
Velocity templates, as well as Velocimacro recursion.

Thanks for using Velocity!

- the Velocity team


