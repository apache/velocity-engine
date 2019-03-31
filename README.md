
# Apache Velocity

Welcome to Apache Velocity Engine! Apache Velocity is a general purpose
template engine written in Java. For more information about Velocity,
please look at the HTML documentation on the Velocity web site:

  [http://velocity.apache.org/index.html](http://velocity.apache.org/index.html)

Here's a description of the top level directories:

    velocity-engine-core/       The Velocity Engine core module
    velocity-engine-examples/   Several simple examples
    velocity-engine-scripting/  JSR-223 implementation for Velocity scripting
    src/                        Source for parent modules, mainly changelog

## REQUIREMENTS

Apache Velocity 2.1 will run with any Java runtime engine v1.8 or greater.

Building from source requires Java development kit v1.8 or greater and Maven 3 (3.0.5+).

At compile time, Maven should fetch all needed dependencies, which are:
* commons-lang v3.8.1
* slf4j-api v1.7.26
plus the following ones, needed for the integrated tests:
* slf4j-simple v1.7.26
* junit v4.12
* hsqldb v2.3.4
* commons-io 2.6

At runtime, Velocity only needs:
* commons-lang v3.8.1+
* slf4j-api and an slf4j binding, v1.7.26+

## BUILDING APACHE VELOCITY

In order to use the latest version of Apache Velocity, you may want to
build it.

Building is easy.  All components necessary to build are included or
get downloaded from the internet during the build, except for the Java
 SDK and the Maven build tool.  You can find details on how to build
Velocity online at:

[http://velocity.apache.org/engine/devel/build.html](http://velocity.apache.org/engine/devel/build.html)

*IMPORTANT* As the Apache Velocity build process wants to download a
number of jars from the internet, you must be online when you are
building for the first time.

To build Velocity's jar, just run maven using the command:

    mvn

This will create a target/ directory containing the Velocity .jar
file in each sub-module directory.

Be sure to update your classpath to include Velocity's .jar
file, or when using a modern servlet container, put it in the
WEB-INF/lib directory.

## TRYING THE EXAMPLES

After building Velocity, you can also build the examples that are
included with the Velocity distribution. These examples show how to
use Velocity in your Java applications.

For more information, please see the README.txt in the
velocity-engine-examples/src/etc/ directory.

- The Apache Velocity Team
