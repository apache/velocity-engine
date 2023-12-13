# Welcome to Velocity!

This package contains a few examples to help get you started.

As always, the if you have any questions :

1. Make sure you followed any directions :-) (did you build
   everything?)

2. Review documentation included in this package, or online at
   [https://velocity.apache.org/](https://velocity.apache.org/)

3. Ask on the [velocity-user list](https://velocity.apache.org/contact.html#user). 
   This is a great source of support information.  To join, read the
   [contact page](https://velocity.apache.org/contact.html) and then follow the links to join the lists.

## Building from sources

To build these examples from Velocity sources, please refer to the [Velocity build page](https://velocity.apache.org/engine/${project.version}/build.html). The build will generate the zip archive target/${project.build.finalName}-pkg.zip containing all the examples.

The archive contains a `build.sh` script which you can use to re-build the examples after tweaking the sources in the `src` directory.

## Running the examples

Once you have downloaded or successfully built the ${project.build.finalName}-pkg.zip downloaded package, unzip it in the location of your choice and change to the ${project.build.finalName} directory.

Note for Windows users: the shell scripts used to running the examples are meant for linux or BSD, but can easily be adapted as batch files. all they do is build the classpath from the jars in the lib/ directory, then invoke Java on the main class with the adequate arguments.

Finally, note that more examples of using Velocity can be found in the velocity-tools subject.  Of special note is the VelocityViewServlet, a quick and easy way to build a web application that uses Velocity.

   https://velocity.apache.org/tools/

# Velocity Examples

## Application Example #1

This simple example shows how to use the Velocity Template Engine
in a standalone program.  It should be pre-compiled for you. Run it using the example
template provided (`example.vm`):

    ./example1.sh

## Application Example #2

Another simple example showing how to use Velocity in a standalone
program.  This examples uses the org.apache.velocity.util.Velocity application utility
class, which provides a few convenient methods for application programmers.  It also
should be precompiled for you.  To run:

    ./example2.sh

## Context Example

This is a demonstration of 2 different context implementations:

- a context implementation that uses a database as the storage. You will need to copy the appropriate JDBC driver jar file in the lib/ directory, and to adapt the JDBC driver and credentials accordingly in the `src/org/apache/velocity/example/DBContextTest.java` file.

- a context implementation that uses a `TreeMap` for storage.  Very simple.

To run:

    ./dbcontexttest.sh

## Xml App example

This is simple example that demonstrates direct access of XML data via
Velocity templates, as well as Velocimacro recursion. To run:

    ./xmlapp_example.sh

## Event Example

This is a simple yet more advanced example, and shows how to use the event handling
features of Velocity.  This is an advanced topic, so if you are just
starting with Velocity, you can come back to it later. To run:

    ./event_example.sh

___

Thanks for using Velocity!

>the Velocity team
