Welcome to Velocity!

This directory contains a few examples to help get you started.

As always, the if you have any questions :

1) Make sure you followed any directions :)  (did you build everything?)
2) Review documentation included in this package, or online at
      http://jakarta.apache.org/velocity/index.html
3) Ask on the velocity-user list.  This is a great source of support information.
   To join, read http://jakarta.apache.org/site/mail.html and then follow the 
   link at the bottom to join the lists.

These examples are very easy to build, but there is a prerequisite for building
any Velocity software, a Java based build tool called 'Ant'.  It can be found at

   http://jakarta.apache.org/ant/

It's really a great tool.  Once ant is installed and working (it's easy, don't
worry...), you can build the example suite via :

Change to the /build directory in the Velocity distribution and then type :

  ant examples

This will invoke ant and tell it to build the examples for you.

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

servlet_example2
----------------
This is another example of how to use Velocity with servlets.
It is different from servlet_example1 in that it shows how to 
use the servlet environment to automatically set paths relative
to the webapplication root.  It also shows how to use the 
ClasspathResourceLoader to make template loading easy.

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

event_example
-------------
This is a more advanced example, and shows how to use the event handling
features of Velocity.  This is an advanced topic, so if you are just 
starting with Velocity, you can come back to it later.


Thanks for using Velocity!

- the Velocity team


