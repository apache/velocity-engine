Welcome to Velocity!

This directory contains a few examples to help get you started.

As always, the if you have any questions :

1) Make sure you followed any directions :-) (did you build
   everything?)

2) Review documentation included in this package, or online at
   http://velocity.apache.org/

3) Ask on the velocity-user list.  This is a great source of support
   information.  To join, read http://velocity.apache.org/contact.html
   and then follow the links to join the lists.


These examples are very easy to build, but there is a prerequisite for
building any Velocity software, installing Apache ant, which can be
downloaded from

   http://ant.apache.org/

Change to the /build directory in the Velocity distribution and then type:

  ant examples

This will invoke ant and tell it to build the examples for you.

Finally, note that more examples of using Velocity can be found in the
Velocity-Tools subject.  Of special note is the VelocityViewServlet,
a quick and easy way to build a web application that uses Velocity.

   http://velocity.apache.org/tools/


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

context_example
--------------
This is a demonstration of 2 different context implementations :
 - a context implementation that uses a database as the storage.
   Use the DBContextTest program to test.  See the DBContextTest.java file for the
   db table info.  Unsupported demonstration code.  You may need to futz with
   it to get it to work in your environment.  To run, use the dbcontexttest.sh
   script.
 - a context implementation that uses a TreeMap for storage.  Very simple.

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
