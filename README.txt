--------
Velocity
--------

Welcome to Velocity. Velocity is a general purpose template engine
written in Java. For more information about Velocity, please look at the
HTML documentation in the docs/ directory, as well as the Velocity
web site

  http://jakarta.apache.org/velocity/index.html

Here is a description of what each of the top level directories
contains. Please consult the documentation in each of the lower level
directories for information that is specific to their contents.

bin/        This is a temporary build directory.
build/      This is where the build scripts live.
docs/       This is where the documentation lives.
examples/   This is where the examples live.
src/        This is where all of the source code to Velocity lives.
test/       This is where all of the tests live.
xdocs/      This is the .xml files for building the .html files
            related to the website and documentation.
            
REQUIREMENTS
------------

The Java 2 SDK is required to build Velocity.

BUILDING VELOCITY
-----------------

In order to get started with Velocity, you may want to build it.  If 
you are working with an offical Velocity release package, you will find
the Velocity jar in the top level directory.

Building Velocity is easy.  All components necessary to build Velocity are
included, except for the Java 2 SDK.

To build Velocity, change directory into the build/ directory and 
execute the platform-specific build script.

Unix:
./build.sh

Win32:
build.bat

This will create a bin/ directory containing the Velocity .jar file. Be
sure to update your classpath to include xVelocity's .jar file.

TRYING THE EXAMPLES
-------------------

After building Velocity, you can also buld the examples that are included
with the Velocity distribution.  These examples show how to use Velocity
in your Java applications and Servlets.  There also are examples of
how to use Anakia, a XML transformation engine and an example of a servlet-
based forum application.

For more information, please see the README.txt in the examples/ directory.

-The Velocity Team
