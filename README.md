Title: Apache Velocity Engine

# Apache Velocity

Welcome to Apache Velocity Engine! Apache Velocity is a general purpose
template engine written in Java. For more information about Velocity,
please look at the HTML documentation on the [Velocity web site](http://velocity.apache.org/index.html).

Here's a description of the top level directories:

    velocity-engine-core/       The Velocity Engine core module
    velocity-engine-examples/   Several simple examples
    velocity-engine-scripting/  JSR-223 implementation for Velocity scripting
    spring-velocity-support     Velocity Engine factory bean for Spring framework
    src/                        Source for parent modules, mainly changelog

## REQUIREMENTS

Apache Velocity 2.2 will run with any Java runtime engine v1.8 or greater.

Building from source requires Java development kit v1.8 or greater and Maven 3 (3.0.5+).

At compile time, Maven should fetch all engine needed dependencies, which are:

* commons-lang v3.9
* slf4j-api v1.7.30

plus the following ones, needed for the integrated tests:

* slf4j-simple v1.7.30
* junit v4.13
* hsqldb v2.5.0
* commons-io 2.8.0

At runtime, Velocity only needs:

* commons-lang v3.9+
* slf4j-api and an slf4j binding, v1.7.30+

## BUILDING APACHE VELOCITY

In order to use the latest version of Apache Velocity, you may want to
build it.

Building is easy.  All components necessary to build are included or
get downloaded from the internet during the build, except for the Java
 SDK and the Maven build tool. You can find details online on [how to build
Velocity](http://velocity.apache.org/engine/devel/build.html).

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

## CUSTOMIZING THE PARSER

Since 2.2, it's possible to [build a custom parser](http://velocity.apache.org/engine/2.2/developer-guide.html#customizing-the-vtl-parser), to change some of the characters used by in the VTL syntax: `*`, `@`, `$` and `#`.

Let's say you want to merge some templatized jQuery code full of `$` characters, you can for instance build you own parser which will use the `§` character as references prefix instead of `$`.

## TRYING THE EXAMPLES

After building Velocity, you can also build the examples that are
included with the Velocity distribution. These examples show how to
use Velocity in your Java applications.

For more information, please see the [examples README](velocity-engine-examples) in the `velocity-engine-examples` directory.

___

- The Apache Velocity Team

