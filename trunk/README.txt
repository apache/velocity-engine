---------------
Apache Velocity
---------------

Welcome to Apache Velocity! Apache Velocity is a general purpose
template engine written in Java. For more information about Velocity,
please look at the HTML documentation in the docs/ directory, as well
as the Velocity web site.

  http://velocity.apache.org/index.html

The following top level directories are in the Velocity distribution
archive. Please consult the documentation in each of the lower level
directories for information that is specific to their contents.

build/      This is where the build scripts live.
convert/    The WebMacro to Apache Velocity conversion program.
docs/       Velocity Documentation in HTML format.
docs/api/   Velocity Javadocs.
examples/   Examples how to use Velocity.
lib/        Dependencies for building and using Velocity.
lib/test/   Dependencies needed for the various unit tests.
src/        This is where all of the source code is located.
test/       Contains test files needed for the unit tests.
xdocs/      Here are the .xml files for building the .html files
            related to the website and documentation. The files
            located in docs/ have been built from these sources.

Caveat! This is the directory structure of the distribution
archive. If you checked out the source from the Apache Subversion
Repository, the directory layout is slightly different.

            
REQUIREMENTS
------------

Apache Velocity will run with any version of Java greater than 1.3.

Building from source requires Java version 1.4 (or greater) and ant
1.6 or greater.

UPGRADING FROM EARLIER RELEASES
-------------------------------

Release with the same major number (1.x) are intended to be drop-in
replacements. However, in most cases the versions of dependency jars
must be adjusted because newer versions of Velocity might require
updates.

Upgrading from Velocity 1.4 or earlier

    * JDOM has been upgraded to version 1.0.
    * Commons Collections has been upgraded to version 3.1.
    * Commons Lang 2.1 has been added.

   Optional:

    * Apache Ant 1.6 or better is required for rebuilding.
    * Java CC 3.2 is recommended to compile the parser files.
    * HSQLDB 1.7.1 is required for running unit tests.

Upgrading from Velocity 1.5

    * Commons Collections has been upgraded to version 3.2.1.
    * Commons Lang has been upgraded to version 2.4.
    * Commons Logging 1.1 has been added.

    Optional:

    * Maven Ant 2.0.9 is required for the Maven Ant tasks.
    * Java CC 4.1 is recommended to compile the parser files.


BUILDING APACHE VELOCITY
------------------------

In order to use the latest version of Apache Velocity, you may want to
build it.

Building is easy.  All components necessary to build are included or
get downloaded from the internet during the build, except for the Java
2 SDK and the Ant build tool.  You can find details on how to build
Velocity online at:

http://velocity.apache.org/engine/devel/build.html

Note that you must use Ant version 1.6 or later.

*IMPORTANT* As the Apache Velocity build process wants to download a
 number of jars from the internet, you must be online when you are
 building for the first time.

To build Velocity's jar, change directory into the build/ directory
and enter:

ant jar

This will create a bin/ directory containing the Velocity .jar
file. Be sure to update your classpath to include Velocity's .jar
file, or when using a modern servlet container, put it in the
WEB-INF/lib directory.

If you wish to build a jar that contains all dependencies, we have
provided an optional build target for your convenience:

ant jar-dep

This will build a complete Velocity jar with dependencies included,
and it can be found in the /bin directory as

velocity-dep-<version>.jar


KNOWN ISSUES AND LIMITATIONS
----------------------------

When running findbugs on a project, the default heap size might not
be enough to complete the build. For now there is no way to fork
findbugs and run with its own memory requirements, but the following
system variable will allow you to do so when running it via Maven:

export MAVEN_OPTS=-Xmx384M


TRYING THE EXAMPLES
-------------------

After building Velocity, you can also buld the examples that are
included with the Velocity distribution.  These examples show how to
use Velocity in your Java applications.  There also are examples of
how to use Anakia, a XML transformation engine.

For more information, please see the README.txt in the examples/
directory.

- The Apache Velocity Team
