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

For users that wish to use Log4J as the logging
system, version 1.1 of Log4J is required.

BUILDING VELOCITY
-----------------

In order to get started with Velocity, you may want to build it.  If 
you are working with an offical Velocity release package, you will find
the Velocity jar in the top level directory.

Note that starting with the versio 1.2 release, this Velocity jar 
does *NOT* contain any classes from external packages that it 
depends on.

Specifically, these are the Jakarta commons-collections, 
Jakarta ORO, and Jakarta Avalon Logkit.  These jars are included 
in the distribution in build/lib, and further information can be 
found in the developers guide.

Building Velocity is easy.  All components necessary to build Velocity are
included, except for the Java 2 SDK and the fabulous Ant build tool
from the Jakarta project.

http://jakarta.apache.org/ant/

To build Velocity's jar, change directory into the build/ directory and 
simply type :

ant jar

This will create a bin/ directory containing the Velocity .jar file. Be
sure to update your classpath to include Velocity's .jar file, or when using a 
modern servlet container, put it in the WEB-INF/lib directory.

If you wish to build a Velocity jar that contains all dependencies, we have
provided an optional build target for our convenience :

ant jar-dep

This will build a complete Velocity jar with dependencies included, and it can
be found in the /bin directory as

velocity-dep-1.2.jar

TRYING THE EXAMPLES
-------------------

After building Velocity, you can also buld the examples that are included
with the Velocity distribution.  These examples show how to use Velocity
in your Java applications and Servlets.  There also are examples of
how to use Anakia, a XML transformation engine and an example of a servlet-
based forum application.

For more information, please see the README.txt in the examples/ directory.

-The Velocity Team
